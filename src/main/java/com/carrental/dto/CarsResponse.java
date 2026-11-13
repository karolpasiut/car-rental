package com.carrental.dto;

import com.carrental.model.CarType;

import java.util.List;
import java.util.Map;

public record CarsResponse(
        List<CarDto> cars,
        Map<CarType, Integer> availabilityByType
) {
}
