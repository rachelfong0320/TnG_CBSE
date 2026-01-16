package com.example.ewallet.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.example.ewallet.entity.Fund;
import com.example.ewallet.entity.InvestmentHistory;
import com.example.ewallet.entity.Portfolio;
import com.example.ewallet.repository.FundRepository;
import com.example.ewallet.repository.InvestmentHistoryRepository;
import com.example.ewallet.repository.PortfolioRepository;

@Service
public class InvestmentService {

    private final FundRepository fundRepository;
    private final InvestmentHistoryRepository investmentHistoryRepository;
    private final PortfolioRepository portfolioRepository;

    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public InvestmentService(FundRepository fundRepository,
            InvestmentHistoryRepository investmentHistoryRepository,
            PortfolioRepository portfolioRepository,
            PaymentService paymentService,
            @Lazy NotificationService notificationService) {
        this.fundRepository = fundRepository;
        this.investmentHistoryRepository = investmentHistoryRepository;
        this.portfolioRepository = portfolioRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    public void initSampleFunds() {
        if (fundRepository.count() == 0) {
            System.out.println("[System] Populating sample funds...");
            createFund("F01", "Low Risk Income Fund", "Focuses on government bonds and steady interest.", "Low", 1.0000);
            createFund("F02", "Balanced Global Fund", "Diversified across international stocks and bonds.", "Medium", 2.5000);
            createFund("F03", "Equity Growth Fund", "High-growth potential targeting tech and emerging markets.", "High", 5.7500);
            createFund("F04", "Digital Assets Fund", "Invests in blockchain infrastructure and crypto-assets.", "High", 10.2000);

            System.out.println("[System] Sample funds initialized.");
        }
    }

    public List<Fund> getAllFunds() {
        return fundRepository.findAll();
    }

    public List<InvestmentHistory> getUserInvestments(String username) {
        return investmentHistoryRepository.findByUserId(username);
    }

    public Portfolio getUserPortfolio(String username) {
        return portfolioRepository.findByUserId(username).orElse(new Portfolio());
    }

    // Buying funds
    public InvestmentHistory investInFund(String phoneNumber, String username, String fundId, double amount) {
        // Validate Fund
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new RuntimeException("Fund not found with ID: " + fundId));

        boolean paymentSuccessful = paymentService.processPayment(phoneNumber, username, amount, "Investment: " + fund.getName());

        if (paymentSuccessful) {
            try {
                // Create Investment Record
                InvestmentHistory investment = new InvestmentHistory();
                investment.setUserId(username);
                investment.setFundId(fundId);
                investment.setType("BUY");
                investment.setAmount(amount);
                investment.calculateUnits(fund.getPrice());
                investment.setStatus("Success");

                updateUserPortfolio(username, fundId, investment.getUnits());
                InvestmentHistory savedInvestment = investmentHistoryRepository.save(investment);

                // Notify user about investment
                notificationService.notifyInvestmentMade(username, fund.getName(), amount, investment.getUnits());

                return savedInvestment;
            } catch (Exception e) {
                paymentService.processTopUp(phoneNumber, username, amount);
                throw new RuntimeException("System Error: Invest in fund failed. Your money is refunded.");
            }
        } else {
            throw new RuntimeException("Insufficient wallet balance.");
        }
    }

    // SELL FUND: Liquidates units, updates portfolio, and adds money back to wallet 
    public void sellFund(String phoneNumber, String username, String fundId, double unitsToSell) {
        // 1. Validate User Portfolio
        Portfolio portfolio = portfolioRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("Portfolio not found for user."));

        double ownedUnits = portfolio.getUnitsForFund(fundId);
        if (ownedUnits < unitsToSell) {
            throw new RuntimeException("Insufficient units in this fund. You only have " + ownedUnits + " units.");
        }

        // 2. Get Current Fund Price for calculation
        Fund fund = fundRepository.findById(fundId).orElseThrow(() -> new RuntimeException("Fund not found."));
        double proceeds = unitsToSell * fund.getPrice();

        // 3. Update holdings
        portfolio.updateHoldings(fundId, -unitsToSell);
        portfolioRepository.save(portfolio);

        // 4. Return money to Wallet
        paymentService.processTopUp(phoneNumber, username, proceeds);

        // 5. Log Transaction
        InvestmentHistory saleLog = new InvestmentHistory();
        saleLog.setUserId(username);
        saleLog.setFundId(fundId);
        saleLog.setType("SELL");
        saleLog.setAmount(proceeds);
        saleLog.setUnits(unitsToSell);
        saleLog.setStatus("SUCCESS");
        investmentHistoryRepository.save(saleLog);
    }

    // Updates the Portfolio summary for the user
    private void updateUserPortfolio(String username, String fundId, double addedUnits) {
        Portfolio portfolio = portfolioRepository.findByUserId(username)
                .orElseGet(() -> {
                    Portfolio newPortfolio = new Portfolio();
                    newPortfolio.setUserId(username);
                    return newPortfolio;
                });

        portfolio.updateHoldings(fundId, addedUnits);
        portfolioRepository.save(portfolio);
    }

    // Compares current value vs total invested 
    public double calculateReturns(String username) {
        Portfolio portfolio = portfolioRepository.findByUserId(username).orElse(new Portfolio());
        List<InvestmentHistory> history = investmentHistoryRepository.findByUserId(username);

        // 1. Calculate Total Cost Basis 
        double netInvestment = history.stream()
                .mapToDouble(h -> "BUY".equals(h.getType()) ? h.getAmount() : -h.getAmount())
                .sum();

        // 2. Calculate Current Market Value of all holdings
        double currentMarketValue = 0;
        if (portfolio.getFundHoldings() != null) {
            for (Map.Entry<String, Double> entry : portfolio.getFundHoldings().entrySet()) {
                String fundId = entry.getKey();
                Double unitsHeld = entry.getValue();

                Fund fund = fundRepository.findById(fundId).orElse(null);
                if (fund != null) {
                    currentMarketValue += (unitsHeld * fund.getPrice());
                }
            }
        }

        // Return: Current Value - Net Cost
        return currentMarketValue - netInvestment;
    }

    // Helper to get a fund by its position in the list (1-indexed)
    public Fund getFundByIndex(int index) {
        List<Fund> funds = getAllFunds();
        if (index < 1 || index > funds.size()) {
            throw new RuntimeException("Invalid selection. Please choose a number from the list.");
        }
        return funds.get(index - 1); // Convert 1-based menu to 0-based list
    }

    public String getFormattedFundsTable(String username) {
        List<Fund> funds = getAllFunds();
        if (funds.isEmpty()) {
            return "No funds currently available.";
        }

        // Get user portfolio to show owned units
        Portfolio portfolio = portfolioRepository.findByUserId(username).orElse(new Portfolio());

        StringBuilder sb = new StringBuilder();
        sb.append("\n----------------------------------------------------------------------------\n");
        sb.append(String.format("%-3s | %-6s | %-25s | %-8s | %-6s | %-10s%n", "#", "ID", "FUND NAME", "NAV (RM)", "RISK", "MY UNITS"));
        sb.append("----------------------------------------------------------------------------\n");
        for (int i = 0; i < funds.size(); i++) {
            Fund f = funds.get(i);
            double ownedUnits = portfolio.getUnitsForFund(f.getFundId());

            sb.append(String.format("%-3d | %-6s | %-25s | %8.4f | %-6s | %10.4f%n",
                    (i + 1), f.getFundId(), f.getName(), f.getPrice(), f.getRiskCategory(), ownedUnits));
        }
        sb.append("----------------------------------------------------------------------------");
        return sb.toString();
    }

    public String getFormattedHistory(String username) {
        List<InvestmentHistory> history = getUserInvestments(username);
        if (history.isEmpty()) {
            return "No transaction history found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(80)).append("\n");
        sb.append(String.format("%-22s | %-10s | %-12s | %-10s | %-10s%n", "DATE", "TYPE", "FUND ID", "UNITS", "AMOUNT"));
        sb.append("-".repeat(80)).append("\n");

        for (InvestmentHistory h : history) {
            String dateStr = (h.getTimestamp() != null) ? h.getTimestamp().toString() : "N/A";
            if (dateStr.length() > 19) {
                dateStr = dateStr.substring(0, 19);
            }

            String prefix = "BUY".equalsIgnoreCase(h.getType()) ? "-" : "+";
            String amountDisplay = String.format("%s RM %.2f", prefix, h.getAmount());

            sb.append(String.format("%-22s | %-10s | %-12s | %-10.4f | %12s%n",
                    dateStr, h.getType(), h.getFundId(), h.getUnits(), amountDisplay));
        }
        return sb.toString();
    }

    // Suggests funds based on a calculated score
    public String evaluateRiskProfile(String username, int score) {
        String profile;
        String suggestedRisk;

        if (score <= 3) {
            profile = "CONSERVATIVE";
            suggestedRisk = "Low";
        } else if (score <= 6) {
            profile = "MODERATE";
            suggestedRisk = "Medium";
        } else {
            profile = "AGGRESSIVE";
            suggestedRisk = "High";
        }

        Portfolio portfolio = portfolioRepository.findByUserId(username)
                .orElseGet(() -> {
                    Portfolio p = new Portfolio();
                    p.setUserId(username);
                    return p;
                });

        portfolio.setRiskCategory(profile); // Save risk result to portfolio
        portfolioRepository.save(portfolio);

        // Find a matching fund to suggest
        List<Fund> suggestions = fundRepository.findAll().stream()
                .filter(f -> f.getRiskCategory().equalsIgnoreCase(suggestedRisk))
                .toList();

        StringBuilder result = new StringBuilder();
        result.append("\nRisk Profile Saved!");
        result.append("\nYour Risk Profile: **").append(profile).append("**\n");
        result.append("Suggested Strategy: Look for ").append(suggestedRisk).append(" risk funds.\n");

        if (!suggestions.isEmpty()) {
            result.append("Recommended for you: ").append(suggestions.get(0).getName());
        }

        return result.toString();
    }

    public String getPortfolioSummary(String username) {
        Portfolio portfolio = portfolioRepository.findByUserId(username)
                .orElse(new Portfolio());

        double returns = calculateReturns(username);
        String status = (returns >= 0) ? "PROFIT" : "LOSS";
        String risk = (portfolio.getRiskCategory() != null) ? portfolio.getRiskCategory() : "Not Assessed (Take the quiz!)";

        StringBuilder sb = new StringBuilder();
        sb.append("\n==========================================\n");
        sb.append("         YOUR PORTFOLIO SUMMARY           \n");
        sb.append("==========================================\n");
        sb.append(String.format("User         : %s%n", username));
        sb.append(String.format("Risk Profile : %s%n", risk));
        sb.append("------------------------------------------\n");

        // List holdings if they exist
        Map<String, Double> holdings = portfolio.getFundHoldings();
        if (holdings == null || holdings.isEmpty()) {
            sb.append(String.format("Holdings     : No active investments.%n"));
        } else {
            sb.append("CURRENT HOLDINGS:\n");
            for (Map.Entry<String, Double> entry : holdings.entrySet()) {
                Fund f = fundRepository.findById(entry.getKey()).orElse(null);
                if (f != null && entry.getValue() > 0) {
                    sb.append(String.format(" - %-20s: %.4f units (RM %.2f)%n",
                            f.getName(), entry.getValue(), (entry.getValue() * f.getPrice())));
                }
            }
        }

        sb.append("------------------------------------------\n");
        sb.append(String.format("Net Performance : RM %.2f%n", returns));
        sb.append(String.format("Overall Status  : %s%n", status));
        sb.append("==========================================\n");

        return sb.toString();
    }

    public void simulateMarketChange() {
        List<Fund> funds = fundRepository.findAll();
        if (funds.isEmpty()) {
            System.out.println("No funds available to simulate.");
            return;
        }

        java.util.Random random = new java.util.Random();

        for (Fund fund : funds) {
            double volatility;

            switch (fund.getRiskCategory().toLowerCase()) {
                case "high":
                    volatility = 0.10;
                    break;
                case "medium":
                    volatility = 0.04;
                    break;
                case "low":
                    volatility = 0.01;
                    break;
                default:
                    volatility = 0.03;
            }

            double changePercent = (random.nextDouble() * 2 * volatility) - volatility;
            double oldPrice = fund.getPrice();
            double newPrice = oldPrice * (1 + changePercent);
            if (newPrice < 0.01) {
                newPrice = 0.01;
            }

            fund.setPrice(newPrice);
            fund.setNav(newPrice);
            fundRepository.save(fund);

            System.out.printf("[Market] %s: RM %.2f -> RM %.2f (%.2f%%)%n",
                    fund.getName(), oldPrice, newPrice, changePercent * 100);
        }
    }

    // Create new funds
    public Fund createFund(String id, String name, String description, String riskCategory, double price) {
        if (id != null && fundRepository.existsById(id)) {
            throw new RuntimeException("Validation Error: Fund ID '" + id + "' already exists.");
        }

        if (fundRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Validation Error: A fund named '" + name + "' already exists.");
        }

        List<String> riskCategories = Arrays.asList("Low", "Medium", "High");
        if (!riskCategories.contains(riskCategory)) {
            throw new RuntimeException("Validation Error: Risk Category must be Low/Medium/High");
        }

        Fund newFund = new Fund();
        if (id != null) {
            newFund.setFundId(id); // Only set if custom ID is provided

                }newFund.setName(name);
        newFund.setDescription(description);
        newFund.setRiskCategory(riskCategory);
        newFund.setPrice(price);
        newFund.setNav(price);

        return fundRepository.save(newFund);
    }

    // Delete funds
    public String deleteFund(int index) {
        Fund fund = getFundByIndex(index);
        String name = fund.getName();
        fundRepository.deleteById(fund.getFundId());
        return name;
    }
}
