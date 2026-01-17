package com.example.ewallet.service;

import com.example.ewallet.entity.Notification;
import com.example.ewallet.entity.User;
import com.example.ewallet.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationUnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AutoPayDataRepository autoPayDataRepository;

    @Mock
    private PaymentDataRepository paymentDataRepository;

    @Mock
    private QRDataRepository qrDataRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FundRepository fundRepository;

    @Mock
    private InvestmentHistoryRepository investmentHistoryRepository;

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test user - ID will be mocked when retrieved from repository
        testUser = new User("testUser", "0123456789");
    }

    /**
     * Test Case ID: UT-03-001
     * Test Scenario: Successfully generate a notification for valid user
     * Expected Result: Notification is saved to repository
     */
    @Test
    void testGenerateNotification_ValidUser_Success() {
        try {
            // Arrange
            String phoneNumber = "0123456789";
            String type = "PAYMENT";
            String message = "Paid RM10";

            // Mock the user to have an ID when returned from repository
            when(userRepository.findByPhoneNumber(phoneNumber)).thenAnswer(invocation -> {
                User user = new User("testUser", phoneNumber);
                // Use reflection to set the ID since there's no setter
                java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, "user123");
                return Optional.of(user);
            });
            when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
                Notification notif = invocation.getArgument(0);
                notif.setId("notif123");
                return notif;
            });

            // Act
            notificationService.generateNotification(phoneNumber, type, message);

            // Assert
            verify(userRepository, times(1)).findByPhoneNumber(phoneNumber);
            verify(notificationRepository, times(1)).save(any(Notification.class));
            
            // Verify the notification object passed to save has correct properties
            verify(notificationRepository).save(argThat(notification ->
                notification.getUserId().equals("user123") &&
                notification.getUsername().equals("testUser") &&
                notification.getPhoneNumber().equals(phoneNumber) &&
                notification.getType().equals(type) &&
                notification.getMessage().equals(message) &&
                !notification.isRead()
            ));
        } catch (Exception e) {
            fail("Test failed due to reflection error: " + e.getMessage());
        }
    }

    /**
     * Test Case ID: UT-03-002
     * Test Scenario: No notification generated when user does not exist
     * Expected Result: No notification saved, no exceptions thrown
     */
    @Test
    void testGenerateNotification_UserNotFound_NoNotificationSaved() {
        // Arrange
        String phoneNumber = "999999999";
        String type = "PAYMENT";
        String message = "Paid RM10";

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // Act
        notificationService.generateNotification(phoneNumber, type, message);

        // Assert
        verify(userRepository, times(1)).findByPhoneNumber(phoneNumber);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    /**
     * Test Case ID: UT-03-003
     * Test Scenario: Service returns unread notifications only
     * Expected Result: Only unread notifications (N1, N2) are returned, read notification (N3) is excluded
     */
    @Test
    void testGetUnreadNotifications_ReturnsOnlyUnreadNotifications() {
        try {
            // Arrange
            String phoneNumber = "0123456789";
            
            // Create user with ID using reflection
            User user = new User("testUser", phoneNumber);
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, "user123");
            
            Notification n1 = new Notification("user123", "testUser", phoneNumber, "PAYMENT", "Message 1");
            n1.setId("N1");
            n1.setRead(false);
            
            Notification n2 = new Notification("user123", "testUser", phoneNumber, "WALLET", "Message 2");
            n2.setId("N2");
            n2.setRead(false);
            
            Notification n3 = new Notification("user123", "testUser", phoneNumber, "INVESTMENT", "Message 3");
            n3.setId("N3");
            n3.setRead(true);

            when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(user));
            when(notificationRepository.findByUserIdAndReadOrderByTimestampAsc("user123", false))
                .thenReturn(Arrays.asList(n1, n2));

            // Act
            List<Notification> result = notificationService.getUnreadNotifications(phoneNumber);

            // Assert
            verify(userRepository, times(1)).findByPhoneNumber(phoneNumber);
            verify(notificationRepository, times(1)).findByUserIdAndReadOrderByTimestampAsc("user123", false);
            
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(n -> !n.isRead()));
            assertTrue(result.stream().anyMatch(n -> n.getId().equals("N1")));
            assertTrue(result.stream().anyMatch(n -> n.getId().equals("N2")));
            assertFalse(result.stream().anyMatch(n -> n.getId().equals("N3")));
        } catch (Exception e) {
            fail("Test failed due to reflection error: " + e.getMessage());
        }
    }

    /**
     * Test Case ID: UT-03-004
     * Test Scenario: A single notification is marked as read
     * Expected Result: Notification read status is set to true and saved
     */
    @Test
    void testMarkAsRead_NotificationExists_MarkedAsRead() {
        // Arrange
        String notificationId = "N1";
        
        Notification notification = new Notification("user123", "testUser", "0123456789", "PAYMENT", "Test message");
        notification.setId(notificationId);
        notification.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(1)).save(notification);
        assertTrue(notification.isRead());
    }
}
