package com.example.ewallet.service;

import com.example.ewallet.entity.ClaimRecord;
import com.example.ewallet.entity.MotorPolicy;
import com.example.ewallet.repository.ClaimRepository;
import com.example.ewallet.repository.PolicyRepository;
import com.example.ewallet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InsuranceService insuranceService;

    // UT-06-001: Purchase Motor Policy Success
    @Test
    void testPurchaseMotorPolicy_Success() {
        // Arrange
        when(paymentService.processPayment(
                anyString(), anyString(), anyDouble(), anyString())
        ).thenReturn(true);

        // Act
        insuranceService.purchaseMotorPolicy(
                "0123456789",
                "user1",
                "ABC1234",
                "Toyota"
        );

        // Assert
        verify(paymentService, times(1))
                .processPayment(anyString(), anyString(), eq(500.0), contains("Motor"));

        verify(policyRepository, times(1))
                .save(any(MotorPolicy.class));
    }

    // UT-06-002: Purchase Motor Policy Failed (Payment Failed)
    @Test
    void testPurchaseMotorPolicy_PaymentFailed() {
        // Arrange
        when(paymentService.processPayment(
                anyString(), anyString(), anyDouble(), anyString())
        ).thenReturn(false);

        // Act
        insuranceService.purchaseMotorPolicy(
                "0123456789",
                "user1",
                "ABC1234",
                "Toyota"
        );

        // Assert
        verify(policyRepository, never()).save(any());
        verify(notificationService, never()).generateNotification(any(), any(), any());
    }

    // UT-06-003: Submit Claim Generates Notification
    @Test
    void testSubmitClaim_GeneratesNotification() {
        // Arrange
        when(claimRepository.save(any(ClaimRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ClaimRecord result = insuranceService.submitClaim(
                "0123456789",
                "POLICY123",
                300.0
        );

        // Assert
        assertNotNull(result);
        assertEquals("Pending", result.getStatus());

        verify(claimRepository, times(1))
                .save(any(ClaimRecord.class));

        verify(notificationService, times(1))
                .generateNotification(
                        eq("0123456789"),
                        eq("CLAIM"),
                        contains("POLICY123")
                );
    }
}
