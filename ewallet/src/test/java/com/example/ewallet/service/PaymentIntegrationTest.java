package com.example.ewallet.service;

import com.example.ewallet.entity.PaymentData;
import com.example.ewallet.entity.User;
import com.example.ewallet.entity.Wallet;
import com.example.ewallet.repository.PaymentDataRepository;
import com.example.ewallet.repository.UserRepository;
import com.example.ewallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentIntegrationTest {

    // --- FIX: Updated Fake Input ---
    // Sequence: 
    // 1. "user1" (Username)
    // 2. "100"   (Initial Balance - needed because DB is empty when App starts)
    // 3. "0"     (Exit Menu)
    static {
        String fakeInput = "user1\n100\n0\n";
        System.setIn(new ByteArrayInputStream(fakeInput.getBytes()));
    }
    // --------------------------------

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentDataRepository paymentDataRepository;

    @BeforeEach
    void setup() {
        // This runs AFTER the Main Menu has finished and exited.
        // We clear whatever the Main Menu created and set up our fresh test state.
        walletRepository.deleteAll();
        userRepository.deleteAll();
        paymentDataRepository.deleteAll();

        userRepository.save(new User("user1", "0123456789"));
        walletService.findOrCreateWallet("user1", "0123456789", 100.0);
    }

    @Test
    void testPurchaseUpdatesWallet_Integration() {
        // Test Data
        String phoneNumber = "0122222222";
        String username = "user1";
        double purchaseAmount = 40.0;
        String merchant = "Tesco";

        // 1. Run the logic
        boolean success = paymentService.processPayment(phoneNumber, username, purchaseAmount, merchant);

        // 2. Verify success
        assertTrue(success, "Payment should succeed");

        // 3. Verify Database
        Wallet updatedWallet = walletService.getWallet(username);
        assertEquals(60.0, updatedWallet.getBalance(), 0.01, "Wallet balance should be 60.0");

        List<PaymentData> history = paymentDataRepository.findByUserId(username);
        assertFalse(history.isEmpty());
        assertEquals("Tesco", history.get(0).getRecipientId());
    }
}