package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.CarRequestDto;
import com.example.carsharingservice.dto.CarResponseDto;
import com.example.carsharingservice.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cars management", description = "Endpoints for managing cars")
@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @PostMapping
    @Operation(summary = "Create a new car", description = "Add a new car to the rent catalog")
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponseDto createCar(@RequestBody @Valid CarRequestDto requestDto) {
        return carService.save(requestDto);
    }

    @GetMapping
    @Operation(summary = "Get all cars",
            description = "Retrieve a paginated list of all cars")
    public List<CarResponseDto> getAllCars(@PageableDefault(size = 10, page = 0)Pageable pageable) {
        return carService.findAll(pageable).getContent();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a car by ID",
            description = "Retrieve specific car by its ID")
    public CarResponseDto getCarById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a car",
            description = "Updates car information, also its inventory by ID")
    public CarResponseDto updateCar(
            @PathVariable Long id,
            @RequestBody @Valid CarRequestDto requestDto
    ) {
        return carService.update(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a car by ID",
            description = "Soft-delete an existing car by its ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
