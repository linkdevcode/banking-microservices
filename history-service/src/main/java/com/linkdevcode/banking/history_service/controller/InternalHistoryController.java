package com.linkdevcode.banking.history_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.history_service.model.request.GetTopUserRequest;
import com.linkdevcode.banking.history_service.model.response.TopUserStatistic;
import com.linkdevcode.banking.history_service.service.TransactionHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/history")
@RequiredArgsConstructor
public class InternalHistoryController {
    
    private final TransactionHistoryService transactionHistoryService;

    @Operation(summary = "Get top users by transfer amount")
    @PostMapping("/top-users")
    public ResponseEntity<List<TopUserStatistic>> getTopUsers(
        @RequestBody GetTopUserRequest getTopUserRequest
    ) {
        return ResponseEntity.ok(transactionHistoryService.getTopUsers(
            getTopUserRequest.getFromDate(), getTopUserRequest.getToDate(), getTopUserRequest.getLimit())
        );
    }
}
