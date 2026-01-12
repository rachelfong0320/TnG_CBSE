package com.example.ewallet;

import com.example.ewallet.entity.Wallet;
import com.example.ewallet.entity.AutoPayData;
import com.example.ewallet.entity.BasePolicy;
import com.example.ewallet.entity.PaymentData;
import com.example.ewallet.entity.QRData;
import com.example.ewallet.service.InsuranceService;
import com.example.ewallet.service.PaymentService;
import com.example.ewallet.service.WalletService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class MainMenuRunner implements CommandLineRunner {

    private final WalletService walletService;
    private final InsuranceService insuranceService;
    private final PaymentService paymentService;    

    public MainMenuRunner(WalletService walletService, InsuranceService insuranceService, PaymentService paymentService) {
        this.walletService = walletService;
        this.insuranceService = insuranceService;
        this.paymentService = paymentService;
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
                System.out.println("2. Payment Menu");
                System.out.println("3. GOprotect Dashboard (Insurance)");
                System.out.println("0. Exit");
                System.out.print("Select Option: ");

                String mainOption = scanner.nextLine();

                switch (mainOption) {
                    case "1":
                        walletMenu(scanner, username);
                        break;
                    case "3":
                        insuranceMenu(scanner, username);
                        break;
                    case "2":
                        paymentMenu(scanner, username); 
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

    private void paymentMenu(Scanner scanner, String username) {
        boolean inPayment = true;
        while (inPayment) {
            double balance = walletService.getWallet(username).getBalance();
            System.out.printf("\n--- PAYMENT MENU --- (Balance: RM %.2f)%n", balance);
            System.out.println("1. Pay Merchant (Retail)");
            System.out.println("2. Scan QR Code");
            System.out.println("3. Setup AutoPay");
            System.out.println("4. View Payment History");
            System.out.println("5. Simulate Month End (Run AutoPay)");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select Option: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1": // Retail Payment
                    System.out.print("Enter Merchant Name: ");
                    String merchant = scanner.nextLine();
                    System.out.print("Enter Amount: RM ");
                    try {
                        double amount = Double.parseDouble(scanner.nextLine());
                        paymentService.processPayment(username, amount, merchant);
                    } catch (Exception e) {
                        System.out.println("Invalid amount.");
                    }
                    break;

                case "2": // QR Payment
                    System.out.println("Simulating QR Scan...");
                    System.out.print("Enter QR Data (Format: MERCHANT:AMOUNT): ");
                    // e.g., "Starbucks:15.50"
                    String qrData = scanner.nextLine();
                    paymentService.processQRPayment(username, qrData);
                    break;

                case "3": // AutoPay Setup
                    System.out.print("Enter Biller Name (e.g., TNB, Water): ");
                    String biller = scanner.nextLine();
            
                    System.out.print("Enter Auto-Deduct Amount: RM ");
                    double amount = 0;
                    try {
                        amount = Double.parseDouble(scanner.nextLine());
                    } catch (Exception e) {
                        System.out.println("Invalid amount.");
                    break;
                    }

                    System.out.print("Enter Billing Day (e.g., 5 for 5th of month): ");
                    int day = 1;
                    try {
                        day = Integer.parseInt(scanner.nextLine());
                    } catch (Exception e) {
                        System.out.println("Invalid day, defaulting to 1st.");
                    }
            
                    // Call the NEW setup method (No deduction happens here)
                    paymentService.setupAutoPay(username, biller, amount, day);
                    break;

                case "4": // History
                    System.out.println("\n--- RETAIL PAYMENTS ---");
                    List<PaymentData> retail = paymentService.getPaymentHistory(username);
                    for (PaymentData p : retail) {
                        System.out.printf("%s | RM %.2f | To: %s | %s%n", 
                                p.getDate(), p.getAmount(), p.getRecipientId(), p.getStatus());
                    }

                    System.out.println("\n--- QR PAYMENTS ---");
                    List<QRData> qr = paymentService.getQRHistory(username);
                    for (QRData q : qr) {
                        System.out.printf("%s | RM %.2f | To: %s | %s%n", 
                                q.getDate(), q.getAmount(), q.getRecipientId(), q.getStatus());
                    }

                    System.out.println("\n--- AUTOPAY LOGS ---");
                    List<AutoPayData> auto = paymentService.getAutoPayHistory(username);
                    for (AutoPayData a : auto) {
                        System.out.printf("%s | RM %.2f | To: %s | %s%n", 
                                a.getLastExecuted(), a.getAmount(), a.getRecipientId(), a.getStatus());
                    }
                    break;

                case "5": // Simulate Execution
                    System.out.print("Enter Current Month (e.g., December): ");
                    String month = scanner.nextLine();
                    // Triggers the deduction now
                    paymentService.simulateAutoPayExecution(username, month);
                    break;

                case "0":
                    inPayment = false;
                    break;
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }
}
