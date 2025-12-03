package com.linkdevcode.banking.user_service.controller;

import com.linkdevcode.banking.user_service.model.request.BalanceUpdateRequest;
import com.linkdevcode.banking.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller for Internal APIs used exclusively by other Microservices.
 * These endpoints should NOT be exposed through the API Gateway.
 */
@RestController
@RequestMapping("/internal/users") // Distinct path to be blocked by Gateway
@Tag(name = "Internal User & Account Management", description = "APIs for inter-service communication (SHOULD NOT BE PUBLIC).")
@Hidden // Hide this controller from public Swagger documentation
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Internal API to get the current account balance of a user.
     * Used by Payment Service before transactions.
     * @param userId The ID of the user.
     * @return The account balance.
     */
    @Operation(summary = "Get user account balance (Internal)")
    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long userId) {
        BigDecimal balance = userService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Internal API to deduct an amount from a user's account balance.
     * Used by Payment Service for the sender's debit operation.
     * @param userId The ID of the user (sender).
     * @param request DTO containing the amount.
     * @return 200 OK on success.
     */
    @Operation(summary = "Deduct balance from user account (Internal)")
    @PostMapping("/{userId}/deduct")
    public ResponseEntity<Void> deductBalance(@PathVariable Long userId,
                                              @RequestBody BalanceUpdateRequest request) {
        userService.deductBalance(userId, request.getAmount());
        return ResponseEntity.ok().build();
    }

    /**
     * Internal API to add an amount to a user's account balance.
     * Used by Payment Service for the recipient's credit operation.
     * @param userId The ID of the user (recipient).
     * @param request DTO containing the amount.
     * @return 200 OK on success.
     */
    @Operation(summary = "Add balance to user account (Internal)")
    @PostMapping("/{userId}/add")
    public ResponseEntity<Void> addBalance(@PathVariable Long userId,
                                           @RequestBody BalanceUpdateRequest request) {
        userService.addBalance(userId, request.getAmount());
        return ResponseEntity.ok().build();
    }

    // A placeholder API for data enrichment (getting recipient's full name)
    /**
     * Internal API to fetch user profile details (e.g., full name) for enrichment.
     * @param userId The ID of the user to look up.
     * @return User details DTO (Placeholder).
     */
    @Operation(summary = "Get user profile details for enrichment (Internal)")
    @GetMapping("/{userId}/profile/internal")
    public ResponseEntity<?> getUserProfileForInternal(@PathVariable Long userId) {
        // Implementation will call userService to fetch basic user details (like full name)
        // For now, this is a placeholder. You'll need a simple UserProfileInternalDTO.
        return ResponseEntity.ok("Placeholder profile data for user: " + userId);
    }
}