package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.UserResponseDto;
import com.example.carsharingservice.dto.UserUpdateRequestDto;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "User management", description = "Endpoints for managing user profiles and roles")
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role",
            description = "Change role for a specific user. Available for Managers only.")
    public UserResponseDto updateRole(@PathVariable Long id, @RequestParam User.UserRole role) {
        return userService.updateRole(id, role);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
            description = "Retrieve profile info of the currently authenticated user.")
    public UserResponseDto getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return userService.getProfile(email);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current user profile",
            description = "Update profile details (first name, last name) of the logged-in user.")
    public UserResponseDto updateMyProfile(
            Authentication authentication,
            @RequestBody @Valid UserUpdateRequestDto requestDto
    ) {
        String email = authentication.getName();
        return userService.updateProfile(email, requestDto);
    }
}
