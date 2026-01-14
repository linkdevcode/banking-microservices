package com.linkdevcode.banking.history_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.history_service.model.request.AdminHistorySearchRequest;
import com.linkdevcode.banking.history_service.model.response.AdminHistorySearchResponse;
import com.linkdevcode.banking.history_service.service.TransactionHistoryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/history")
@RequiredArgsConstructor
@Tag(name = "Admin Transaction History", description = "APIs for admins to view all users' transaction history.")
public class AdminTransactionHistoryController {
    
    private final TransactionHistoryService transactionHistoryService;
    
    // Search transactions for admin
    @PostMapping("/search")
    public Page<AdminHistorySearchResponse> searchAllHistory(
        @RequestBody AdminHistorySearchRequest request
    ){
        return transactionHistoryService.searchForAdmin(request);
    }
}
