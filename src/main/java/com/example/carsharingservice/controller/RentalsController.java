package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.RentalRequestDto;
import com.example.carsharingservice.dto.RentalResponseDto;
import com.example.carsharingservice.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rentals management", description = "Endpoints for managing car rentals")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalsController {
    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new rental",
            description = "Rent a car if available. Decreases car inventory by 1")
    public RentalResponseDto createRental(
            @RequestBody @Valid RentalRequestDto requestDto,
            Authentication authentication
    ) {
        return rentalService.save(requestDto, authentication.getName());
    }

    @GetMapping
    @Operation(summary = "Get rentals by user ID and status",
            description = "Get list of rentals. Customers see only their own."
            + "Managers can filter by user id and active status.")
    public List<RentalResponseDto> getRentals(
            @RequestParam(required = false, name = "user_id") Long userId,
            @RequestParam(required = false, name = "is_active") Boolean isActive,
            Authentication authentication
    ) {
        return rentalService.search(userId, isActive, authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific rental by ID",
            description = "Retrieve rental detailed information")
    public RentalResponseDto getRentalById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return rentalService.findById(id, authentication.getName());
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return a rented car",
            description = "Set actual return date. Increases car inventory by 1.")
    public RentalResponseDto returnCar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return rentalService.returnCar(id, authentication.getName());
    }
}
