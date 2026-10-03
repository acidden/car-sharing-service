package com.example.carsharingservice.service;

import com.example.carsharingservice.dto.CarRequestDto;
import com.example.carsharingservice.dto.CarResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto save(CarRequestDto requestDto);

    Page<CarResponseDto> findAll(Pageable pageable);

    CarResponseDto findById(Long id);

    CarResponseDto update(Long id, CarRequestDto requestDto);

    void deleteById(Long id);
}
