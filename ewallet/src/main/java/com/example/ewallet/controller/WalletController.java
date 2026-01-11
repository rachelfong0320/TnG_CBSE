package com.example.ewallet.controller;

import com.example.ewallet.entity.User;
import com.example.ewallet.entity.Wallet;
import com.example.ewallet.service.WalletService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // Create wallet for user
    @PostMapping("/create")
    public Wallet createWallet(
            @RequestParam String username,
            @RequestParam double balance) {
        return walletService.findOrCreateWallet(username, balance);
    }

    // View wallet by username
    @GetMapping("/{username}")
    public Wallet getWallet(@PathVariable String username) {
        return walletService.getWallet(username);
    }

    // View all wallets
    @GetMapping("/all")
    public Iterable<Wallet> getAllWallets() {
        return walletService.getAllWallets();
    }
}
