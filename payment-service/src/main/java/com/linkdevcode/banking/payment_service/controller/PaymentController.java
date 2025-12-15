package com.linkdevcode.banking.payment_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.linkdevcode.banking.payment_service.model.request.TransferRequest;
import com.linkdevcode.banking.payment_service.model.response.TransactionHistoryResponse;
import com.linkdevcode.banking.payment_service.model.response.TransferResponse;
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
     * Executes a money transfer between two user accounts.
     * This endpoint relies on the API Gateway to extract the senderId from the JWT token.
     * @param request The transfer details (recipientId, amount, message).
     * @param senderId The ID of the user initiating the transfer (In a real system, this is passed via a custom header/argument resolver from the Gateway).
     * @return TransferResponse indicating success or failure status.
     */
    @Operation(summary = "Initiate a money transfer to a recipient's account")
    @PostMapping("/transfers")
    // NOTE: Replace @RequestHeader with a proper argument resolver based on your Security/Gateway setup later
    public ResponseEntity<TransferResponse> transferMoney(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(name = "X-User-Id", required = true) Long senderId) { 
        
        // The service acts as the orchestrator to call User Service for balance updates.
        TransferResponse response = paymentService.processTransfer(senderId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves the transaction history for the authenticated user (sender or recipient).
     * The service will enrich the data (add recipient/sender name) before returning.
     * @param userId The ID of the user whose history is requested (via JWT).
     * @param pageable Pagination and sorting criteria.
     * @return Paginated list of transactions with enriched data.
     */
    @Operation(summary = "View transaction history with pagination")
    @GetMapping("/transfers/history")
    public ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(
            @RequestHeader(name = "X-User-Id", required = true) Long userId,
            @PageableDefault(size = 10, sort = "transactionTime", direction = Sort.Direction.DESC) Pageable pageable) {

        // The service retrieves history and performs data enrichment (calling User Service for names).
        Page<TransactionHistoryResponse> history = paymentService.getHistory(userId, pageable);
        return ResponseEntity.ok(history);
    }
}