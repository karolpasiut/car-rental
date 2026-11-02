package com.carrental;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarRentalServiceTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 10, 9, 0);

    @Test
    void reservesEachCarTypeAtRequestedDateTimeAndDuration() {
        CarRentalService rental = new CarRentalService(Fleet.of(1, 1, 1));

        Reservation sedan = rental.reserve(CarType.SEDAN, START, 3);
        Reservation suv = rental.reserve(CarType.SUV, START, 2);
        Reservation van = rental.reserve(CarType.VAN, START, 5);

        assertEquals(CarType.SEDAN, sedan.getCar().getType());
        assertEquals(START, sedan.getStart());
        assertEquals(3, sedan.getDurationDays());
        assertEquals(START.plusDays(3), sedan.getEnd());

        assertEquals(CarType.SUV, suv.getCar().getType());
        assertEquals(2, suv.getDurationDays());

        assertEquals(CarType.VAN, van.getCar().getType());
        assertEquals(5, van.getDurationDays());
    }

    @Test
    void overlappingReservationsAreLimitedByFleetCountForThatType() {
        CarRentalService rental = new CarRentalService(Fleet.of(2, 0, 0));

        Reservation first = rental.reserve(CarType.SEDAN, START, 2);
        Reservation second = rental.reserve(CarType.SEDAN, START.plusHours(1), 2);

        assertNotEquals(first.getCar().getId(), second.getCar().getId());
        assertThrows(ReservationConflictException.class,
                () -> rental.reserve(CarType.SEDAN, START.plusDays(1), 1));
    }

    @Test
    void sameTypeCanBeReusedAfterTheReservationEnds() {
        CarRentalService rental = new CarRentalService(Fleet.of(1, 0, 0));

        Reservation first = rental.reserve(CarType.SEDAN, START, 2);
        Reservation later = rental.reserve(CarType.SEDAN, START.plusDays(2).plusHours(1), 1);

        assertEquals(first.getCar().getId(), later.getCar().getId());
    }

    @Test
    void adjacentIntervalsDoNotOverlap() {
        CarRentalService rental = new CarRentalService(Fleet.of(1, 0, 0));

        Reservation first = rental.reserve(CarType.SEDAN, START, 2);
        Reservation next = rental.reserve(CarType.SEDAN, START.plusDays(2), 3);

        assertEquals(first.getEnd(), next.getStart());
        assertEquals(first.getCar().getId(), next.getCar().getId());
    }

    @Test
    void differentTypesDoNotShareCapacity() {
        CarRentalService rental = new CarRentalService(Fleet.of(1, 1, 0));

        rental.reserve(CarType.SEDAN, START, 4);
        Reservation suv = rental.reserve(CarType.SUV, START, 4);

        assertEquals(CarType.SUV, suv.getCar().getType());
        assertTrue(rental.getReservations().stream()
                .anyMatch(reservation -> reservation.getCar().getType() == CarType.SEDAN));
    }

    @Test
    void rejectsInvalidDurationAndNullArguments() {
        CarRentalService rental = new CarRentalService(Fleet.of(1, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> rental.reserve(CarType.SEDAN, START, 0));
        assertThrows(IllegalArgumentException.class, () -> rental.reserve(CarType.SEDAN, START, -1));
        assertThrows(NullPointerException.class, () -> rental.reserve(null, START, 1));
        assertThrows(NullPointerException.class, () -> rental.reserve(CarType.VAN, null, 1));
    }
}
