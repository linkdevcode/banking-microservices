package com.linkdevcode.banking.batch_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BatchConfirmRequest {
    @NotBlank
    private String batchTempId;

    private boolean executeValidOnly;
}
