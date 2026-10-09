package com.example.carsharingservice.mapper;

import com.example.carsharingservice.config.MapperConfig;
import com.example.carsharingservice.dto.PaymentRequestDto;
import com.example.carsharingservice.dto.PaymentResponseDto;
import com.example.carsharingservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    @Mapping(source = "rental.id", target = "rentalId")
    PaymentResponseDto toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "sessionUrl", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "amountToPay", ignore = true)
    @Mapping(target = "rental", ignore = true)
    Payment toModel(PaymentRequestDto requestDto);
}

