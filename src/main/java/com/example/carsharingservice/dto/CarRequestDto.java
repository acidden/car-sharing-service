package com.example.carsharingservice.dto;

import com.example.carsharingservice.model.Car;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CarRequestDto(
        @NotBlank(message = "Model cannot be blank")
        String model,
        @NotBlank(message = "Brand cannot be blank")
        String brand,
        @NotNull(message = "Car type cannot be null")
        Car.CarType type,
        @PositiveOrZero(message = "Inventory must be zero or positive number")
        int inventory,
        @NotNull(message = "Daily fee cannot be null")
        @Positive(message = "Daily fee must be a positive")
        BigDecimal dailyFee
) {
}
