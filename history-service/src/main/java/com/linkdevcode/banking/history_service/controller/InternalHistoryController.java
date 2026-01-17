package com.linkdevcode.banking.history_service.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/top-users")
    public ResponseEntity<List<TopUserStatistic>> getTopUsers(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fromDate,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate toDate,

        @RequestParam(defaultValue = "10")
        int limit
    ) {
        return ResponseEntity.ok(
            transactionHistoryService.getTopUsers(fromDate, toDate, limit)
        );
    }
}
