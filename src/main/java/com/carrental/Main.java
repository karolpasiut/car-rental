package com.carrental;

import java.time.LocalDateTime;

public final class Main {
    public static void main(String[] args) {
        CarRentalService rental = new CarRentalService(Fleet.of(2, 2, 1));
        Reservation reservation = rental.reserve(
                CarType.SEDAN,
                LocalDateTime.of(2026, 11, 10, 9, 0),
                3);
        System.out.println("Reserved " + reservation.getCar().getType()
                + " from " + reservation.getStart()
                + " for " + reservation.getDurationDays() + " day(s)");
    }
}
