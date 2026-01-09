package com.example.ewallet.controller;

import com.example.ewallet.entity.User;
import com.example.ewallet.service.WalletService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // Create wallet (new user with initial balance)
    @PostMapping("/create")
    public User createWallet(
            @RequestParam String username,
            @RequestParam double balance) {
        return walletService.createWallet(username, balance);
    }

    // View all wallets
    @GetMapping("/all")
    public Iterable<User> getAllWallets() {
        return walletService.getAllWallets();
    }
}
