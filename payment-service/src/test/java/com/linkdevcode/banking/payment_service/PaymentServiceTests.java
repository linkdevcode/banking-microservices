package com.linkdevcode.banking.payment_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.linkdevcode.banking.payment_service.client.user_service.UserClient;
import com.linkdevcode.banking.payment_service.client.user_service.request.BalanceUpdateRequest;
import com.linkdevcode.banking.payment_service.entity.Transaction;
import com.linkdevcode.banking.payment_service.model.request.TransferRequest;
import com.linkdevcode.banking.payment_service.repository.TransactionRepository;
import com.linkdevcode.banking.payment_service.service.PaymentService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the PaymentService class, focusing on transfer and history logic.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    // Inject the mocks into the PaymentService instance
    @InjectMocks
    private PaymentService paymentService;

    // Mock external dependencies
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserClient userClient;

    // Global Test Data
    private final Long SENDER_ID = 100L;
    private final Long RECIPIENT_ID = 200L;
    private final BigDecimal TRANSFER_AMOUNT = new BigDecimal("500.00");
    private final BigDecimal SENDER_INITIAL_BALANCE = new BigDecimal("1000.00");
    
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        transferRequest = new TransferRequest(SENDER_ID, RECIPIENT_ID, TRANSFER_AMOUNT, "Payment for services");
    }

    // =========================================================================
    //                            1. Transfer Creation Tests
    // =========================================================================

    @Test
    void createTransfer_SuccessfulFlow_ShouldCompleteAndSaveTransaction() {
        // ARRANGE: Set up mock behavior
        
        // 1. Balance Check: Assume sender has 1000.00 (sufficient)
        when(userClient.getBalance(SENDER_ID))
                .thenReturn(ResponseEntity.ok(SENDER_INITIAL_BALANCE));

        // 2. Deduct: Assume User Service returns 200 OK on successful deduction
        when(userClient.deductBalance(eq(SENDER_ID), any(BalanceUpdateRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        // 3. Add: Assume User Service returns 200 OK on successful addition
        when(userClient.addBalance(eq(RECIPIENT_ID), any(BalanceUpdateRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        // 4. Save Transaction: Return the object passed to save
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT: Execute the method under test
        Transaction result = paymentService.createTransfer(transferRequest);

        // ASSERT: Verify the results
        
        // 1. Verify all external API calls were made exactly once
        verify(userClient, times(1)).getBalance(SENDER_ID);
        verify(userClient, times(1)).deductBalance(eq(SENDER_ID), any(BalanceUpdateDto.class));
        verify(userClient, times(1)).addBalance(eq(RECIPIENT_ID), any(BalanceUpdateDto.class));
        
        // 2. Verify the transaction was saved
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        
        // 3. Verify the final Transaction status and data
        assertEquals(SENDER_ID, result.getSenderId());
        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void createTransfer_InsufficientFunds_ShouldThrowExceptionAndNotCallDeduct() {
        // ARRANGE: Set up mock behavior
        
        // 1. Balance Check: Assume sender only has 400.00 (insufficient for 500.00)
        BigDecimal insufficientBalance = new BigDecimal("400.00");
        when(userClient.getBalance(SENDER_ID))
                .thenReturn(ResponseEntity.ok(insufficientBalance));

        // ACT & ASSERT: Execute and check for the expected exception
        assertThrows(InsufficientFundsException.class, () -> 
            paymentService.createTransfer(transferRequest));

        // 1. Verify that deduction and addition were NEVER called
        verify(userClient, never()).deductBalance(anyLong(), any(BalanceUpdateDto.class));
        verify(userClient, never()).addBalance(anyLong(), any(BalanceUpdateDto.class));
        
        // 2. Verify no COMPLETED transaction was saved
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    // =========================================================================
    //                            2. History Retrieval Tests
    // =========================================================================

    @Test
    void getTransferHistory_ShouldReturnTransactionsForUser() {
        // ARRANGE: Set up sample history data
        Transaction t1 = new Transaction();
        t1.setSenderId(SENDER_ID);
        t1.setAmount(new BigDecimal("100.00"));
        
        Transaction t2 = new Transaction();
        t2.setRecipientId(SENDER_ID);
        t2.setAmount(new BigDecimal("200.00"));
        
        List<Transaction> expectedHistory = Arrays.asList(t1, t2);

        // 1. Mock Repository to return the list
        when(transactionRepository.findBySenderIdOrRecipientIdOrderByTimestampDesc(SENDER_ID, SENDER_ID))
                .thenReturn(expectedHistory);

        // ACT: Execute the method
        List<Transaction> actualHistory = paymentService.getTransferHistory(SENDER_ID);

        // ASSERT: Verify the results
        
        // 1. Verify repository was called once with the correct parameters
        verify(transactionRepository, times(1))
            .findBySenderIdOrRecipientIdOrderByTimestampDesc(SENDER_ID, SENDER_ID);
            
        // 2. Verify the returned list matches the expected data
        assertNotNull(actualHistory);
        assertEquals(2, actualHistory.size());
        assertEquals(expectedHistory, actualHistory);
    }
    
    @Test
    void getTransferHistory_NoTransactions_ShouldReturnEmptyList() {
        // ARRANGE: Mock Repository to return an empty list
        when(transactionRepository.findBySenderIdOrRecipientIdOrderByTimestampDesc(SENDER_ID, SENDER_ID))
                .thenReturn(List.of());

        // ACT: Execute the method
        List<Transaction> actualHistory = paymentService.getTransferHistory(SENDER_ID);

        // ASSERT: Verify an empty list is returned
        assertNotNull(actualHistory);
        assertTrue(actualHistory.isEmpty());
    }
}