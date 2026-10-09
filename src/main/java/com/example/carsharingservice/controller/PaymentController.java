package com.example.carsharingservice.controller;

import com.example.carsharingservice.dto.PaymentRequestDto;
import com.example.carsharingservice.dto.PaymentResponseDto;
import com.example.carsharingservice.exception.EntityNotFoundException;
import com.example.carsharingservice.mapper.PaymentMapper;
import com.example.carsharingservice.model.Payment;
import com.example.carsharingservice.model.User;
import com.example.carsharingservice.repository.UserRepository;
import com.example.carsharingservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Payment management", description = "Endpoints for payments")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;

    @GetMapping
    @Operation(
            summary = "Get payments",
            description = "Retrieve payments. Customers can only see their own payments, "
                    + " while Managers can filter by user ID or get all payments."
    )
    public List<PaymentResponseDto> getPayments(
            @RequestParam(required = false, name = "user_id") Long userId,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new EntityNotFoundException("Can`t find user by ID: "
                        + authentication.getName()));
        if (currentUser.getRole() == User.UserRole.CUSTOMER) {
            userId = currentUser.getId();
        } else if (userId == null) {
            return paymentService.getAllPayments().stream()
                    .map(paymentMapper::toDto)
                    .toList();
        }
        return paymentService.getPaymentsByUserId(userId).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @PostMapping
    @Operation(
            summary = "Create payment session",
            description = "Create a new Stripe checkout session for car rental payment."
    )
    public ResponseEntity<PaymentResponseDto> createPayment(
            @RequestBody PaymentRequestDto requestDto,
            UriComponentsBuilder uriBuilder
    ) {
        String successUrl = uriBuilder.cloneBuilder()
                .path("/payments/success")
                .toUriString();

        String cancelUrl = uriBuilder.cloneBuilder()
                .path("/payments/cancel")
                .toUriString();

        Payment payment = paymentService.createPaymentSession(requestDto, successUrl, cancelUrl);
        return ResponseEntity.ok(paymentMapper.toDto(payment));
    }

    @PostMapping("/{id}/renew")
    @Operation(
            summary = "Renew payment session",
            description = "Renew an expired Stripe payment session for a specific payment ID."
    )
    public ResponseEntity<PaymentResponseDto> renewPayment(
            @PathVariable Long id,
            UriComponentsBuilder uriBuilder
    ) {
        String successUrl = uriBuilder.cloneBuilder()
                .path("/payments/success")
                .toUriString();

        String cancelUrl = uriBuilder.cloneBuilder()
                .path("/payments/cancel")
                .toUriString();

        PaymentResponseDto responseDto = paymentService.renewPaymentSession(
                id,
                successUrl,
                cancelUrl
        );
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/success")
    @Operation(
            summary = "Handle successful payment",
            description = "Callback endpoint invoked by Stripe upon successful payment completion."
    )
    public ResponseEntity<String> successPayment(@RequestParam("session_id") String sessionId) {
        paymentService.handleSuccessPayment(sessionId);
        return ResponseEntity.ok("Payment successful! Thank you.");
    }

    @GetMapping("/cancel")
    @Operation(
            summary = "Handle canceled payment",
            description = "Callback endpoint invoked when a user cancels"
                    + " their Stripe checkout session."
    )
    public ResponseEntity<String> cancelPayment() {
        return ResponseEntity.ok(paymentService.handleCancelPayment());
    }
}
