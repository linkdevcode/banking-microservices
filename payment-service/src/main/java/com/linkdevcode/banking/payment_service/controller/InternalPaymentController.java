package com.linkdevcode.banking.payment_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.payment_service.model.request.TransferRequest;
import com.linkdevcode.banking.payment_service.model.response.PaymentResponse;
import com.linkdevcode.banking.payment_service.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/payment")
@RequiredArgsConstructor
public class InternalPaymentController {
    
    private final PaymentService paymentService;

    @PostMapping("/transfer")
    public ResponseEntity<PaymentResponse> transferMoney(
        @Valid @RequestBody TransferRequest request) {

        PaymentResponse response = paymentService.systemTransfer(request);
        return ResponseEntity.ok(response);
    }
}
