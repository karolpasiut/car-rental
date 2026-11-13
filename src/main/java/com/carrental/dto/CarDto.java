package com.carrental.dto;

import com.carrental.model.Car;
import com.carrental.model.CarType;

import java.util.UUID;

public record CarDto(
        UUID id,
        CarType type,
        String brand,
        String model,
        String photoUrl
) {
    public static CarDto from(Car car) {
        return new CarDto(
                car.getId(),
                car.getType(),
                car.getBrand(),
                car.getModel(),
                car.getPhotoUrl());
    }
}
