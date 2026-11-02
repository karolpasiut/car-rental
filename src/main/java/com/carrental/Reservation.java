package com.carrental;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Reservation {
    private final UUID id;
    private final Car car;
    private final LocalDateTime start;
    private final int durationDays;

    public Reservation(UUID id, Car car, LocalDateTime start, int durationDays) {
        this.id = Objects.requireNonNull(id, "id");
        this.car = Objects.requireNonNull(car, "car");
        this.start = Objects.requireNonNull(start, "start");
        if (durationDays < 1) {
            throw new IllegalArgumentException("durationDays must be at least 1");
        }
        this.durationDays = durationDays;
    }

    public UUID getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public LocalDateTime getEnd() {
        return start.plusDays(durationDays);
    }

    boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(getEnd());
    }
}
