package com.example.carsharingservice.dto;

import com.example.carsharingservice.model.Car;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CarRequestDto(
        @NotBlank
        String model,
        @NotBlank
        String brand,
        @NotNull
        Car.CarType type,
        @PositiveOrZero
        int inventory,
        @NotNull
        @Positive
        BigDecimal dailyFee
) {
}
