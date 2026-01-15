package com.linkdevcode.banking.batch_service.service;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.linkdevcode.banking.batch_service.entity.BatchTemp;
import com.linkdevcode.banking.batch_service.entity.BatchTempItem;
import com.linkdevcode.banking.batch_service.model.BatchCsvRecord;
import com.linkdevcode.banking.batch_service.model.response.BatchValidationResponse;
import com.linkdevcode.banking.batch_service.repository.BatchTempItemRepository;
import com.linkdevcode.banking.batch_service.repository.BatchTempRepository;
import com.opencsv.bean.CsvToBeanBuilder;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchValidationService {
    
    private final BatchTempRepository batchTempRepository;
    private final BatchTempItemRepository batchTempItemRepository;

    @Transactional
    public BatchValidationResponse validate(
        Long currentUserId, MultipartFile file
    ){
        String tempId = UUID.randomUUID().toString();

        List<BatchCsvRecord> batchCsvRecords = parseCsv(file);
        
        int totalRecord = batchCsvRecords.size();
        int validRecord = 0;
        int invalidRecord = 0;

        List<BatchTempItem> batchTempItems = new ArrayList<>();
        String sourceAccount = batchCsvRecords.get(0).getFromAccountNumber();

        for (BatchCsvRecord record : batchCsvRecords) {

            BatchTempItem batchTempItem = new BatchTempItem();
            batchTempItem.setBatchTempId(tempId);
            batchTempItem.setFromAccountNumber(record.getFromAccountNumber());
            batchTempItem.setToAccountNumber(record.getToAccountNumber());
            batchTempItem.setAmount(record.getAmount());
            batchTempItem.setTransferMessage(record.getTransferMessage());

            try{
                validateRecord(sourceAccount, record);
                batchTempItem.setValid(true);
                validRecord++;
            } catch (Exception ex){
                batchTempItem.setValid(false);
                batchTempItem.setErrorMessage(ex.getMessage());
            }

            batchTempItems.add(batchTempItem);
        }

        BatchTemp batchTemp = new BatchTemp(
            tempId,
            currentUserId,
            totalRecord,
            validRecord,
            invalidRecord,
            LocalDateTime.now()
        );

        batchTempRepository.save(batchTemp);
        batchTempItemRepository.saveAll(batchTempItems);

        return BatchValidationResponse.from(batchTemp);
    }

    // Business validation logic
    private void validateRecord(String sourceAccount, BatchCsvRecord record) {

        if (!sourceAccount.equals(record.getFromAccountNumber())) {
            throw new IllegalArgumentException(
                "Multiple source accounts in one batch are not allowed");
        }

        if (record.getFromAccountNumber() == null ||
            record.getToAccountNumber() == null) {
            throw new IllegalArgumentException("Account number is required");
        }

        if (record.getFromAccountNumber().equals(record.getToAccountNumber())) {
            throw new IllegalArgumentException("Same source & destination account");
        }

        if (record.getAmount() == null ||
            record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
    }

    private List<BatchCsvRecord> parseCsv(MultipartFile file) {
        try {
            return new CsvToBeanBuilder<BatchCsvRecord>(
                    new InputStreamReader(file.getInputStream()))
                    .withType(BatchCsvRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid CSV format");
        }
    }
}
