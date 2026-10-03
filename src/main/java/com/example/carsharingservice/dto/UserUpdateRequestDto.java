package com.example.carsharingservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDto(
        @NotBlank
        String firstName,

        @NotBlank
        String lastName
) {
}
