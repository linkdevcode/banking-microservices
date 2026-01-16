package com.linkdevcode.banking.batch_service.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.linkdevcode.banking.batch_service.entity.BatchJob;
import com.linkdevcode.banking.batch_service.entity.BatchTemp;
import com.linkdevcode.banking.batch_service.enumeration.EBatchJobStatus;
import com.linkdevcode.banking.batch_service.model.request.BatchConfirmRequest;
import com.linkdevcode.banking.batch_service.model.response.BatchConfirmResponse;
import com.linkdevcode.banking.batch_service.model.response.BatchStatusResponse;
import com.linkdevcode.banking.batch_service.repository.BatchJobRepository;
import com.linkdevcode.banking.batch_service.repository.BatchTempRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchConfirmService {
    
    private final BatchTempRepository batchTempRepository;
    private final BatchJobRepository batchJobRepository;
    private final BatchExecutionService batchExecutionService;

    @Transactional
    public BatchConfirmResponse confirm(BatchConfirmRequest request) {

        BatchTemp temp = batchTempRepository.findById(request.getBatchTempId())
                .orElseThrow(() -> new IllegalArgumentException("BatchTemp not found"));

        if (temp.getInvalidRecords() > 0 && !request.isExecuteValidOnly()) {
            throw new IllegalStateException("Batch contains invalid records");
        }

        BatchJob job = new BatchJob();
        job.setBatchTempId(temp.getId());
        job.setTotalItems(temp.getValidRecords());
        job.setStatus(EBatchJobStatus.PROCESSING);
        job.setStartedAt(LocalDateTime.now());

        batchJobRepository.save(job);

        batchExecutionService.execute(job, temp.getId());

        return new BatchConfirmResponse(job.getId(), job.getStatus());
    }
}
