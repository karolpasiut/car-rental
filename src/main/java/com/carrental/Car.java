package com.carrental;

import java.util.Objects;
import java.util.UUID;

public final class Car {
    private final UUID id;
    private final CarType type;

    public Car(UUID id, CarType type) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
    }

    public UUID getId() {
        return id;
    }

    public CarType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Car car)) {
            return false;
        }
        return id.equals(car.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
