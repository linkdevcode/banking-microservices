package com.linkdevcode.banking.batch_service.model.response;

import com.linkdevcode.banking.batch_service.enumeration.EBatchJobStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchConfirmResponse {
    private Long batchId;
    private EBatchJobStatus status;
}
