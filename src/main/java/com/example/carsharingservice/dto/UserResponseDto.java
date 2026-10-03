package com.example.carsharingservice.dto;

import com.example.carsharingservice.model.User;

public record UserResponseDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        User.UserRole role
) {
}
