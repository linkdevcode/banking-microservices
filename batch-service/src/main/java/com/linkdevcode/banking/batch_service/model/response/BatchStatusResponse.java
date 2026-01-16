package com.linkdevcode.banking.batch_service.model.response;

import java.time.LocalDateTime;

import com.linkdevcode.banking.batch_service.enumeration.EBatchJobStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchStatusResponse {
    private Long batchJobId;
    private EBatchJobStatus batchStatus;
    private int totalItems;
    private int successItems;
    private int failedItems;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
