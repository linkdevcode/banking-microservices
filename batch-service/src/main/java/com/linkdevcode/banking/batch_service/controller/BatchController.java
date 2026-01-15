package com.linkdevcode.banking.batch_service.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.linkdevcode.banking.batch_service.model.request.BatchConfirmRequest;
import com.linkdevcode.banking.batch_service.model.response.BatchConfirmResponse;
import com.linkdevcode.banking.batch_service.model.response.BatchValidationResponse;
import com.linkdevcode.banking.batch_service.service.BatchConfirmService;
import com.linkdevcode.banking.batch_service.service.BatchValidationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {
    
    private final BatchValidationService validationService;
    private final BatchConfirmService confirmService;
    
    @Operation(
        summary = "Validate batch CSV",
        description = "Upload CSV file to validate batch transfer records"
    )
    @PostMapping(
        value = "/validate",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BatchValidationResponse> validate(

        @Parameter(
            description = "User ID",
            required = true,
            in = ParameterIn.HEADER
        )
        @RequestHeader("X-User-Id") Long userId,

        @Parameter(
            description = "CSV file",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = @Schema(type = "string", format = "binary")
            )
        )
        @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(validationService.validate(userId, file));
    }


    @PostMapping("/confirm")
    public ResponseEntity<BatchConfirmResponse> confirm(
            @RequestBody @Valid BatchConfirmRequest request) {

        return ResponseEntity.ok(confirmService.confirm(request));
    }
}
