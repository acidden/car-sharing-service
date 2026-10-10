package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.CarRequestDto;
import com.example.carsharingservice.dto.CarResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.mapper.CarMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.repository.CarRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    private Car car;
    private CarRequestDto requestDto;
    private CarResponseDto responseDto;

    @BeforeEach
    void setUp() {
        car = new Car();
        car.setId(1L);
        car.setModel("Model S");
        car.setBrand("Tesla");
        car.setType(Car.CarType.SEDAN);
        car.setInventory(5);
        car.setDailyFee(BigDecimal.valueOf(100));

        requestDto = new CarRequestDto(
                "Model S",
                "Tesla",
                Car.CarType.SEDAN,
                5,
                BigDecimal.valueOf(100)
        );

        responseDto = new CarResponseDto(
                1L,
                "Model S",
                "Tesla",
                Car.CarType.SEDAN,
                5,
                BigDecimal.valueOf(100)
        );
    }

    @Test
    @DisplayName("Save car - Should successfully save and return CarResponseDto")
    void createCar_ValidRequest_ReturnsCarDto() {
        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto actualResponse = carService.save(requestDto);

        assertNotNull(actualResponse);
        assertEquals(responseDto, actualResponse);

        verify(carMapper).toModel(requestDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Find by ID - Should return CarResponseDto when car exists")
    void getCarById_ExistingId_ReturnsCarDto() {
        Long carId = 1L;
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto actualResponse = carService.findById(carId);

        assertNotNull(actualResponse);
        assertEquals(carId, actualResponse.id());
        verify(carRepository).findById(carId);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Find by ID - Should throw EntityNotFoundException when car does not exist")
    void getCarById_InvalidId_ThrowsException() {
        Long carId = 999L;
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            carService.findById(carId);
        });

        verify(carRepository).findById(carId);
        verifyNoInteractions(carMapper);
    }

    @Test
    @DisplayName("Find all cars - Should return page of CarResponseDto")
    void getAll_ValidPageable_ReturnsPageOfCarDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(responseDto);

        Page<CarResponseDto> actualPage = carService.findAll(pageable);

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(responseDto, actualPage.getContent().get(0));

        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Update car - Should successfully update and return updated CarResponseDto")
    void update_ExistingId_ReturnsUpdatedResponseDto() {
        Long carId = 1L;

        when(carRepository.existsById(carId)).thenReturn(true);
        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(responseDto);

        CarResponseDto actualResponse = carService.update(carId, requestDto);

        assertNotNull(actualResponse);
        assertEquals(carId, actualResponse.id());
        verify(carRepository).existsById(carId);
        verify(carMapper).toModel(requestDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Update car - Should throw EntityNotFoundException when car to update not found")
    void update_NonExistingId_ThrowsEntityNotFoundException() {
        Long carId = 99L;
        when(carRepository.existsById(carId)).thenReturn(false);

       assertThrows(EntityNotFoundException.class, () ->
                carService.update(carId, requestDto)
        );

        verify(carRepository).existsById(carId);
        verifyNoMoreInteractions(carRepository);
        verifyNoInteractions(carMapper);
    }

    @Test
    @DisplayName("Delete by ID - Should successfully delete car when it exists")
    void deleteById_ExistingId_DeletesCar() {
        Long carId = 1L;
        when(carRepository.existsById(carId)).thenReturn(true);
        doNothing().when(carRepository).deleteById(carId);

        carService.deleteById(carId);

        verify(carRepository).existsById(carId);
        verify(carRepository).deleteById(carId);
    }

    @Test
    @DisplayName("Delete by ID - Should throw EntityNotFoundException when car to delete not found")
    void deleteById_NonExistingId_ThrowsEntityNotFoundException() {
        Long carId = 99L;
        when(carRepository.existsById(carId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
                carService.deleteById(carId)
        );

        verify(carRepository).existsById(carId);
        verify(carRepository, never()).deleteById(anyLong());
    }
}
