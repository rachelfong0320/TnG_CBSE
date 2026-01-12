package com.example.ewallet.service;

import com.example.ewallet.entity.User;
import com.example.ewallet.entity.Wallet;
import com.example.ewallet.repository.UserRepository;
import com.example.ewallet.repository.WalletRepository;

import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public WalletService(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public Wallet findOrCreateWallet(String username, double initialBalance) {

        // Find or create user
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username)));

        // Find wallet by userId
        Wallet wallet = walletRepository.findByUserId(user.getId());

        // If no wallet, create one
        if (wallet == null) {
            wallet = walletRepository.save(new Wallet(user.getId(), initialBalance));
        }

        return wallet;
    }

    public Wallet getWallet(String username) {
        return userRepository.findByUsername(username)
                .map(user -> walletRepository.findByUserId(user.getId()))
                .orElse(null);
    }

    public Iterable<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public Wallet addFunds(String username, double amount) {
        Wallet wallet = getWallet(username);
        if (wallet != null) {
            wallet.setBalance(wallet.getBalance() + amount);
            walletRepository.save(wallet);
            return wallet;
        } else {
            System.out.println("User wallet not found: " + username);
            return null;
        }
    }

    // New content added by mingyang
    // WalletService to handle the money deduction.
    public boolean deductBalance(String username, double amount, String description) {
        Wallet wallet = getWallet(username);
        if (wallet != null && wallet.getBalance() >= amount) {
            wallet.setBalance(wallet.getBalance() - amount);
            walletRepository.save(wallet);
            return true;
        }
        return false;
    }
}
