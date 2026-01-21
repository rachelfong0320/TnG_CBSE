package com.example.ewallet.service;

import com.example.ewallet.entity.*;
import com.example.ewallet.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InvestmentIntegrationTest {

    // Simulates a user starting the app and exiting immediately to let tests run
    static {
        String fakeInput = "999\ninvestorUser\n500\n0\n";
        System.setIn(new ByteArrayInputStream(fakeInput.getBytes()));
    }

    @Autowired private InvestmentService investmentService;
    @Autowired private WalletService walletService;
    @Autowired private FundRepository fundRepository;
    @Autowired private PortfolioRepository portfolioRepository;
    @Autowired private InvestmentHistoryRepository historyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;

    @BeforeEach
    void setup() {
        // Manual cleanup (Order matters due to Foreign Key constraints)
        fundRepository.deleteAll();
        historyRepository.deleteAll();
        portfolioRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        
        // Setup User and Wallet for tests
        String username = "investor1";
        String phone = "01122334455";
        userRepository.save(new User(username, phone));
        walletService.findOrCreateWallet(username, phone, 500.0);

        // Ensure sample funds exist in the DB
        investmentService.initSampleFunds();
    }

    @Test
    void testFullInvestmentCycle_Integration() {
        String username = "investor1";
        String phone = "01122334455";
        double investAmount = 100.0;

        // 1. EXECUTE: Buy units of Low Risk Income Fund (F01 - Price: 1.0)
        InvestmentHistory history = investmentService.investInFund(phone, username, "F01", investAmount);

        // 2. VERIFY: Investment Record
        assertNotNull(history, "History record should be saved and returned");
        assertEquals(100.0, history.getUnits());
        assertEquals("Success", history.getStatus());

        // 3. VERIFY: Wallet Balance (500 - 100 = 400)
        Wallet updatedWallet = walletService.getWallet(phone);
        assertEquals(400.0, updatedWallet.getBalance(), 0.01);

        // 4. VERIFY: Portfolio Persistence
        Portfolio portfolio = investmentService.getUserPortfolio(username);
        assertEquals(100.0, portfolio.getUnitsForFund("F01"));
    }

    @Test
    void testMarketSimImpactOnReturns() {
        String username = "investor1";
        String phone = "01122334455";
        
        investmentService.investInFund(phone, username, "F01", 100.0);

        // Force a price change in DB (Market Simulation)
        Fund f01 = fundRepository.findById("F01").orElseThrow();
        f01.setPrice(1.50); 
        fundRepository.save(f01);

        // Calculate returns: (100 units * 1.50) - 100 cost = RM 50 Profit
        double netReturns = investmentService.calculateReturns(username);
        assertEquals(50.0, netReturns, 0.01);
        
        String summary = investmentService.getPortfolioSummary(username);
        assertTrue(summary.contains("PROFIT"));
        assertTrue(summary.contains("RM 50.00"));
    }

    @Test
    void testRiskProfilePersistence() {
        String username = "investor1";
        
        investmentService.evaluateRiskProfile(username, 9); // Aggressive Score

        Portfolio p = portfolioRepository.findByUserId(username).orElseThrow();
        assertEquals("AGGRESSIVE", p.getRiskCategory());
    }
}