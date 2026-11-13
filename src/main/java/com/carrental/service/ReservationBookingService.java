package com.carrental.service;

import com.carrental.dto.AvailabilityResponse;
import com.carrental.dto.BookingRequest;
import com.carrental.dto.CarDto;
import com.carrental.dto.CarsResponse;
import com.carrental.dto.ReservationDto;
import com.carrental.dto.TypeQuantityRequest;
import com.carrental.exception.ApiException;
import com.carrental.model.Car;
import com.carrental.model.CarType;
import com.carrental.model.Reservation;
import com.carrental.repository.CarRepository;
import com.carrental.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationBookingService {

    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;
    private final EntityManager entityManager;

    public ReservationBookingService(
            CarRepository carRepository,
            ReservationRepository reservationRepository,
            EntityManager entityManager) {
        this.carRepository = carRepository;
        this.reservationRepository = reservationRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public CarsResponse listCars(LocalDate startDate, LocalDate endDate) {
        if ((startDate == null) != (endDate == null)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "startDate and endDate must be provided together. Missing "
                            + (startDate == null ? "startDate" : "endDate"));
        }

        List<Car> allCars = carRepository.findAllByOrderByTypeAscBrandAscModelAsc();
        List<CarDto> carDtos = allCars.stream().map(CarDto::from).toList();

        if (startDate == null) {
            Map<CarType, Integer> fleetCounts = countByType(allCars);
            return new CarsResponse(carDtos, fleetCounts);
        }

        validateDateRange(startDate, endDate);
        List<Car> available = findAvailableCars(startDate, endDate);
        Map<CarType, Integer> availability = countByType(available);
        for (CarType type : CarType.values()) {
            availability.putIfAbsent(type, 0);
        }
        return new CarsResponse(
                available.stream().map(CarDto::from).toList(),
                availability);
    }

    @Transactional(readOnly = true)
    public ReservationDto getReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found: " + id));
        return ReservationDto.from(reservation);
    }

    @Transactional
    public ReservationDto bookReservation(BookingRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        Map<CarType, Integer> requested = toRequestedMap(request.cars());
        lockCarsForUpdate(List.copyOf(requested.keySet()));

        List<Car> selected = allocateCars(request.startDate(), request.endDate(), requested);
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                request.startDate(),
                request.endDate(),
                new HashSet<>(selected));
        try {
            return ReservationDto.from(reservationRepository.saveAndFlush(reservation));
        } catch (DataIntegrityViolationException ex) {
            String cause = ex.getMostSpecificCause().getMessage();
            if (cause != null && cause.toLowerCase().contains("already booked")) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Car is already booked for an overlapping period");
            }
            throw ex;
        }
    }

    @Transactional
    public ReservationDto cancelReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found: " + id));
        if (reservation.isCancelled()) {
            throw new ApiException(HttpStatus.CONFLICT, "Reservation is already cancelled");
        }
        reservation.cancel();
        return ReservationDto.from(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> listReservations(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return reservationRepository.findActiveOverlapping(startDate, endDate).stream()
                .map(ReservationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(BookingRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        Map<CarType, Integer> requested = toRequestedMap(request.cars());
        Map<CarType, Integer> availableByType = countByType(findAvailableCars(request.startDate(), request.endDate()));
        for (CarType type : CarType.values()) {
            availableByType.putIfAbsent(type, 0);
        }

        boolean available = requested.entrySet().stream()
                .allMatch(entry -> availableByType.getOrDefault(entry.getKey(), 0) >= entry.getValue());
        return new AvailabilityResponse(available, requested, availableByType);
    }

    /**
     * Locks every car of the requested types, in id order, before the availability
     * read. The second concurrent booking waits, then sees the first assignment.
     */
    private void lockCarsForUpdate(Collection<CarType> types) {
        if (types.isEmpty()) {
            return;
        }
        List<UUID> ids = entityManager.createQuery(
                        "select c.id from Car c where c.type in :types order by c.id",
                        UUID.class)
                .setParameter("types", types)
                .getResultList();
        for (UUID id : ids) {
            if (entityManager.find(Car.class, id, LockModeType.PESSIMISTIC_WRITE) == null) {
                throw new ApiException(HttpStatus.CONFLICT, "Car no longer exists: " + id);
            }
        }
    }

    private List<Car> allocateCars(LocalDate startDate, LocalDate endDate, Map<CarType, Integer> requested) {
        List<Car> available = findAvailableCars(startDate, endDate);
        Map<CarType, List<Car>> byType = available.stream()
                .collect(Collectors.groupingBy(Car::getType, () -> new EnumMap<>(CarType.class), Collectors.toList()));

        List<Car> selected = new ArrayList<>();
        for (Map.Entry<CarType, Integer> entry : requested.entrySet()) {
            List<Car> pool = byType.getOrDefault(entry.getKey(), List.of());
            if (pool.size() < entry.getValue()) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "Unavailable: type " + entry.getKey()
                                + " — requested " + entry.getValue()
                                + ", available " + pool.size());
            }
            selected.addAll(pool.subList(0, entry.getValue()));
        }
        return selected;
    }

    /**
     * A car is available for the whole range when it does not overlap any active
     * reservation on any day in [startDate, endDate].
     */
    private List<Car> findAvailableCars(LocalDate startDate, LocalDate endDate) {
        Set<UUID> busyCarIds = new LinkedHashSet<>();
        for (Reservation reservation : reservationRepository.findActiveOverlapping(startDate, endDate)) {
            for (Car car : reservation.getCars()) {
                busyCarIds.add(car.getId());
            }
        }

        return carRepository.findAllByOrderByTypeAscBrandAscModelAsc().stream()
                .filter(car -> !busyCarIds.contains(car.getId()))
                .toList();
    }

    private static Map<CarType, Integer> toRequestedMap(List<TypeQuantityRequest> cars) {
        Map<CarType, Integer> requested = new EnumMap<>(CarType.class);
        for (TypeQuantityRequest item : cars) {
            requested.merge(item.type(), item.quantity(), Integer::sum);
        }
        return requested;
    }

    private static Map<CarType, Integer> countByType(List<Car> cars) {
        Map<CarType, Integer> counts = new EnumMap<>(CarType.class);
        for (Car car : cars) {
            counts.merge(car.getType(), 1, Integer::sum);
        }
        return counts;
    }

    private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Both dates are required: startDate and endDate");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate cannot be earlier than startDate");
        }
    }
}
