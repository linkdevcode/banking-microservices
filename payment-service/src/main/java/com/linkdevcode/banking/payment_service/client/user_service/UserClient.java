package com.linkdevcode.banking.payment_service.client.user_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.linkdevcode.banking.payment_service.client.user_service.request.BalanceUpdateRequest;
import com.linkdevcode.banking.payment_service.client.user_service.request.GetBalanceRequest;
import java.math.BigDecimal;

/**
 * Feign Client for synchronous communication with the User Service.
 */
@FeignClient(name = "user-service")
public interface UserClient {
    /**
     * API to get the balance of a user's account.
     * Maps to: GET /api/accounts/get-balance
     * @param request DTO containing the user ID.
     * @return ResponseEntity<BigDecimal> with the user's balance.
     */
    @GetMapping("/api/accounts/get-balance")
    ResponseEntity<BigDecimal> getBalance(@RequestBody GetBalanceRequest request);

    /**
     * API to deduct an amount from a user's account balance.
     * Maps to: POST /api/accounts/dispense
     * @param request DTO containing the user ID and amount to deduct.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/api/accounts/dispense")
    ResponseEntity<Void> dispense(@RequestBody BalanceUpdateRequest request);

    /**
     * API to add an amount to a user's account balance.
     * Maps to: POST /api/accounts/deposit
     * @param request DTO containing the user ID and amount to add.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/api/accounts/deposit")
    ResponseEntity<Void> deposit(@RequestBody BalanceUpdateRequest request);
}