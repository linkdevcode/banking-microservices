package com.linkdevcode.banking.payment_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.linkdevcode.banking.payment_service.model.request.DepositRequest;
import com.linkdevcode.banking.payment_service.model.request.DispenseRequest;
import com.linkdevcode.banking.payment_service.model.request.TransferRequest;
import com.linkdevcode.banking.payment_service.model.response.PaymentResponse;
import com.linkdevcode.banking.payment_service.service.PaymentService;

/**
 * Controller for handling external payment and transfer related APIs.
 * Mapped to /api/payment via API Gateway.
 */
@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment and Transfer Management", description = "APIs for initiating transfers and viewing transaction history.")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Executes a deposit into the user's account.
     * This endpoint relies on the API Gateway to extract the userId from the JWT token.
     * @param request The deposit details (amount, message).
     * @param userId The ID of the user receiving the deposit (In a real system, this is passed via a custom header/argument resolver from the Gateway).
     * @return TransferResponse indicating success or failure status.
     */
    @Operation(summary = "Deposit money into user's account")
    @PostMapping("/deposits")
    public ResponseEntity<PaymentResponse> deposit(
        @RequestHeader(name = "X-User-Id", required = true) Long userId,
        @Valid @RequestBody DepositRequest request) {

        PaymentResponse response = paymentService.processDeposit(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Executes a dispense from the user's account.
     * This endpoint relies on the API Gateway to extract the userId from the JWT token.
     * @param request The dispense details (amount, message).
     * @param userId The ID of the user dispensing the money (In a real system, this is passed via a custom header/argument resolver from the Gateway).
     * @return TransferResponse indicating success or failure status.
     */
    @Operation(summary = "Dispense money from user's account")
    @PostMapping("/dispenses")
    public ResponseEntity<PaymentResponse> dispense(
        @RequestHeader(name = "X-User-Id", required = true) Long userId,
        @Valid @RequestBody DispenseRequest request) {

        PaymentResponse response = paymentService.processDispense(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Executes a money transfer between two user accounts.
     * This endpoint relies on the API Gateway to extract the senderId from the JWT token.
     * @param request The transfer details (recipientId, amount, message).
     * @param senderId The ID of the user initiating the transfer (In a real system, this is passed via a custom header/argument resolver from the Gateway).
     * @return TransferResponse indicating success or failure status.
     */
    @Operation(summary = "Initiate a money transfer to a recipient's account")
    @PostMapping("/transfers")
    public ResponseEntity<PaymentResponse> transferMoney(
        @RequestHeader(name = "X-User-Id", required = true) Long senderId,
        @Valid @RequestBody TransferRequest request) {

        PaymentResponse response = paymentService.processTransfer(senderId, request);
        return ResponseEntity.ok(response);
    }
}