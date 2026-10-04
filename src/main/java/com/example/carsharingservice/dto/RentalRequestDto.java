package com.example.carsharingservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RentalRequestDto(
        @NotNull
        Long carId,

        @NotNull
        @FutureOrPresent
        LocalDate returnDate
) {}
