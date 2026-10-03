package com.example.carsharingservice.service.impl;

import com.example.carsharingservice.dto.CarRequestDto;
import com.example.carsharingservice.dto.CarResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.mapper.CarMapper;
import com.example.carsharingservice.model.Car;
import com.example.carsharingservice.repository.CarRepository;
import com.example.carsharingservice.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional
    public CarResponseDto save(CarRequestDto requestDto) {
        Car car = carMapper.toModel(requestDto);
        Car savedCar = carRepository.save(car);
        return carMapper.toDto(savedCar);
    }

    @Override
    public Page<CarResponseDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Override
    public CarResponseDto findById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can`t find car by ID: " + id)
        );
        return carMapper.toDto(car);
    }

    @Override
    @Transactional
    public CarResponseDto update(Long id, CarRequestDto requestDto) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Can`t update car. Car not found with ID:" + id);
        }
        Car car = carMapper.toModel(requestDto);
        car.setId(id);
        Car updatedCar = carRepository.save(car);
        return carMapper.toDto(updatedCar);
    }

    @Override
    public void deleteById(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Can`t delete car. Car not found with ID:" + id);
        }
        carRepository.deleteById(id);
    }
}
