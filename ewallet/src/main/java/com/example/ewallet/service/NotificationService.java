package com.example.ewallet.service;

import com.example.ewallet.entity.*;
import com.example.ewallet.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AutoPayDataRepository autoPayDataRepository;
    private final PaymentDataRepository paymentDataRepository;
    private final QRDataRepository qrDataRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final FundRepository fundRepository;
    private final InvestmentHistoryRepository investmentHistoryRepository;
    private final ClaimRepository claimRepository;

    public NotificationService(NotificationRepository notificationRepository,
            AutoPayDataRepository autoPayDataRepository,
            PaymentDataRepository paymentDataRepository,
            QRDataRepository qrDataRepository,
            WalletRepository walletRepository,
            UserRepository userRepository,
            FundRepository fundRepository,
            InvestmentHistoryRepository investmentHistoryRepository,
            ClaimRepository claimRepository) {
        this.notificationRepository = notificationRepository;
        this.autoPayDataRepository = autoPayDataRepository;
        this.paymentDataRepository = paymentDataRepository;
        this.qrDataRepository = qrDataRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.fundRepository = fundRepository;
        this.investmentHistoryRepository = investmentHistoryRepository;
        this.claimRepository = claimRepository;
    }

    private User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    private String getUserId(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        return user != null ? user.getId() : null;
    }

    public void generateNotification(String phoneNumber, String type, String message) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user != null) {
            Notification notification = new Notification(user.getId(), user.getUsername(), phoneNumber, type, message);
            notificationRepository.save(notification);
        }
    }

    public List<Notification> getAllNotifications(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return List.of();
        return notificationRepository.findByUserIdOrderByTimestampAsc(user.getId());
    }

    public List<Notification> getUnreadNotifications(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return List.of();
        return notificationRepository.findByUserIdAndReadOrderByTimestampDesc(user.getId(), false);
    }

    public long getUnreadCount(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return 0;
        return notificationRepository.countByUserIdAndRead(user.getId(), false);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void markAllAsRead(String phoneNumber) {
        getUnreadNotifications(phoneNumber).forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public String getAutoPayStatus(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return "User not found";
        List<AutoPayData> autoPayList = autoPayDataRepository.findByUserId(user.getUsername());
        if (autoPayList.isEmpty())
            return "No AutoPay setup";
        return autoPayList.size() + " active AutoPay(s)";
    }

    public String getPaymentStatus(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return "User not found";
        List<PaymentData> payments = paymentDataRepository.findByUserId(user.getUsername());
        if (payments.isEmpty())
            return "No payments made";
        PaymentData latest = payments.get(payments.size() - 1);
        return String.format("Last payment: RM %.2f to %s - %s", latest.getAmount(), latest.getRecipientId(),
                latest.getStatus());
    }

    public String getQRPaymentStatus(String phoneNumber) {
        User user = getUserByPhoneNumber(phoneNumber);
        if (user == null)
            return "User not found";
        List<QRData> qrPayments = qrDataRepository.findByUserId(user.getUsername());
        if (qrPayments.isEmpty())
            return "No QR payments made";
        return qrPayments.size() + " QR payment(s) made";
    }

    public Double getWalletBalance(String phoneNumber) {
        String userId = getUserId(phoneNumber);
        if (userId == null)
            return 0.0;
        Wallet wallet = walletRepository.findByUserId(userId);
        return wallet != null ? wallet.getBalance() : 0.0;
    }

    public String getInvestmentStatus(String phoneNumber) {
        String userId = getUserId(phoneNumber);
        if (userId == null)
            return "User not found";
        List<InvestmentHistory> investments = investmentHistoryRepository.findByUserId(userId);
        if (investments.isEmpty())
            return "No investments made";
        double total = investments.stream().mapToDouble(InvestmentHistory::getAmount).sum();
        return String.format("%d investment(s), Total: RM %.2f", investments.size(), total);
    }

    public String getClaimStatus(String phoneNumber) {
        String userId = getUserId(phoneNumber);
        if (userId == null)
            return "User not found";
        List<ClaimRecord> allClaims = claimRepository.findAll();
        if (allClaims.isEmpty())
            return "No claims filed";
        ClaimRecord latest = allClaims.get(allClaims.size() - 1);
        return String.format("Last claim: %s - RM %.2f", latest.getStatus(), latest.getAmount());
    }

    public Double getFundPrice(String fundName) {
        return fundRepository.findByName(fundName).map(Fund::getPrice).orElse(0.0);
    }

    public void notifyPaymentCompleted(String phoneNumber, String recipient, double amount) {
        String message = String.format("Payment of RM %.2f to %s completed successfully", amount, recipient);
        generateNotification(phoneNumber, "PAYMENT", message);
    }

    public void notifyLowBalance(String phoneNumber, double balance) {
        String message = String.format("Low wallet balance alert! Current balance: RM %.2f", balance);
        generateNotification(phoneNumber, "WALLET", message);
    }

    public void notifyAutoPayExecuted(String phoneNumber, String service, double amount) {
        String message = String.format("AutoPay executed: RM %.2f paid to %s", amount, service);
        generateNotification(phoneNumber, "AUTOPAY", message);
    }

    public void notifyQRPayment(String phoneNumber, String merchant, double amount) {
        String message = String.format("QR payment of RM %.2f to %s successful", amount, merchant);
        generateNotification(phoneNumber, "QR", message);
    }

    public void notifyInvestmentMade(String phoneNumber, String fundName, double amount, double units) {
        String message = String.format("Invested RM %.2f in %s (%.4f units)", amount, fundName, units);
        generateNotification(phoneNumber, "INVESTMENT", message);
    }

    public void notifyFundPriceChange(String phoneNumber, String fundName, double oldPrice, double newPrice) {
        double change = ((newPrice - oldPrice) / oldPrice) * 100;
        String message = String.format("Fund %s price changed: RM %.2f -> RM %.2f (%.2f%%)", fundName, oldPrice,
                newPrice, change);
        generateNotification(phoneNumber, "FUND", message);
    }

    public void notifyClaimUpdate(String phoneNumber, String policyType, String status, double amount) {
        String message = String.format("Claim update: %s claim for RM %.2f - Status: %s", policyType, amount, status);
        generateNotification(phoneNumber, "CLAIM", message);
    }

    public void displayNotifications(String phoneNumber) {
        List<Notification> notifications = getAllNotifications(phoneNumber);
        long unreadCount = getUnreadCount(phoneNumber);

        System.out.println("\n=== NOTIFICATIONS ===");
        System.out.printf("Total: %d | Unread: %d%n", notifications.size(), unreadCount);
        System.out.println("--------------------");

        if (notifications.isEmpty()) {
            System.out.println("No notifications yet.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        for (int i = 0; i < notifications.size(); i++) {
            Notification notif = notifications.get(i);
            String readStatus = notif.isRead() ? "[ ]" : "[*]";
            System.out.printf("%d. %s [%s] %s%n", i + 1, readStatus, notif.getType(),
                    notif.getTimestamp().format(formatter));
            System.out.printf("   %s%n", notif.getMessage());
        }
    }

    public void displayNotificationSummary(String phoneNumber) {
        System.out.println("\n--- Notification Summary ---");
        System.out.println("AutoPay Status: " + getAutoPayStatus(phoneNumber));
        System.out.println("Payment Status: " + getPaymentStatus(phoneNumber));
        System.out.println("QR Payment Status: " + getQRPaymentStatus(phoneNumber));
        System.out.printf("Wallet Balance: RM %.2f%n", getWalletBalance(phoneNumber));
        System.out.println("Investment Status: " + getInvestmentStatus(phoneNumber));
        System.out.println("Claim Status: " + getClaimStatus(phoneNumber));
    }
}
