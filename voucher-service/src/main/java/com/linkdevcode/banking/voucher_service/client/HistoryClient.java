package com.linkdevcode.banking.voucher_service.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.linkdevcode.banking.voucher_service.client.response.TopUserStatistic;

@FeignClient(name = "history-service")
public interface HistoryClient {
    
    @GetMapping("/api/internal/history/top-users")
    List<TopUserStatistic> getTopUsers(
        @RequestParam LocalDate fromDate,
        @RequestParam LocalDate toDate,
        @RequestParam int limit
    );
}
