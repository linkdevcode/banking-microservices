package com.linkdevcode.banking.user_service.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.user_service.model.request.BalanceUpdateRequest;
import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get account balance")
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id) {

        return ResponseEntity.ok(userService.getBalance(id));
    }

    @Operation(summary = "Add balance (internal)")
    @PostMapping("/{id}/balance/add")
    public ResponseEntity<Void> addBalance(
            @PathVariable Long id,
            @RequestBody BalanceUpdateRequest request) {

        userService.addBalance(id, request.getAmount());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deduct balance (internal)")
    @PostMapping("/{id}/balance/deduct")
    public ResponseEntity<Void> deductBalance(
            @PathVariable Long id,
            @RequestBody BalanceUpdateRequest request) {

        userService.deductBalance(id, request.getAmount());
        return ResponseEntity.ok().build();
    }
}