package com.example.ewallet.service;

import com.example.ewallet.entity.User;
import com.example.ewallet.entity.Wallet;
import com.example.ewallet.repository.UserRepository;
import com.example.ewallet.repository.WalletRepository;

import org.springframework.context.annotation.Lazy;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final NotificationService notificationService;

    private static final double LOW_BALANCE_THRESHOLD = 50.0;

    public WalletService(UserRepository userRepository, WalletRepository walletRepository, @Lazy NotificationService notificationService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.notificationService = notificationService;
    }

    public Wallet findOrCreateWallet(String username, String phoneNumber, double initialBalance) {

        // Find or create user
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> userRepository.save(new User(username, phoneNumber)));

        // Find wallet by userId
        Wallet wallet = walletRepository.findByUserId(user.getId());

        // If no wallet, create one
        if (wallet == null) {
            wallet = walletRepository.save(new Wallet(user.getId(), initialBalance));
        }

        return wallet;
    }

    public Wallet getWallet(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(user -> walletRepository.findByUserId(user.getId()))
                .orElse(null);
    }

    public Iterable<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    public Wallet addMoney(String phoneNumber, double amount) {
        Wallet wallet = getWallet(phoneNumber);
        if (wallet != null) {
            wallet.setBalance(wallet.getBalance() + amount);
            wallet.addTransaction("TOP_UP", amount, "Added money");
            walletRepository.save(wallet);
            notificationService.generateNotification(username, "WALLET", 
                String.format("RM %.2f added to wallet. New balance: RM %.2f", amount, wallet.getBalance()));
            return wallet;
        } else {
            System.out.println("User wallet not found: " + phoneNumber);
            return null;
        }
    }

    public boolean deductBalance(String phoneNumber, double amount, String description) {
        Wallet wallet = getWallet(phoneNumber);
        if (wallet != null && wallet.getBalance() >= amount) {
            wallet.setBalance(wallet.getBalance() - amount);
            walletRepository.save(wallet);
            
            // Check for low balance and notify
            if (wallet.getBalance() < LOW_BALANCE_THRESHOLD) {
                notificationService.notifyLowBalance(username, wallet.getBalance());
            }
            
            return true;
        }
        return false;
    }

    public boolean sendMoney(String senderPhoneNumber, String recipientPhoneNumber, double amount) {
        Wallet senderWallet = getWallet(senderPhoneNumber);
        if (senderWallet == null || senderWallet.getBalance() < amount) {
            return false;
        }

        User senderUser = userRepository.findByPhoneNumber(senderPhoneNumber).orElse(null);
        User recipientUser = userRepository.findByPhoneNumber(recipientPhoneNumber).orElse(null);
        if (senderUser == null || recipientUser == null) {
            return false;
        }

        Wallet recipientWallet = walletRepository.findByUserId(recipientUser.getId());
        if (recipientWallet == null) {
            return false;
        }

        // Prevent self-transfer
        if (senderPhoneNumber.equals(recipientPhoneNumber)) {
            return false;
        }

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        recipientWallet.setBalance(recipientWallet.getBalance() + amount);

        senderWallet.addTransaction("SEND", amount,
                "Sent to " + recipientUser.getUsername() + " (" + recipientPhoneNumber + ")");
        recipientWallet.addTransaction("RECEIVE", amount,
                "Received from " + senderUser.getUsername() + " (" + senderPhoneNumber + ")");

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        return true;
    }

    public List<Wallet.Transaction> getTransactionHistory(String phoneNumber) {
        Wallet wallet = getWallet(phoneNumber);
        return wallet != null ? wallet.getTransactions() : List.of();
    }

}
