package com.linkdevcode.banking.batch_service.service.helper;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.linkdevcode.banking.batch_service.entity.BatchItem;
import com.linkdevcode.banking.batch_service.entity.BatchTempItem;
import com.linkdevcode.banking.batch_service.model.BatchCsvRecord;
import com.opencsv.bean.CsvToBeanBuilder;

@Component
public class CsvDataHelper {

    public List<BatchCsvRecord> parseCsv(MultipartFile file) {
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

    public ByteArrayResource buildValidationErrorCsv(List<BatchTempItem> items) {

        StringBuilder sb = new StringBuilder();
        sb.append("fromAccountNumber,toAccountNumber,amount,message,errorReason\n");

        for (BatchTempItem item : items) {
            sb.append(item.getFromAccountNumber()).append(",")
              .append(item.getToAccountNumber()).append(",")
              .append(item.getAmount()).append(",")
              .append(escape(item.getMessage())).append(",")
              .append(escape(item.getErrorReason()))
              .append("\n");
        }

        return new ByteArrayResource(
            sb.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    public ByteArrayResource buildExecutionErrorCsv(List<BatchItem> items) {

        StringBuilder sb = new StringBuilder();
        sb.append("fromAccountNumber,toAccountNumber,amount,message,errorReason\n");

        for (BatchItem item : items) {
            sb.append(item.getFromAccountNumber()).append(",")
              .append(item.getToAccountNumber()).append(",")
              .append(item.getAmount()).append(",")
              .append(escape(item.getMessage())).append(",")
              .append(escape(item.getErrorReason()))
              .append("\n");
        }

        return new ByteArrayResource(
            sb.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
