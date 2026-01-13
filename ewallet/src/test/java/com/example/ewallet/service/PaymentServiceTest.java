package com.example.ewallet.service;

import com.example.ewallet.entity.AutoPayData;
import com.example.ewallet.entity.PaymentData;
import com.example.ewallet.entity.QRData;
import com.example.ewallet.repository.AutoPayDataRepository;
import com.example.ewallet.repository.PaymentDataRepository;
import com.example.ewallet.repository.QRDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentDataRepository paymentDataRepository;

    @Mock
    private QRDataRepository qrDataRepository;

    @Mock
    private AutoPayDataRepository autoPayDataRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private PaymentService paymentService;

    // --- UT-02-001: Verify retail payment success logic ---
    @Test
    void testProcessPayment_Success() {
        // Pre-conditions: Wallet has balance (returns true)
        when(walletService.deductBalance(anyString(), anyDouble(), anyString())).thenReturn(true);

        // Test Steps: Call processPayment
        boolean result = paymentService.processPayment("user1", 50.0, "Tesco");

        // Expected Result: Method returns true
        assertTrue(result, "Payment should be successful");

        // Verify: Repository saves PaymentData with status "SUCCESS"
        ArgumentCaptor<PaymentData> captor = ArgumentCaptor.forClass(PaymentData.class);
        verify(paymentDataRepository).save(captor.capture());
        
        PaymentData savedData = captor.getValue();
        assertEquals("user1", savedData.getUserId());
        assertEquals(50.0, savedData.getAmount());
        assertEquals("Tesco", savedData.getRecipientId());
        assertEquals("SUCCESS", savedData.getStatus());
    }

    // --- UT-02-002: Verify retail payment failure ---
    @Test
    void testProcessPayment_Failure() {
        // Pre-conditions: Wallet insufficient balance (returns false)
        when(walletService.deductBalance(anyString(), anyDouble(), anyString())).thenReturn(false);

        // Test Steps: Call processPayment
        boolean result = paymentService.processPayment("user1", 5000.0, "Ferrari");

        // Expected Result: Method returns false
        assertFalse(result, "Payment should fail");

        // Verify: Repository saves PaymentData with status "FAILED"
        ArgumentCaptor<PaymentData> captor = ArgumentCaptor.forClass(PaymentData.class);
        verify(paymentDataRepository).save(captor.capture());

        PaymentData savedData = captor.getValue();
        assertEquals("Ferrari", savedData.getRecipientId());
        assertEquals("FAILED", savedData.getStatus());
    }

    // --- UT-02-003: Verify QR Code parsing and processing ---
    @Test
    void testProcessQRPayment_ValidAndInvalid() {
        // --- Scenario 1: Valid QR String ---
        // Pre-conditions
        when(walletService.deductBalance(anyString(), anyDouble(), anyString())).thenReturn(true);

        // Test Steps
        paymentService.processQRPayment("user1", "Starbucks:15.50");

        // Verify parsing logic & save
        ArgumentCaptor<QRData> qrCaptor = ArgumentCaptor.forClass(QRData.class);
        verify(qrDataRepository).save(qrCaptor.capture());

        QRData savedQR = qrCaptor.getValue();
        assertEquals("Starbucks", savedQR.getRecipientId());
        assertEquals(15.50, savedQR.getAmount());
        assertEquals("SUCCESS", savedQR.getStatus());

        // --- Scenario 2: Invalid QR String (delimited by dot instead of colon) ---
        // Reset mocks to clear previous interactions
        clearInvocations(qrDataRepository);

        // Test Steps
        paymentService.processQRPayment("user1", "Starbucks.15.50");

        // Verify: Repository should NOT save anything
        verify(qrDataRepository, never()).save(any(QRData.class));
    }

    // --- UT-02-004: Verify AutoPay Execution Simulation ---
    @Test
    void testSimulateAutoPayExecution() {
        // Pre-conditions: Active AutoPayData exists
        AutoPayData activeAutoPay = new AutoPayData("user1", "TNB", 100.0, 5);
        activeAutoPay.setStatus("Active");
        
        when(autoPayDataRepository.findByUserId("user1")).thenReturn(List.of(activeAutoPay));
        when(walletService.deductBalance(anyString(), anyDouble(), anyString())).thenReturn(true);

        // Test Steps
        paymentService.simulateAutoPayExecution("user1", "January");

        // Verify walletService.deductBalance is called
        verify(walletService).deductBalance(eq("user1"), eq(100.0), contains("AutoPay"));

        // Verify lastExecuted date is updated (AutoPayData is saved)
        ArgumentCaptor<AutoPayData> autoPayCaptor = ArgumentCaptor.forClass(AutoPayData.class);
        verify(autoPayDataRepository).save(autoPayCaptor.capture());
        
        AutoPayData updatedAutoPay = autoPayCaptor.getValue();
        assertNotNull(updatedAutoPay.getLastExecuted(), "lastExecuted should be updated to current timestamp");

        // Verify history log is created (PaymentData is saved)
        verify(paymentDataRepository).save(any(PaymentData.class));
    }
}