package com.carrental.dto;

import com.carrental.model.CarType;

import java.util.Map;

public record AvailabilityResponse(
        boolean available,
        Map<CarType, Integer> requested,
        Map<CarType, Integer> availableByType
) {
}
