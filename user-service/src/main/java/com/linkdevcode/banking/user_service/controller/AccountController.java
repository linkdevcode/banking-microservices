package com.linkdevcode.banking.user_service.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.user_service.model.request.AccountTransferRequest;
import com.linkdevcode.banking.user_service.model.request.BalanceUpdateRequest;
import com.linkdevcode.banking.user_service.model.request.GetBalanceRequest;
import com.linkdevcode.banking.user_service.model.response.AccountResolveResponse;
import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Account")
public class AccountController {

    private final UserService userService;

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResolveResponse> resolveAccount(@PathVariable String accountNumber) {

        return ResponseEntity.ok(userService.resolveAccount(accountNumber));
    }

    @Operation(summary = "Get account balance")
    @GetMapping("/get-balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestBody GetBalanceRequest request) {

        return ResponseEntity.ok(userService.getBalance(request.getAccountNumber()));
    }

    @Operation(summary = "Add balance (internal)")
    @PostMapping("/deposit")
    public ResponseEntity<Void> addBalance(@RequestBody BalanceUpdateRequest request) {

        userService.deposit(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deduct balance (internal)")
    @PostMapping("/dispense")
    public ResponseEntity<Void> deductBalance(@RequestBody BalanceUpdateRequest request) {

        userService.dispense(request.getAccountNumber(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Transfer balance between accounts (internal)")
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBalance(@RequestBody AccountTransferRequest request) {

        userService.transfer(request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());
        return ResponseEntity.ok().build();
    }
}