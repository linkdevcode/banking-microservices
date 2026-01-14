package com.linkdevcode.banking.payment_service.client.user_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.linkdevcode.banking.payment_service.client.user_service.request.BalanceUpdateRequest;
import com.linkdevcode.banking.payment_service.client.user_service.request.AccountTransferRequest;
import com.linkdevcode.banking.payment_service.client.user_service.response.AccountResolveResponse;
import com.linkdevcode.banking.payment_service.config.FeignConfig;

/**
 * Feign Client for synchronous communication with the User Service.
 */
@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserClient {

    /**
     * API to resolve account details by account number.
     * Maps to: GET /api/accounts/{accountNumber}
     * @param accountNumber The account number to resolve.
     * @return ResponseEntity containing account details.
     */
    @GetMapping("/api/account/{accountNumber}")
    AccountResolveResponse resolve(
        @PathVariable("accountNumber") String accountNumber
    );

    /**
     * API to deduct an amount from a user's account balance.
     * Maps to: POST /api/account/dispense
     * @param request DTO containing the user ID and amount to deduct.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/api/account/dispense")
    ResponseEntity<Void> dispense(@RequestBody BalanceUpdateRequest request);

    /**
     * API to add an amount to a user's account balance.
     * Maps to: POST /api/account/deposit
     * @param request DTO containing the user ID and amount to add.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/api/account/deposit")
    ResponseEntity<Void> deposit(@RequestBody BalanceUpdateRequest request);

    /**
     * API to transfer an amount between two user accounts.
     * Maps to: POST /api/account/transfer
     * @param request DTO containing source and destination account details and amount.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/api/account/transfer")
    ResponseEntity<Void> transfer(@RequestBody AccountTransferRequest request);
}