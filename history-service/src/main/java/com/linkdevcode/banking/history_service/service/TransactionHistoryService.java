package com.linkdevcode.banking.history_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import com.linkdevcode.banking.history_service.entity.TransactionHistory;
import com.linkdevcode.banking.history_service.enumeration.ETransactionStatus;
import com.linkdevcode.banking.history_service.model.request.AdminHistorySearchRequest;
import com.linkdevcode.banking.history_service.model.request.UserHistorySearchRequest;
import com.linkdevcode.banking.history_service.model.response.AdminHistorySearchResponse;
import com.linkdevcode.banking.history_service.model.response.TopUserStatistic;
import com.linkdevcode.banking.history_service.model.response.UserHistorySearchResponse;
import com.linkdevcode.banking.history_service.repository.TransactionHistoryRepository;
import com.linkdevcode.banking.history_service.repository.projection.TopUserProjection;
import com.linkdevcode.banking.history_service.repository.specification.TransactionHistorySpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionHistoryService {
    
    private final TransactionHistoryRepository transactionRepository;

    // Search transactions for a specific user
    public Page<UserHistorySearchResponse> searchForUser(
        Long userId,
        UserHistorySearchRequest request
    ){
        Pageable pageable = buildPageable(request);

        Specification<TransactionHistory> spec = Specification
            .where(TransactionHistorySpecifications.hasUserId(userId))
            .and(TransactionHistorySpecifications.hasAccountNumber(request.getAccountNumber()))
            .and(TransactionHistorySpecifications.hasTransactionType(request.getTransactionType()))
            .and(TransactionHistorySpecifications.hasTransactionStatus(request.getTransactionStatus()))
            .and(TransactionHistorySpecifications.createdBetween(request.getFromDate(), request.getToDate()));
        
        return transactionRepository.findAll(spec, pageable)
                .map(this::mapToUserResponse);
    }

    // Search transactions for admin
    public Page<AdminHistorySearchResponse> searchForAdmin(
        AdminHistorySearchRequest request
    ){
        Pageable pageable = buildPageable(request);

        Specification<TransactionHistory> spec = Specification
            .where(TransactionHistorySpecifications.hasUserId(request.getUserId()))
            .and(TransactionHistorySpecifications.hasAccountNumber(request.getAccountNumber()))
            .and(TransactionHistorySpecifications.hasTransactionType(request.getTransactionType()))
            .and(TransactionHistorySpecifications.hasTransactionStatus(request.getTransactionStatus()))
            .and(TransactionHistorySpecifications.createdBetween(request.getFromDate(), request.getToDate()));
        
        return transactionRepository.findAll(spec, pageable)
                .map(this::mapToAdminResponse);
    }

    // Build pageable from request
    private Pageable buildPageable(Object request){
        String sortBy = request instanceof UserHistorySearchRequest r
            ? r.getSortBy()
            : ((AdminHistorySearchRequest) request).getSortBy();
        
        String direction = request instanceof UserHistorySearchRequest r
            ? r.getDirection()
            : ((AdminHistorySearchRequest) request).getDirection();

        int page = request instanceof UserHistorySearchRequest r
            ? r.getPage()
            : ((AdminHistorySearchRequest) request).getPage();
        
        int size = request instanceof UserHistorySearchRequest r
            ? r.getSize()
            : ((AdminHistorySearchRequest) request).getSize();
        
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return PageRequest.of(page, size, sort);
    }

    public List<TopUserStatistic> getTopUsers(
            LocalDate fromDate,
            LocalDate toDate,
            int limit
    ) {

        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay();

        List<TopUserProjection> result =
            transactionRepository.findTopUsers(
                ETransactionStatus.SUCCESS,
                from,
                to
            );

        return result.stream()
            .limit(limit)
            .map(p -> new TopUserStatistic(
                p.getUserId(),
                p.getTotalAmount()
            ))
            .collect(Collectors.toList());
    }

    // Map entity to user response
    private UserHistorySearchResponse mapToUserResponse(TransactionHistory tx) {
        return new UserHistorySearchResponse(
            tx.getTransactionId(),
            tx.getFromAccountNumber(),
            tx.getToAccountNumber(),
            tx.getAmount(),
            tx.getTransactionType(),
            tx.getTransactionStatus(),
            tx.getMessage(),
            tx.getTransactionTime()
        );
    }

    // Map entity to admin response
    private AdminHistorySearchResponse mapToAdminResponse(TransactionHistory tx) {
        return new AdminHistorySearchResponse(
            tx.getTransactionId(),
            tx.getFromUserId(),
            tx.getToUserId(),
            tx.getFromAccountNumber(),
            tx.getToAccountNumber(),
            tx.getAmount(),
            tx.getTransactionType(),
            tx.getTransactionStatus(),
            tx.getMessage(),
            tx.getTransactionTime(),
            tx.getRecordedAt()
        );
    }
}