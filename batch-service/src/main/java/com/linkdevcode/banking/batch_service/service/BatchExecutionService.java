package com.linkdevcode.banking.batch_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.linkdevcode.banking.batch_service.client.PaymentClient;
import com.linkdevcode.banking.batch_service.client.request.TransferRequest;
import com.linkdevcode.banking.batch_service.entity.BatchItem;
import com.linkdevcode.banking.batch_service.entity.BatchJob;
import com.linkdevcode.banking.batch_service.entity.BatchTempItem;
import com.linkdevcode.banking.batch_service.enumeration.EBatchItemStatus;
import com.linkdevcode.banking.batch_service.enumeration.EBatchJobStatus;
import com.linkdevcode.banking.batch_service.repository.BatchItemRepository;
import com.linkdevcode.banking.batch_service.repository.BatchJobRepository;
import com.linkdevcode.banking.batch_service.repository.BatchTempItemRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchExecutionService {
    
    private final BatchTempItemRepository tempItemRepository;
    private final BatchItemRepository batchItemRepository;
    private final BatchJobRepository batchJobRepository;
    private final PaymentClient paymentClient;

    @Transactional
    public void execute (BatchJob batchJob, String batchTempId){

        List<BatchTempItem> validBatchTempItems = tempItemRepository.findByBatchTempIdAndValidTrue(batchTempId);

        int success = 0;
        int failed = 0;

        for (BatchTempItem batchTempItem : validBatchTempItems) {

            BatchItem batchItem = new BatchItem();
            batchItem.setBatchJobId(batchJob.getId());
            batchItem.setFromAccountNumber(batchTempItem.getFromAccountNumber());
            batchItem.setToAccountNumber(batchTempItem.getToAccountNumber());
            batchItem.setAmount(batchTempItem.getAmount());

            try {
                paymentClient.transfer(
                    new TransferRequest(
                        batchTempItem.getFromAccountNumber(),
                        batchTempItem.getToAccountNumber(),
                        batchTempItem.getAmount(),
                        batchTempItem.getTransferMessage()
                    )
                );
                batchItem.setStatus(EBatchItemStatus.SUCCESS);
                success++;

            } catch (Exception e) {
                batchItem.setStatus(EBatchItemStatus.FAILED);
                batchItem.setErrorMessage(e.getMessage());
                failed++;
            }

            batchItemRepository.save(batchItem);
        }

        batchJob.setSuccessItems(success);
        batchJob.setFailedItems(failed);
        batchJob.setFinishedAt(LocalDateTime.now());

        if (failed == 0) {
            batchJob.setStatus(EBatchJobStatus.COMPLETED);
        } else if (success > 0) {
            batchJob.setStatus(EBatchJobStatus.PARTIAL_FAILED);
        } else {
            batchJob.setStatus(EBatchJobStatus.FAILED);
        }

        batchJobRepository.save(batchJob);
    }
}
