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

    public void generateNotification(String username, String type, String details) {
        Notification notification = new Notification(username, type, details);
        notificationRepository.save(notification);
    }

    public String getAutoPayStatus(String username) {
        List<AutoPayData> autoPayList = autoPayDataRepository.findByUserId(username);
        if (autoPayList.isEmpty()) {
            return "No AutoPay setup";
        }
        return autoPayList.size() + " active AutoPay(s)";
    }

    public String getPaymentStatus(String username) {
        List<PaymentData> payments = paymentDataRepository.findByUserId(username);
        if (payments.isEmpty()) {
            return "No payments made";
        }
        PaymentData latestPayment = payments.get(payments.size() - 1);
        return "Last payment: RM " + String.format("%.2f", latestPayment.getAmount()) + 
               " to " + latestPayment.getRecipientId() + " - " + latestPayment.getStatus();
    }

    public String getQRPaymentStatus(String username) {
        List<QRData> qrPayments = qrDataRepository.findByUserId(username);
        if (qrPayments.isEmpty()) {
            return "No QR payments made";
        }
        return qrPayments.size() + " QR payment(s) made";
    }

    public Double getWalletBalance(String username) {
        return userRepository.findByUsername(username)
                .map(user -> walletRepository.findByUserId(user.getId()))
                .map(wallet -> wallet.getBalance())
                .orElse(0.0);
    }

    public Double getFundPrice(String fundName) {
        return fundRepository.findByName(fundName)
                .map(Fund::getPrice)
                .orElse(0.0);
    }

    public String getInvestmentStatus(String username) {
        List<InvestmentHistory> investments = investmentHistoryRepository.findByUserId(username);
        if (investments.isEmpty()) {
            return "No investments made";
        }
        double totalInvested = investments.stream()
                .mapToDouble(InvestmentHistory::getAmount)
                .sum();
        return investments.size() + " investment(s), Total: RM " + String.format("%.2f", totalInvested);
    }

    public String getClaimStatus(String username) {
        List<ClaimRecord> allClaims = claimRepository.findAll();
        if (allClaims.isEmpty()) {
            return "No claims filed";
        }
        ClaimRecord latestClaim = allClaims.get(allClaims.size() - 1);
        return "Last claim: " + latestClaim.getStatus() + " - RM " + 
               String.format("%.2f", latestClaim.getAmount());
    }

    public void notifyPaymentCompleted(String username, String recipient, double amount) {
        String message = String.format("Payment of RM %.2f to %s completed successfully", amount, recipient);
        generateNotification(username, "PAYMENT", message);
    }

    public void notifyLowBalance(String username, double balance) {
        String message = String.format("Low wallet balance alert! Current balance: RM %.2f", balance);
        generateNotification(username, "WALLET", message);
    }

    public void notifyAutoPayExecuted(String username, String service, double amount) {
        String message = String.format("AutoPay executed: RM %.2f paid to %s", amount, service);
        generateNotification(username, "AUTOPAY", message);
    }

    public void notifyQRPayment(String username, String merchant, double amount) {
        String message = String.format("QR payment of RM %.2f to %s successful", amount, merchant);
        generateNotification(username, "QR", message);
    }

    public void notifyInvestmentMade(String username, String fundName, double amount, double units) {
        String message = String.format("Invested RM %.2f in %s (%.4f units)", amount, fundName, units);
        generateNotification(username, "INVESTMENT", message);
    }

    public void notifyFundPriceChange(String username, String fundName, double oldPrice, double newPrice) {
        double change = ((newPrice - oldPrice) / oldPrice) * 100;
        String message = String.format("Fund %s price changed: RM %.2f to RM %.2f (%.2f%%)", 
                                      fundName, oldPrice, newPrice, change);
        generateNotification(username, "FUND", message);
    }

    public void notifyClaimUpdate(String username, String policyType, String status, double amount) {
        String message = String.format("Claim update: %s claim for RM %.2f - Status: %s", 
                                      policyType, amount, status);
        generateNotification(username, "CLAIM", message);
    }

    // Get all notifications for a user (oldest first, newest last)
    public List<Notification> getAllNotifications(String username) {
        return notificationRepository.findByUsernameOrderByTimestampAsc(username);
    }

    // Get unread notifications
    public List<Notification> getUnreadNotifications(String username) {
        return notificationRepository.findByUsernameAndReadOrderByTimestampDesc(username, false);
    }

    // Get unread count
    public long getUnreadCount(String username) {
        return notificationRepository.countByUsernameAndRead(username, false);
    }

    // Mark notification as read
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    // Mark all as read
    public void markAllAsRead(String username) {
        List<Notification> unreadNotifications = getUnreadNotifications(username);
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    // Display all notifications (console version)
    public void displayNotifications(String username) {
        List<Notification> notifications = getAllNotifications(username);
        long unreadCount = getUnreadCount(username);

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

    // Get notification summary for dashboard
    public void displayNotificationSummary(String username) {
        System.out.println("\n--- Notification Summary ---");
        System.out.println("AutoPay Status: " + getAutoPayStatus(username));
        System.out.println("Payment Status: " + getPaymentStatus(username));
        System.out.println("QR Payment Status: " + getQRPaymentStatus(username));
        System.out.printf("Wallet Balance: RM %.2f%n", getWalletBalance(username));
        System.out.println("Investment Status: " + getInvestmentStatus(username));
        System.out.println("Claim Status: " + getClaimStatus(username));
    }
}
