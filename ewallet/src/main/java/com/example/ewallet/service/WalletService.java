package com.example.ewallet.service;

import com.example.ewallet.entity.User;
import com.example.ewallet.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final UserRepository userRepository;

    public WalletService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Find user by username, return null if not exists
    public User findUser(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // Create wallet if not exists
    public User createWallet(String username, double balance) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username, balance)));
    }

    // Retrieve all wallet accounts
    public Iterable<User> getAllWallets() {
        return userRepository.findAll();

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
}
