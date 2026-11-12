package com.carrental.dto;

import com.carrental.model.Reservation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record ReservationDto(
        UUID id,
        LocalDate startDate,
        LocalDate endDate,
        boolean cancelled,
        List<CarDto> cars
) {
    public static ReservationDto from(Reservation reservation) {
        List<CarDto> cars = reservation.getCars().stream()
                .map(CarDto::from)
                .sorted(Comparator.comparing(CarDto::type).thenComparing(CarDto::brand))
                .toList();
        return new ReservationDto(
                reservation.getId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.isCancelled(),
                cars);
    }
}
