package com.linkdevcode.banking.batch_service.model.response;

import com.linkdevcode.banking.batch_service.entity.BatchTemp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchValidationResponse {
    
    private String batchTempId;
    private int totalRecords;
    private int validRecords;
    private int invalidRecords;

    public static BatchValidationResponse from(BatchTemp temp){
        return new BatchValidationResponse(
            temp.getId(),
            temp.getTotalRecords(),
            temp.getValidRecords(),
            temp.getInvalidRecords()
        );
    }
}
