package com.example.carsharingservice.dto;

import com.example.carsharingservice.model.Car;
import java.math.BigDecimal;

public record CarResponseDto(
        Long id,
        String model,
        String brand,
        Car.CarType type,
        int inventory,
        BigDecimal dailyFee
) {}
