package com.linkdevcode.banking.user_service.model.request;

import com.linkdevcode.banking.user_service.enumeration.EUserStatus;

public record UserSearchRequest(
        String keyword,      
        EUserStatus status,
        int page,
        int size,
        String sortBy,
        String direction
) {}