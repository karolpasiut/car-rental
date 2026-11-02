package com.carrental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Fleet {
    private final List<Car> cars;

    public Fleet(Map<CarType, Integer> countsByType) {
        if (countsByType == null) {
            throw new IllegalArgumentException("countsByType");
        }
        List<Car> built = new ArrayList<>();
        for (CarType type : CarType.values()) {
            int count = countsByType.getOrDefault(type, 0);
            if (count < 0) {
                throw new IllegalArgumentException("count for " + type + " must be non-negative");
            }
            for (int i = 0; i < count; i++) {
                built.add(new Car(UUID.randomUUID(), type));
            }
        }
        this.cars = List.copyOf(built);
    }

    public static Fleet of(int sedans, int suvs, int vans) {
        Map<CarType, Integer> counts = new EnumMap<>(CarType.class);
        counts.put(CarType.SEDAN, sedans);
        counts.put(CarType.SUV, suvs);
        counts.put(CarType.VAN, vans);
        return new Fleet(counts);
    }

    public List<Car> carsOfType(CarType type) {
        List<Car> matching = new ArrayList<>();
        for (Car car : cars) {
            if (car.getType() == type) {
                matching.add(car);
            }
        }
        return Collections.unmodifiableList(matching);
    }

    public List<Car> allCars() {
        return cars;
    }
}
