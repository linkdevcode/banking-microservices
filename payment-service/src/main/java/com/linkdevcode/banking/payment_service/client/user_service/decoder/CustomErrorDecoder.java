package com.linkdevcode.banking.payment_service.client.user_service.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linkdevcode.banking.payment_service.model.response.ErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream bodyIs = response.body().asInputStream()) {
            
            ErrorResponse errorResponse = objectMapper.readValue(bodyIs, ErrorResponse.class);
            return new RuntimeException(errorResponse.getMessage());
            
        } catch (IOException e) {
            return new RuntimeException("Failed to decode error response", e);
        }
    }
}