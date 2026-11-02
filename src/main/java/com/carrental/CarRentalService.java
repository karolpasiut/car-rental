package com.carrental;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CarRentalService {
    private final Fleet fleet;
    private final List<Reservation> reservations = new ArrayList<>();

    public CarRentalService(Fleet fleet) {
        this.fleet = Objects.requireNonNull(fleet, "fleet");
    }

    public Reservation reserve(CarType type, LocalDateTime start, int durationDays) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(start, "start");
        if (durationDays < 1) {
            throw new IllegalArgumentException("durationDays must be at least 1");
        }

        LocalDateTime end = start.plusDays(durationDays);
        Car available = findAvailableCar(type, start, end);
        if (available == null) {
            throw new ReservationConflictException(
                    "No " + type + " is available from " + start + " for " + durationDays + " day(s)");
        }

        Reservation reservation = new Reservation(UUID.randomUUID(), available, start, durationDays);
        reservations.add(reservation);
        return reservation;
    }

    private Car findAvailableCar(CarType type, LocalDateTime start, LocalDateTime end) {
        for (Car car : fleet.carsOfType(type)) {
            if (isFree(car, start, end)) {
                return car;
            }
        }
        return null;
    }

    private boolean isFree(Car car, LocalDateTime start, LocalDateTime end) {
        for (Reservation reservation : reservations) {
            if (reservation.getCar().equals(car) && reservation.overlaps(start, end)) {
                return false;
            }
        }
        return true;
    }

    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }
}
