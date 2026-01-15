package com.linkdevcode.banking.batch_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.linkdevcode.banking.batch_service.client.request.TransferRequest;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/internal/payment/transfer")
    void transfer(@RequestBody TransferRequest request);
}
