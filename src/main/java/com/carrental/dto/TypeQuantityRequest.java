package com.carrental.dto;

import com.carrental.model.CarType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TypeQuantityRequest(
        @NotNull CarType type,
        @Min(1) int quantity
) {
}
