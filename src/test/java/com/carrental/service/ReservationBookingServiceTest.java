package com.carrental.service;

import com.carrental.dto.AvailabilityResponse;
import com.carrental.dto.BookingRequest;
import com.carrental.dto.ReservationDto;
import com.carrental.dto.TypeQuantityRequest;
import com.carrental.exception.ApiException;
import com.carrental.model.Car;
import com.carrental.model.CarType;
import com.carrental.repository.CarRepository;
import com.carrental.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(ReservationBookingService.class)
class ReservationBookingServiceTest {

    @Autowired
    private ReservationBookingService service;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private static final LocalDate START = LocalDate.of(2026, 11, 20);
    private static final LocalDate END = LocalDate.of(2026, 11, 22);

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        carRepository.deleteAll();
        carRepository.saveAll(List.of(
                new Car(UUID.randomUUID(), CarType.SUV, "Toyota", "RAV4", "photo"),
                new Car(UUID.randomUUID(), CarType.SUV, "Kia", "Sportage", "photo"),
                new Car(UUID.randomUUID(), CarType.SEDAN, "Toyota", "Corolla", "photo"),
                new Car(UUID.randomUUID(), CarType.VAN, "VW", "Golf", "photo")
        ));
    }

    @Test
    void booksRequestedQuantitiesAndBlocksOverlap() {
        BookingRequest request = new BookingRequest(START, END, List.of(
                new TypeQuantityRequest(CarType.SUV, 2),
                new TypeQuantityRequest(CarType.SEDAN, 1)
        ));

        ReservationDto booked = service.bookReservation(request);
        assertEquals(3, booked.cars().size());

        AvailabilityResponse availability = service.checkAvailability(request);
        assertFalse(availability.available());

        assertThrows(ApiException.class, () -> service.bookReservation(request));
    }

    @Test
    void cancelFreesCarsForNewBooking() {
        BookingRequest request = new BookingRequest(START, END, List.of(
                new TypeQuantityRequest(CarType.VAN, 1)
        ));
        ReservationDto booked = service.bookReservation(request);
        service.cancelReservation(booked.id());

        AvailabilityResponse availability = service.checkAvailability(request);
        assertTrue(availability.available());
        assertEquals(1, service.bookReservation(request).cars().size());
    }

    @Test
    void availabilityCountsWholeRange() {
        service.bookReservation(new BookingRequest(START, END, List.of(
                new TypeQuantityRequest(CarType.SUV, 1)
        )));

        var response = service.listCars(START, END);
        assertEquals(1, response.availabilityByType().get(CarType.SUV));
        assertEquals(1, response.availabilityByType().get(CarType.SEDAN));
        assertEquals(1, response.availabilityByType().get(CarType.VAN));
    }

    @Test
    void listCarsRejectsASingleDate() {
        ApiException missingEnd = assertThrows(ApiException.class, () -> service.listCars(START, null));
        assertEquals(HttpStatus.BAD_REQUEST, missingEnd.getStatus());
        assertTrue(missingEnd.getMessage().contains("endDate"));

        ApiException missingStart = assertThrows(ApiException.class, () -> service.listCars(null, END));
        assertEquals(HttpStatus.BAD_REQUEST, missingStart.getStatus());
        assertTrue(missingStart.getMessage().contains("startDate"));
    }
}
