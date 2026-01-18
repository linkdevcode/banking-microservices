package com.linkdevcode.banking.voucher_service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.linkdevcode.banking.voucher_service.client.request.GetTopUserRequest;
import com.linkdevcode.banking.voucher_service.client.response.TopUserStatistic;

@FeignClient(name = "history-service")
public interface HistoryClient {
    
    @PostMapping("/api/internal/history/top-users")
    List<TopUserStatistic> getTopUsers(
        @RequestBody GetTopUserRequest getTopUserRequest
    );
}
