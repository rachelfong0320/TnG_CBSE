package com.example.ewallet.service;

import com.example.ewallet.entity.Notification;
import com.example.ewallet.entity.User;
import com.example.ewallet.repository.NotificationRepository;
import com.example.ewallet.repository.UserRepository;
import com.example.ewallet.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for NotificationService
 * Tests the full interaction between services and repositories
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NotificationIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private String testPhoneNumber;
    private String testUsername;

    @BeforeEach
    void setUp() {
        // Clean up repositories before each test
        notificationRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        // Setup test user
        testPhoneNumber = "0123456789";
        testUsername = "testUser";
        
        // Create user and wallet
        walletService.findOrCreateWallet(testUsername, testPhoneNumber, 1000.0);
    }

    @AfterEach
    void tearDown() {
        // Clean up after tests
        notificationRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Integration Test Case 1: End-to-End Notification Creation and Retrieval
     * Tests: NotificationService + NotificationRepository + UserRepository
     * Scenario: Create a user, generate multiple notifications, retrieve them from database
     */
    @Test
    void testEndToEndNotificationCreationAndRetrieval() {
        // Act: Generate multiple notifications
        notificationService.generateNotification(testPhoneNumber, "PAYMENT", "Payment 1 completed");
        notificationService.generateNotification(testPhoneNumber, "WALLET", "Wallet topped up");
        notificationService.generateNotification(testPhoneNumber, "CLAIM", "Claim status");

        // Assert: Retrieve all notifications
        List<Notification> allNotifications = notificationService.getAllNotifications(testPhoneNumber);
        assertEquals(3, allNotifications.size(), "Should have 3 notifications");

        // Verify notification types
        assertTrue(allNotifications.stream().anyMatch(n -> n.getType().equals("PAYMENT")));
        assertTrue(allNotifications.stream().anyMatch(n -> n.getType().equals("WALLET")));
        assertTrue(allNotifications.stream().anyMatch(n -> n.getType().equals("CLAIM")));

        // Verify all are unread
        long unreadCount = notificationService.getUnreadCount(testPhoneNumber);
        assertEquals(3, unreadCount, "All 3 notifications should be unread");

        // Verify unread notifications list
        List<Notification> unreadNotifs = notificationService.getUnreadNotifications(testPhoneNumber);
        assertEquals(3, unreadNotifs.size(), "Should have 3 unread notifications");
        assertTrue(unreadNotifs.stream().allMatch(n -> !n.isRead()), "All should be unread");
    }

}
