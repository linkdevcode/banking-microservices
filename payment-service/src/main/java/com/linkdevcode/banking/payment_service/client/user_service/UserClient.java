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
 * The 'name' attribute must match the service ID registered in Eureka (user-service).
 */
@FeignClient(name = "user-service") // Tên của service trong Eureka
public interface UserClient {

    /**
     * Internal API to get the current account balance of a user.
     * Maps to: GET /internal/users/{userId}/balance
     * @param userId The ID of the user.
     * @return ResponseEntity containing the current balance (BigDecimal).
     */
    @GetMapping("/internal/users/{userId}/balance")
    ResponseEntity<BigDecimal> getBalance(@PathVariable("userId") Long userId);

    /**
     * Internal API to deduct an amount from a user's account balance.
     * Maps to: POST /internal/users/{userId}/deduct
     * @param userId The ID of the user (sender).
     * @param request DTO containing the amount to deduct.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/internal/users/{userId}/deduct")
    ResponseEntity<Void> deductBalance(@PathVariable("userId") Long userId, 
                                        @RequestBody BalanceUpdateRequest request);

    /**
     * Internal API to add an amount to a user's account balance.
     * Maps to: POST /internal/users/{userId}/add
     * @param userId The ID of the user (recipient).
     * @param request DTO containing the amount to add.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    @PostMapping("/internal/users/{userId}/add")
    ResponseEntity<Void> addBalance(@PathVariable("userId") Long userId, 
                                    @RequestBody BalanceUpdateRequest request);
    
    // Future API for data enrichment (Flow 3)
    /**
     * Internal API to fetch user profile details (e.g., full name) for data enrichment.
     * Maps to: GET /api/user/profile (or similar internal lookup)
     * NOTE: We assume User Service provides a simple internal lookup DTO here.
     */
    @GetMapping("/api/user/{userId}/profile/internal") // Assuming a new internal lookup endpoint
    ResponseEntity<UserLookupResponse> getUserProfileForInternal(@PathVariable("userId") Long userId);
}