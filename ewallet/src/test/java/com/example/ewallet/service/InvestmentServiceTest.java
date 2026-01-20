package com.example.ewallet.service;

import com.example.ewallet.entity.*;
import com.example.ewallet.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock private FundRepository fundRepository;
    @Mock private InvestmentHistoryRepository investmentHistoryRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private PaymentService paymentService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private InvestmentService investmentService;

    private Fund sampleFund;

    @BeforeEach
    void setUp() {
        sampleFund = new Fund();
        sampleFund.setFundId("F01");
        sampleFund.setName("Low Risk Income Fund");
        sampleFund.setPrice(1.0000);
        sampleFund.setRiskCategory("Low");
    }

    // UT-05-001: Buy Fund Success
    @Test
    void testInvestInFund_Success() {
        when(fundRepository.findById("F01")).thenReturn(Optional.of(sampleFund));
        when(paymentService.processPayment(anyString(), anyString(), anyDouble(), anyString())).thenReturn(true);
        when(portfolioRepository.findByUserId("user1")).thenReturn(Optional.of(new Portfolio()));
        when(investmentHistoryRepository.save(any(InvestmentHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        InvestmentHistory result = investmentService.investInFund("0123", "user1", "F01", 100.0);

        assertEquals(100.0, result.getUnits());
        assertEquals("Success", result.getStatus());
        
        verify(investmentHistoryRepository).save(any(InvestmentHistory.class));
        verify(notificationService).notifyInvestmentMade(eq("user1"), anyString(), eq(100.0), eq(100.0));
    }

    // UT-05-002: Buy Fund Failed (Insufficient Balance)
    @Test
    void testInvestInFund_InsufficientBalance() {
        when(fundRepository.findById("F01")).thenReturn(Optional.of(sampleFund));
        when(paymentService.processPayment(anyString(), anyString(), anyDouble(), anyString())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> 
            investmentService.investInFund("0123", "user1", "F01", 1000.0)
        );

        assertEquals("Insufficient wallet balance.", exception.getMessage());
        verify(investmentHistoryRepository, never()).save(any());
    }

    // UT-05-003: Buy Fund System Error (Refund Path)
    @Test
    void testInvestInFund_SystemErrorRefund() {
        when(fundRepository.findById("F01")).thenReturn(Optional.of(sampleFund));
        when(paymentService.processPayment(anyString(), anyString(), anyDouble(), anyString())).thenReturn(true);
        when(investmentHistoryRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        Exception exception = assertThrows(RuntimeException.class, () -> 
            investmentService.investInFund("0123", "user1", "F01", 50.0)
        );

        assertTrue(exception.getMessage().contains("Your money is refunded"));
        verify(paymentService).processTopUp("0123", "user1", 50.0);
    }

    // UT-05-004: Sell Fund Success
    @Test
    void testSellFund_Success() {
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId("user1");
        portfolio.updateHoldings("F01", 10.0);

        when(portfolioRepository.findByUserId("user1")).thenReturn(Optional.of(portfolio));
        when(fundRepository.findById("F01")).thenReturn(Optional.of(sampleFund));

        investmentService.sellFund("0123", "user1", "F01", 5.0);

        verify(paymentService).processTopUp("0123", "user1", 5.0); // 5 units * 1.0 price
        
        ArgumentCaptor<Portfolio> portCaptor = ArgumentCaptor.forClass(Portfolio.class);
        verify(portfolioRepository).save(portCaptor.capture());
        assertEquals(5.0, portCaptor.getValue().getUnitsForFund("F01"));
        
        verify(investmentHistoryRepository).save(argThat(h -> h.getType().equals("SELL")));
    }

    // UT-05-005: Sell Fund Failed (Insufficient Units)
    @Test
    void testSellFund_InsufficientUnits() {
        // Setup portfolio with only 3.0 units
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId("user1");
        portfolio.updateHoldings("F01", 3.0); 

        when(portfolioRepository.findByUserId("user1")).thenReturn(Optional.of(portfolio));

        // Execute & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            investmentService.sellFund("0123", "user1", "F01", 10.0)
        );

        // Verify error message and that no financial/DB actions occurred
        assertEquals("Insufficient units in this fund. You only have 3.0 units.", exception.getMessage());
        verify(paymentService, never()).processTopUp(anyString(), anyString(), anyDouble());
        verify(portfolioRepository, never()).save(any());
        verify(investmentHistoryRepository, never()).save(any());
    }

    // UT-05-006: Risk Assessment Logic
    @Test
    void testEvaluateRiskProfile() {
        when(portfolioRepository.findByUserId("user1")).thenReturn(Optional.of(new Portfolio()));

        String result = investmentService.evaluateRiskProfile("user1", 9); // Aggressive score

        assertTrue(result.contains("AGGRESSIVE"));
        verify(portfolioRepository).save(argThat(p -> p.getRiskCategory().equals("AGGRESSIVE")));
    }
}