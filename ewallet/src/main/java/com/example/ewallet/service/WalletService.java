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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return walletRepository.findByUserId(user.getId());
    }
  
   public Iterable<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }
  
    // New content added by mingyang.I added a deductBalance method in your WalletService to handle the money deduction.
    public boolean deductBalance(String username, double amount, String description) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getBalance() >= amount) {
            user.setBalance(user.getBalance() - amount);
            userRepository.save(user);
            System.out.println("[WALLET LOG]: Deducted RM" + amount + " from " + username + ". Reason: " + description);
            return true;
        }
        return false;

}
