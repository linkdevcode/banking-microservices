package com.linkdevcode.banking.payment_service.client.user_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.linkdevcode.banking.payment_service.client.user_service.request.BalanceUpdateRequest;
import com.linkdevcode.banking.payment_service.client.user_service.response.UserLookupResponse;

import java.math.BigDecimal;

/**
 * Feign Client for synchronous communication with the User Service.
 */
@FeignClient(name = "user-service") // Tên của service trong Eureka
public interface UserClient {

    /**
     * API to get the current account balance of a user.
     * Maps to: GET /api/user/{id}/balance
     * @param userId The ID of the user.
     * @return ResponseEntity containing the current balance (BigDecimal).
     */
    @GetMapping("/api/accounts/{id}/balance")
    ResponseEntity<BigDecimal> getBalance(@PathVariable("id") Long id);

    /**
     * API to deduct an amount from a user's account balance.
     * Maps to: POST /api/user/{id}/balance/deduct
     * @param userId The ID of the user (sender).
     * @param request DTO containing the amount to deduct.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/api/accounts/{id}/balance/deduct")
    ResponseEntity<Void> deductBalance(@PathVariable("id") Long id, 
                                        @RequestBody BalanceUpdateRequest request);

    /**
     * API to add an amount to a user's account balance.
     * Maps to: POST /api/user/{id}/balance/add
     * @param userId The ID of the user (recipient).
     * @param request DTO containing the amount to add.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/api/accounts/{id}/balance/add")
    ResponseEntity<Void> addBalance(@PathVariable("id") Long id, 
                                    @RequestBody BalanceUpdateRequest request);
    
    /**
     * API to fetch user profile details (e.g., full name) for data enrichment.
     * Maps to: GET /api/user/{id}/profile (or similar internal lookup)
     */
    @GetMapping("/internal/user/{id}/profile")
    ResponseEntity<UserLookupResponse> getUserProfileForInternal(@PathVariable("userId") Long userId);
}