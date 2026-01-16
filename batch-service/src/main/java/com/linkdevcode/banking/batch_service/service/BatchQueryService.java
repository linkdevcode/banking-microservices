package com.linkdevcode.banking.batch_service.service;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.linkdevcode.banking.batch_service.entity.BatchItem;
import com.linkdevcode.banking.batch_service.entity.BatchJob;
import com.linkdevcode.banking.batch_service.entity.BatchTempItem;
import com.linkdevcode.banking.batch_service.enumeration.EBatchItemStatus;
import com.linkdevcode.banking.batch_service.model.response.BatchStatusResponse;
import com.linkdevcode.banking.batch_service.repository.BatchItemRepository;
import com.linkdevcode.banking.batch_service.repository.BatchJobRepository;
import com.linkdevcode.banking.batch_service.repository.BatchTempItemRepository;
import com.linkdevcode.banking.batch_service.repository.BatchTempRepository;
import com.linkdevcode.banking.batch_service.service.helper.CsvDataHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchQueryService {
    
    private final BatchJobRepository batchJobRepository;
    private final BatchItemRepository batchItemRepository;
    private final BatchTempItemRepository batchTempItemRepository;
    private final BatchTempRepository batchTempRepository;
    private final CsvDataHelper csvDataHelper;
    
    public BatchStatusResponse getStatus(Long batchId){

        BatchJob batchJob = batchJobRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("BatchJob not found"));

        return new BatchStatusResponse(
            batchJob.getId(),
            batchJob.getStatus(),
            batchJob.getTotalItems(),
            batchJob.getSuccessItems(),
            batchJob.getFailedItems(),
            batchJob.getStartedAt(),
            batchJob.getFinishedAt()
        );
    }

    public ResponseEntity<Resource> downloadValidationErrors(String batchTempId) {

        batchTempRepository.findById(batchTempId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Batch temp not found"
            ));

        List<BatchTempItem> invalidItems =
            batchTempItemRepository.findByBatchTempIdAndValidFalse(batchTempId);

        if (invalidItems.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No validation errors"
            );
        }

        ByteArrayResource resource = csvDataHelper.buildValidationErrorCsv(invalidItems);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=batch_validate_" + batchTempId + "_errors.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(resource);
    }

    public ResponseEntity<Resource> downloadExecutionErrors(Long batchJobJd) {

        batchJobRepository.findById(batchJobJd)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Batch job not found"
            ));

        List<BatchItem> invalidItems =
            batchItemRepository.findByBatchJobIdAndStatus(batchJobJd, EBatchItemStatus.FAILED);

        if (invalidItems.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No execution errors"
            );
        }

        ByteArrayResource resource = csvDataHelper.buildExecutionErrorCsv(invalidItems);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=batch_execute_" + batchJobJd + "_errors.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(resource);
    }
}
