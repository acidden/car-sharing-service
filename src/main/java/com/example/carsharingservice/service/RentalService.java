package com.example.carsharingservice.service;

import com.example.carsharingservice.dto.RentalRequestDto;
import com.example.carsharingservice.dto.RentalResponseDto;
import java.util.List;

public interface RentalService {
    RentalResponseDto save(RentalRequestDto requestDto, String userEmail);

    List<RentalResponseDto> search(Long id, Boolean isActive, String userEmail);

    RentalResponseDto findById(Long id, String userEmail);

    RentalResponseDto returnCar(Long id, String userEmail);
}
