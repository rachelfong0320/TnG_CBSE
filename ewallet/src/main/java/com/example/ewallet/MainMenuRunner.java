package com.example.ewallet;

import java.util.List;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.ewallet.entity.BasePolicy;
import com.example.ewallet.entity.Fund;
import com.example.ewallet.entity.InvestmentHistory;
import com.example.ewallet.entity.Portfolio;
import com.example.ewallet.entity.Wallet;
import com.example.ewallet.service.InsuranceService;
import com.example.ewallet.service.InvestmentService;
import com.example.ewallet.service.WalletService;

@Component
public class MainMenuRunner implements CommandLineRunner {

    private final WalletService walletService;
    private final InsuranceService insuranceService;
    private final InvestmentService investmentService;

    public MainMenuRunner(WalletService walletService, InsuranceService insuranceService, InvestmentService investmentService) {
        this.walletService = walletService;
        this.insuranceService = insuranceService;
        this.investmentService = investmentService;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            // Check wallet exists
            Wallet wallet = walletService.getWallet(username);
            if (wallet == null) {
                System.out.print("Welcome " + username + "! Enter initial balance (e.g., 100.00): RM");
                double balance = Double.parseDouble(scanner.nextLine());
                wallet = walletService.findOrCreateWallet(username, balance);
                System.out.println("User created successfully!");
            }

            boolean running = true;
            while (running) {
                System.out.println("\n=== MAIN MENU ===");
                double balance = walletService.getWallet(username).getBalance();
                System.out.printf("User: %s | Wallet Balance: RM %.2f%n", username, balance);
                System.out.println("1. Wallet Menu");
                System.out.println("2. GOprotect Dashboard (Insurance)");
                System.out.println("3. GOinvest Dashboard (Investment)");
                System.out.println("0. Exit");
                System.out.print("Select Option: ");

                String mainOption = scanner.nextLine();

                switch (mainOption) {
                    case "1":
                        walletMenu(scanner, username);
                        break;
                    case "2":
                        insuranceMenu(scanner, username);
                        break;
                    case "3":
                        investmentService.initSampleFunds();
                        investmentMenu(scanner, username);
                        break;
                    case "0":
                        running = false;
                        System.out.println("Exiting...");
                        System.out.println("Thank you for using the e-wallet!");
                        break;
                    default:
                        System.out.println("Invalid option, try again.");
                }
            }
        }
    }

    private void walletMenu(Scanner scanner, String username) {
        boolean inWallet = true;
        while (inWallet) {
            Wallet wallet = walletService.getWallet(username);
            System.out.println("\n--- WALLET MENU ---");
            System.out.printf("Current Balance: RM %.2f%n", wallet.getBalance());
            System.out.println("1. Add Funds");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select Option: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1":
                    System.out.print("Enter amount to add: RM ");
                    try {
                        double amount = Double.parseDouble(scanner.nextLine());
                        walletService.addFunds(username, amount);
                        System.out.println("Funds added successfully!");
                    } catch (Exception e) {
                        System.out.println("Invalid input, try again.");
                    }
                    break;

                case "0":
                    inWallet = false;
                    break;
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }

    private void insuranceMenu(Scanner scanner, String username) {
        boolean inInsurance = true;
        while (inInsurance) {
            double balance = walletService.getWallet(username).getBalance();
            System.out.printf("\n--- GOprotect DASHBOARD --- (Balance: RM %.2f)%n", balance);
            System.out.println("1. Buy Motor Insurance (RM 500)");
            System.out.println("2. Buy Travel Insurance (RM 80/pax)");
            System.out.println("3. View My Policies");
            System.out.println("4. Submit Claim");
            System.out.println("5. Check Claim Status");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select Option: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1":
                    System.out.print("Enter Car Plate: ");
                    String plate = scanner.nextLine();
                    insuranceService.purchaseMotorPolicy(username, plate, "Sedan");
                    break;
                case "2":
                    System.out.print("Enter Destination: ");
                    String dest = scanner.nextLine();
                    System.out.print("Enter Pax: ");
                    try {
                        int pax = Integer.parseInt(scanner.nextLine());
                        insuranceService.purchaseTravelPolicy(username, dest, pax);
                    } catch (Exception e) {
                        System.out.println("Invalid input, try again.");
                    }
                    break;
                case "3":
                    List<BasePolicy> policies = insuranceService.getPolicyList(username);
                    if (policies.isEmpty()) {
                        System.out.println("No policies found.");
                    } else {
                        for (BasePolicy p : policies) {
                            System.out.println("[" + p.getPolicyType() + "] ID: " + p.getPolicyId() + " | Status: "
                                    + p.getStatus());
                        }
                    }
                    break;
                case "4":
                    System.out.print("Enter Policy ID: ");
                    String pid = scanner.nextLine();
                    System.out.print("Enter Claim Amount: ");
                    try {
                        double amount = Double.parseDouble(scanner.nextLine());
                        insuranceService.submitClaim(pid, amount);
                    } catch (Exception e) {
                        System.out.println("Error processing claim.");
                    }
                    break;
                case "5":
                    System.out.print("Enter Claim ID: ");
                    String cid = scanner.nextLine();
                    System.out.println("Status: " + insuranceService.getClaimStatus(cid));
                    break;
                case "0":
                    inInsurance = false;
                    break;
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }

    private void investmentMenu(Scanner scanner, String username) {
        boolean inInvestment = true;
        while (inInvestment) {
            System.out.println("\nGOinvest DASHBOARD");
            System.out.println("1. View Available Funds");
            System.out.println("2. Invest in a Fund (Buy)");
            System.out.println("3. Sell Fund Units");
            System.out.println("4. View Investment History");
            System.out.println("5. View Portfolio & Returns");
            System.out.println("6. Take Risk Assessment Quiz");

            if (username.equalsIgnoreCase("admin")) {
                System.out.println("\n[ADMIN CONTROLS]");
                System.out.println("98. Delete Fund");
                System.out.println("99. Create Fund");
            }
            System.out.println("0. Back to Main Menu");
            System.out.print("Select Option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.println(investmentService.getFormattedFundsTable(username));
                    break;

                case "2": // BUY FLOW
                    System.out.println("\n--- SELECT A FUND TO BUY ---");
                    System.out.println(investmentService.getFormattedFundsTable(username));
                    System.out.print("Enter Fund Number (or 0 to cancel): ");
                    try {
                        int choice = Integer.parseInt(scanner.nextLine());
                        if (choice == 0) break;

                        Fund selected = investmentService.getFundByIndex(choice);
                        System.out.printf("Current Price for %s: RM %.2f%n", selected.getName(), selected.getPrice());
                        System.out.print("Enter Amount to Invest: RM ");
                        double amount = Double.parseDouble(scanner.nextLine());
                        
                        InvestmentHistory res = investmentService.investInFund(username, selected.getFundId(), amount);
                        System.out.printf("Success! You now own %.4f units of %s.%n", res.getUnits(), selected.getName());
                    } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
                    break;
                
                case "3": // SELL FLOW
                    System.out.println("\n--- SELECT A FUND TO SELL ---");
                    System.out.println(investmentService.getFormattedFundsTable(username));
                    System.out.print("Enter Fund Number to Sell (or 0 to cancel): ");
                    try {
                        int sChoice = Integer.parseInt(scanner.nextLine());
                        if (sChoice == 0) break;

                        Fund toSell = investmentService.getFundByIndex(sChoice);
                        
                        System.out.print("Enter Units to Sell: ");
                        double units = Double.parseDouble(scanner.nextLine());
                        
                        investmentService.sellFund(username, toSell.getFundId(), units);
                        System.out.println("Sale processed. Money credited to your wallet.");
                    } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
                    break;

                case "4": // HISTORY FLOW
                    System.out.println(investmentService.getFormattedHistory(username));
                    break;

                case "5":
                    String summary = investmentService.getPortfolioSummary(username);
                    System.out.println(summary);
                    break;

                case "6":
                    try {
                        Portfolio currentPortfolio = investmentService.getUserPortfolio(username);
                        if (currentPortfolio.getRiskCategory() != null) {
                            System.out.println("\nYour current saved profile is: " + currentPortfolio.getRiskCategory());
                            System.out.print("Would you like to retake the quiz? (Y/N): ");
                            if (!scanner.nextLine().equalsIgnoreCase("Y")) break;
                        }

                        System.out.println("\n--- RISK ASSESSMENT QUIZ ---");
                        int totalScore = 0;
                        String[] questions = {
                            "1. What is your investment goal?\n(1) Preserve Capital | (2) Balanced Growth | (3) Maximize Returns",
                            "2. How do you react if your investment drops 10%?\n(1) Sell everything | (2) Do nothing | (3) Buy more",
                            "3. What is your investment timeframe?\n(1) < 1 year | (2) 1-5 years | (3) 5+ years"
                        };

                        for (String q : questions) {
                            int choice = 0;
                            while (choice < 1 || choice > 3) {
                                System.out.println(q);
                                System.out.print("Your choice (1-3): ");
                                try {
                                    choice = Integer.parseInt(scanner.nextLine());
                                    if (choice < 1 || choice > 3) {
                                        System.out.println("Please enter only 1, 2, or 3.");
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid input. Please enter a number.");
                                }
                            }
                            totalScore += choice;
                        }

                        // Call service to get the result
                        String result = investmentService.evaluateRiskProfile(username, totalScore);
                        System.out.println(result);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input! Please enter numbers (1, 2, or 3) only.");
                    }
                    break;

                case "7": // Simulate Market
                    System.out.println("\n--- SIMULATING MARKET FLUCTUATIONS ---");
                    investmentService.simulateMarketChange();
                    System.out.println("Market prices updated! Check your Portfolio (Option 5) to see the impact.");
                break;

                case "98": // ADMIN DELETE
                    if (!username.equalsIgnoreCase("admin")) {
                        System.out.println("Unauthorized access.");
                        break;
                    }
                    System.out.println(investmentService.getFormattedFundsTable(username));
                    System.out.print("Enter Fund Number to delete: ");
                    try {
                        int delIndex = Integer.parseInt(scanner.nextLine());
                        String deletedName = investmentService.deleteFund(delIndex);
                        System.out.println("Fund '" + deletedName + "' deleted successfully.");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case "99": // ADMIN CREATE
                    try {
                        if (!username.equalsIgnoreCase("admin")) {
                            System.out.println("Unauthorized access.");
                            break;
                        }
                        System.out.print("Enter Fund ID: ");
                        String id = scanner.nextLine();
                        System.out.print("Enter Fund Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter Description: ");
                        String desc = scanner.nextLine();
                        System.out.print("Enter Risk Category (Low/Medium/High): ");
                        String risk = scanner.nextLine();
                        System.out.print("Enter Initial Price: RM ");
                        double price = Double.parseDouble(scanner.nextLine());
                        
                        investmentService.createFund(id, name, desc, risk, price);
                        System.out.println("Fund created successfully!");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case "0":
                    inInvestment = false;
                    break;

                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }
    
}
