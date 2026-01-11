package com.example.ewallet;

import com.example.ewallet.entity.Wallet;
import com.example.ewallet.insurance.entity.BasePolicy;
import com.example.ewallet.insurance.service.InsuranceService;
import com.example.ewallet.service.WalletService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class MainMenuRunner implements CommandLineRunner {

    private final WalletService walletService;
    private final InsuranceService insuranceService;

    public MainMenuRunner(WalletService walletService, InsuranceService insuranceService) {
        this.walletService = walletService;
        this.insuranceService = insuranceService;
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
                System.out.printf("User: %s | Wallet Balance: RM %.2f%n", username, wallet.getBalance());
                System.out.println("1. Wallet Menu");
                System.out.println("2. GOprotect Dashboard (Insurance)");
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
}
