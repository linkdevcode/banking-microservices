package com.linkdevcode.banking.history_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.history_service.model.request.UserHistorySearchRequest;
import com.linkdevcode.banking.history_service.model.response.UserHistorySearchResponse;
import com.linkdevcode.banking.history_service.service.TransactionHistoryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "User Transaction History", description = "APIs for users to view their transaction history.")
public class UserTransactionHistoryController {
    
    private final TransactionHistoryService transactionHistoryService;

    // Search transaction history for user
    @PostMapping("/search")
    public Page<UserHistorySearchResponse> searchMyHistory(
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody UserHistorySearchRequest request
    ){
        return transactionHistoryService.searchForUser(userId, request);
    }
}
