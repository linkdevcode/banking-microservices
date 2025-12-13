package com.linkdevcode.banking.payment_service;

import com.linkdevcode.banking.payment_service.client.user_service.UserClient;
import com.linkdevcode.banking.payment_service.client.user_service.request.BalanceUpdateRequest;
import com.linkdevcode.banking.payment_service.client.user_service.response.UserLookupResponse;
import com.linkdevcode.banking.payment_service.entity.Transaction;
import com.linkdevcode.banking.payment_service.model.request.TransferRequest;
import com.linkdevcode.banking.payment_service.model.response.TransactionHistoryResponse;
import com.linkdevcode.banking.payment_service.model.response.TransferResponse;
import com.linkdevcode.banking.payment_service.repository.TransactionRepository;
import com.linkdevcode.banking.payment_service.service.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the PaymentService class, reflecting the 'processTransfer' and 'getHistory' methods.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    // Mock external dependencies and repositories
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserClient userClient;

    // Global Test Data
    private final Long SENDER_ID = 100L;
    private final Long RECIPIENT_ID = 200L;
    private final BigDecimal TRANSFER_AMOUNT = new BigDecimal("500.00");
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        transferRequest = new TransferRequest();
        transferRequest.setRecipientId(RECIPIENT_ID);
        transferRequest.setAmount(TRANSFER_AMOUNT);
        transferRequest.setMessage("Test transfer");
    }

    // =========================================================================
    //                            1. processTransfer Tests
    // =========================================================================

    @Test
    void processTransfer_SuccessfulFlow_ShouldCompleteAndReturnSuccess() {
        // ARRANGE: Set up mock behavior for the 5 steps
        
        // 1. Initial save (PENDING)
        Transaction initialTransaction = new Transaction();
        initialTransaction.setSenderId(SENDER_ID);
        initialTransaction.setRecipientId(RECIPIENT_ID);
        initialTransaction.setAmount(TRANSFER_AMOUNT);
        initialTransaction.setStatus("PENDING");
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(initialTransaction); // Return the transaction object

        // 2. Deduct: Assume User Service returns 200 OK on successful deduction
        when(userClient.deductBalance(eq(SENDER_ID), any(BalanceUpdateRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        // 3. Add: Assume User Service returns 200 OK on successful addition
        when(userClient.addBalance(eq(RECIPIENT_ID), any(BalanceUpdateRequest.class)))
                .thenReturn(ResponseEntity.ok().build());
        
        // ACT
        TransferResponse response = paymentService.processTransfer(SENDER_ID, transferRequest);

        // ASSERT
        // 1. Verify the core distributed steps were called
        verify(userClient, times(1)).deductBalance(eq(SENDER_ID), any(BalanceUpdateRequest.class));
        verify(userClient, times(1)).addBalance(eq(RECIPIENT_ID), any(BalanceUpdateRequest.class));
        
        // 2. Verify the transaction status was saved twice (PENDING and SUCCESS)
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        
        // 3. Verify final response status
        assertEquals("SUCCESS", response.getStatus());
        // Verify the status was updated to SUCCESS on the saved object
        assertEquals("SUCCESS", initialTransaction.getStatus()); 
    }

    @Test
    void processTransfer_DeductBalanceFails_ShouldSaveFailedStatusAndThrowResponseStatusException() {
        // ARRANGE
        Transaction initialTransaction = new Transaction();
        initialTransaction.setSenderId(SENDER_ID);
        initialTransaction.setRecipientId(RECIPIENT_ID);
        initialTransaction.setAmount(TRANSFER_AMOUNT);
        initialTransaction.setStatus("PENDING");
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(initialTransaction);
        
        // 2. Simulate insufficient funds (HTTP 400 Bad Request) from deductBalance
        HttpClientErrorException mockException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        when(userClient.deductBalance(eq(SENDER_ID), any(BalanceUpdateRequest.class)))
                .thenThrow(mockException);

        // ACT & ASSERT
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
            paymentService.processTransfer(SENDER_ID, transferRequest));

        // 1. Verify exception details
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        
        // 2. Verify addBalance was NOT called
        verify(userClient, never()).addBalance(anyLong(), any(BalanceUpdateRequest.class));
        
        // 3. Verify transaction was saved with FAILED status
        // (Once for PENDING, once for FAILED)
        verify(transactionRepository, times(2)).save(any(Transaction.class)); 
        assertEquals("FAILED", initialTransaction.getStatus());
    }

    @Test
    void processTransfer_TransferToSelf_ShouldThrowIllegalArgumentException() {
        // ARRANGE
        transferRequest.setRecipientId(SENDER_ID);
        Transaction initialTransaction = new Transaction();
        initialTransaction.setSenderId(SENDER_ID);
        initialTransaction.setRecipientId(RECIPIENT_ID);
        initialTransaction.setAmount(TRANSFER_AMOUNT);
        initialTransaction.setStatus("PENDING");
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(initialTransaction);

        // ACT & ASSERT
        assertThrows(ResponseStatusException.class, () -> 
            paymentService.processTransfer(SENDER_ID, transferRequest));

        // Verify deduct/add were NOT called
        verify(userClient, never()).deductBalance(anyLong(), any(BalanceUpdateRequest.class));
        verify(userClient, never()).addBalance(anyLong(), any(BalanceUpdateRequest.class));
        
        // Verify transaction was marked FAILED
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        assertEquals("FAILED", initialTransaction.getStatus());
    }

    // =========================================================================
    //                            2. getHistory Tests
    // =========================================================================

    @Test
    void getHistory_ShouldReturnPaginatedHistoryWithEnrichedNames() {
        // ARRANGE
        final int PAGE_SIZE = 2;
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("timestamp").descending());

        // Sample Transactions
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setSenderId(SENDER_ID);
        t1.setRecipientId(RECIPIENT_ID);
        t1.setAmount(TRANSFER_AMOUNT);
        t1.setStatus("SUCCESS");
        Transaction t2 = new Transaction();
        t2.setId(2L);
        t2.setSenderId(300L);
        t2.setRecipientId(SENDER_ID);
        t2.setAmount(new BigDecimal("100.00"));
        t2.setStatus("SUCCESS");
        List<Transaction> transactionList = List.of(t1, t2);
        
        // Mock Repository result
        Page<Transaction> transactionPage = new PageImpl<>(transactionList, pageable, 10);
        when(transactionRepository.findBySenderIdOrRecipientId(eq(SENDER_ID), eq(SENDER_ID), eq(pageable)))
                .thenReturn(transactionPage);

        // Mock User Client Lookup (Required IDs: 100, 200, 300)
        UserLookupResponse senderProfile = new UserLookupResponse(SENDER_ID, "Alice Smith", null);
        UserLookupResponse recipientProfile = new UserLookupResponse(RECIPIENT_ID, "Bob Johnson", null);
        UserLookupResponse thirdPartyProfile = new UserLookupResponse(300L, "Charlie Brown", null);
        
        when(userClient.getUserProfileForInternal(SENDER_ID))
            .thenReturn(ResponseEntity.ok(senderProfile));
        when(userClient.getUserProfileForInternal(RECIPIENT_ID))
            .thenReturn(ResponseEntity.ok(recipientProfile));
        when(userClient.getUserProfileForInternal(300L))
            .thenReturn(ResponseEntity.ok(thirdPartyProfile));

        // ACT
        Page<TransactionHistoryResponse> resultPage = paymentService.getHistory(SENDER_ID, pageable);

        // ASSERT
        // 1. Verify lookup calls (NOTE: Loop means it calls for all unique IDs)
        verify(userClient, times(1)).getUserProfileForInternal(SENDER_ID);
        verify(userClient, times(1)).getUserProfileForInternal(RECIPIENT_ID);
        verify(userClient, times(1)).getUserProfileForInternal(300L);
        
        // 2. Verify mapping and enrichment
        assertEquals(2, resultPage.getContent().size());
        
        TransactionHistoryResponse dto1 = resultPage.getContent().stream()
            .filter(dto -> dto.getTransactionId().equals(1L)).collect(Collectors.toList()).get(0);
        assertEquals("Alice Smith", dto1.getSenderName());
        assertEquals("Bob Johnson", dto1.getRecipientName());

        TransactionHistoryResponse dto2 = resultPage.getContent().stream()
            .filter(dto -> dto.getTransactionId().equals(2L)).collect(Collectors.toList()).get(0);
        assertEquals("Charlie Brown", dto2.getSenderName());
        assertEquals("Alice Smith", dto2.getRecipientName());
    }
}