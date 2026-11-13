package com.carrental.controller;

import com.carrental.dto.AvailabilityResponse;
import com.carrental.dto.BookingRequest;
import com.carrental.dto.CarsResponse;
import com.carrental.dto.ReservationDto;
import com.carrental.service.ReservationBookingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ReservationBookingController {

    private final ReservationBookingService bookingService;

    public ReservationBookingController(ReservationBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/cars")
    public CarsResponse listCars(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return bookingService.listCars(startDate, endDate);
    }

    @GetMapping("/reservation/{id}")
    public ReservationDto getReservation(@PathVariable UUID id) {
        return bookingService.getReservation(id);
    }

    @PostMapping("/bookReservation")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDto bookReservation(@Valid @RequestBody BookingRequest request) {
        return bookingService.bookReservation(request);
    }

    @PostMapping("/cancelReservation/{id}")
    public ReservationDto cancelReservationPost(@PathVariable UUID id) {
        return bookingService.cancelReservation(id);
    }

    @DeleteMapping("/cancelReservation/{id}")
    public ReservationDto cancelReservationDelete(@PathVariable UUID id) {
        return bookingService.cancelReservation(id);
    }

    @GetMapping("/reservations/{startDate}/{endDate}")
    public List<ReservationDto> listReservations(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return bookingService.listReservations(startDate, endDate);
    }

    @PostMapping("/checkAvailability")
    public AvailabilityResponse checkAvailability(@Valid @RequestBody BookingRequest request) {
        return bookingService.checkAvailability(request);
    }
}
