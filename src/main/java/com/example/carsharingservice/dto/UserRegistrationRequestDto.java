package com.example.carsharingservice.dto;

import com.example.carsharingservice.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldMatch(
        first = "password",
        second = "repeatPassword",
        message = "incorrect password"
)
public record UserRegistrationRequestDto(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        @Size(min = 8, max = 35)
        String password,

        @NotBlank
        @Size(min = 8, max = 35)
        String repeatPassword
) {
}
