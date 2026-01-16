package com.example.ewallet.service;

import com.example.ewallet.entity.AutoPayData;
import com.example.ewallet.entity.PaymentData;
import com.example.ewallet.entity.QRData;
import com.example.ewallet.repository.AutoPayDataRepository;
import com.example.ewallet.repository.PaymentDataRepository;
import com.example.ewallet.repository.QRDataRepository;
import com.example.ewallet.repository.UserRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentDataRepository paymentDataRepository;
    private final QRDataRepository qrDataRepository;
    private final AutoPayDataRepository autoPayDataRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public PaymentService(PaymentDataRepository paymentDataRepository,
            QRDataRepository qrDataRepository,
            AutoPayDataRepository autoPayDataRepository,
            WalletService walletService,
            @Lazy NotificationService notificationService,
            UserRepository userRepository) {
        this.paymentDataRepository = paymentDataRepository;
        this.qrDataRepository = qrDataRepository;
        this.autoPayDataRepository = autoPayDataRepository;
        this.walletService = walletService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // 1. Process Retail Payment
    public boolean processPayment(String phoneNumber, String username, double amount, String merchantName) {
        boolean success = walletService.deductBalance(phoneNumber, amount, "Payment to " + merchantName);
        PaymentData payment = new PaymentData(username, amount, merchantName, success ? "SUCCESS" : "FAILED");
        paymentDataRepository.save(payment);

        if (success) {
            System.out.println("Payment successful.");
            notificationService.generateNotification(phoneNumber, "PAYMENT",
                    String.format("Payment of RM %.2f to %s completed successfully", amount, merchantName));
        } else {
            System.out.println("Payment failed: Insufficient funds.");
            notificationService.generateNotification(phoneNumber, "PAYMENT",
                    String.format("Payment FAILED: RM %.2f to %s (Insufficient funds)", amount, merchantName));
        }

        return success;
    }

    // 2. Process QR Payment
    public void processQRPayment(String phoneNumber, String username, String qrString) {
        try {
            String[] parts = qrString.split(":");
            if (parts.length == 2) {
                String merchant = parts[0];
                double amount = Double.parseDouble(parts[1]);

                boolean success = walletService.deductBalance(phoneNumber, amount, "QR Pay: " + merchant);
                QRData qr = new QRData(username, qrString, merchant, amount, success ? "SUCCESS" : "FAILED");
                qrDataRepository.save(qr);

                if (success) {
                    System.out.println("QR Payment successful!");
                    notificationService.generateNotification(phoneNumber, "QR",
                            String.format("QR payment of RM %.2f to %s successful", amount, merchant));
                } else {
                    System.out.println("QR Payment failed: Insufficient funds.");
                    notificationService.generateNotification(phoneNumber, "QR",
                            String.format("QR Payment FAILED: RM %.2f to %s (Insufficient funds)", amount, merchant));
                }
            } else {
                System.out.println("Invalid QR format.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid QR Amount.");
        }
    }

    // 3. Setup AutoPay ONLY (No Deduction yet)
    public void setupAutoPay(String username, String billerName, double amount, int billingDay) {
        AutoPayData autoPay = new AutoPayData(username, billerName, amount, billingDay);
        autoPayDataRepository.save(autoPay);
        System.out.println("AutoPay Setup Successfully!");
        System.out.println("Schedule: Deduct RM" + amount + " on Day " + billingDay + " of every month.");
        System.out.println("No funds have been deducted yet.");
        userRepository.findByUsername(username).ifPresent(user -> {
            notificationService.generateNotification(user.getPhoneNumber(), "AUTOPAY",
                    String.format("AutoPay setup for %s - RM %.2f on day %d of each month",
                            billerName, amount, billingDay));
        });
    }

    // 4. Simulate Month Passing (Triggers the actual deduction)
    public void simulateAutoPayExecution(String phoneNumber, String username, String currentMonthStr) {
        System.out.println("\n--- Simulating AutoPay Run for " + currentMonthStr + " ---");

        List<AutoPayData> myAutoPays = autoPayDataRepository.findByUserId(username);

        if (myAutoPays.isEmpty()) {
            System.out.println("No active AutoPay setups found.");
            return;
        }

        for (AutoPayData ap : myAutoPays) {
            if ("Active".equals(ap.getStatus())) {
                String desc = "AutoPay (" + currentMonthStr + ") to " + ap.getRecipientId();

                // Attempt Deduction
                boolean success = walletService.deductBalance(phoneNumber, ap.getAmount(), desc);

                // Update Last Executed Date
                ap.setLastExecuted(new Date());
                autoPayDataRepository.save(ap);

                // Log the transaction in PaymentData (so it shows in history)
                PaymentData log = new PaymentData(username, ap.getAmount(), ap.getRecipientId(),
                        success ? "SUCCESS (" + currentMonthStr + ")" : "FAILED (" + currentMonthStr + ")");
                paymentDataRepository.save(log);

                if (success) {
                    System.out.println(" [SUCCESS] Processed scheduled payment to " + ap.getRecipientId());
                    notificationService.generateNotification(phoneNumber, "AUTOPAY",
                            String.format("AutoPay executed: RM %.2f paid to %s", ap.getAmount(), ap.getRecipientId()));
                } else {
                    System.out.println(
                            " [FAILED] Could not process payment to " + ap.getRecipientId() + " (Insufficient Funds)");
                    notificationService.generateNotification(phoneNumber, "AUTOPAY",
                            String.format("AutoPay FAILED for %s - RM %.2f (Insufficient funds)", ap.getRecipientId(),
                                    ap.getAmount()));
                }
            }
        }
        System.out.println("--- End Simulation ---\n");
    }

    // 5. Process Wallet Top-Up
    public void processTopUp(String phoneNumber, String username, double amount) {
        // 1. Call WalletService to actually add the money
        // We assume walletService.addMoney returns the updated Wallet object or null
        var result = walletService.addMoney(phoneNumber, amount);

        if (result != null) {
            // 2. If successful, log it in PaymentData
            PaymentData log = new PaymentData(
                    username,
                    amount,
                    "Wallet Top-Up",
                    "SUCCESS");
            paymentDataRepository.save(log);
            System.out.println("Top-Up Successful! Transaction recorded.");
        } else {
            System.out.println("Top-Up Failed: User not found.");
        }
    }

    // History Methods
    public List<PaymentData> getPaymentHistory(String username) {
        return paymentDataRepository.findByUserId(username);
    }

    public List<QRData> getQRHistory(String username) {
        return qrDataRepository.findByUserId(username);
    }

    public List<AutoPayData> getAutoPayHistory(String username) {
        return autoPayDataRepository.findByUserId(username);
    }
}