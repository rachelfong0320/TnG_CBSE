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
}
