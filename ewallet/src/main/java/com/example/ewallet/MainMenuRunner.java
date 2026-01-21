package com.example.ewallet;

import com.example.ewallet.entity.Wallet;
import com.example.ewallet.entity.AutoPayData;
import com.example.ewallet.entity.BasePolicy;
import com.example.ewallet.entity.PaymentData;
import com.example.ewallet.entity.QRData;
import com.example.ewallet.entity.User;
import com.example.ewallet.entity.Fund;
import com.example.ewallet.entity.InvestmentHistory;
import com.example.ewallet.entity.Portfolio;
import com.example.ewallet.service.InvestmentService;
import com.example.ewallet.service.InsuranceService;
import com.example.ewallet.service.PaymentService;
import com.example.ewallet.service.WalletService;
import com.example.ewallet.service.NotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

@Component
@Profile("!test")
public class MainMenuRunner implements CommandLineRunner {

    private final WalletService walletService;
    private final InsuranceService insuranceService;
    private final PaymentService paymentService;
    private final InvestmentService investmentService;
    private final NotificationService notificationService;

    public MainMenuRunner(WalletService walletService, InsuranceService insuranceService, PaymentService paymentService,
            InvestmentService investmentService, NotificationService notificationService) {
        this.walletService = walletService;
        this.insuranceService = insuranceService;
        this.paymentService = paymentService;
        this.investmentService = investmentService;
        this.notificationService = notificationService;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your phone number: ");
            String phoneNumber = scanner.nextLine();

            Wallet wallet = walletService.getWallet(phoneNumber);

            String username;
            if (wallet == null) {
                System.out.print("Enter your username: ");
                username = scanner.nextLine();
                System.out.print("Welcome " + username + "! Enter initial balance (e.g., 100.00): RM ");
                double balance = Double.parseDouble(scanner.nextLine());
                wallet = walletService.findOrCreateWallet(username, phoneNumber, balance);
                System.out.println("User created successfully!");
            } else {
                User user = walletService.getUserByPhoneNumber(phoneNumber);
                username = user.getUsername();
            }

            boolean running = true;
            while (running) {
                System.out.println("\n=== MAIN MENU ===");
                double balance = walletService.getWallet(phoneNumber).getBalance();
                long unreadCount = notificationService.getUnreadCount(phoneNumber);
                System.out.printf("User: %s | Wallet Balance: RM %.2f%n", username, balance);
                if (unreadCount > 0) {
                    System.out.printf("[!] You have %d unread notification(s)%n", unreadCount);
                }
                System.out.println("1. Wallet Menu");
                System.out.println("2. Payment Menu");
                System.out.println("3. GOprotect Dashboard (Insurance)");
                System.out.println("4. Investment Menu");
                System.out.println("5. Notifications");
                System.out.println("0. Exit");
                System.out.print("Select Option: ");

                String mainOption = scanner.nextLine();

                switch (mainOption) {
                    case "1":
                        walletMenu(scanner, phoneNumber, username);
                        break;
                    case "2":
                        paymentMenu(scanner, phoneNumber, username);
                        break;
                    case "3":
                        insuranceMenu(scanner, phoneNumber, username);
                        break;
                    case "4":
                        investmentService.initSampleFunds();
                        investmentMenu(scanner, phoneNumber, username);
                        break;
                    case "5":
                        notificationMenu(scanner, phoneNumber);
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

    private void walletMenu(Scanner scanner, String phoneNumber, String username) {
        boolean inWallet = true;
        while (inWallet) {
            Wallet wallet = walletService.getWallet(phoneNumber);
            System.out.println("\n--- WALLET MENU ---");
            System.out.printf("Current Balance: RM %.2f%n", wallet.getBalance());
            System.out.println("1. Add Money");
            System.out.println("2. Send Money");
            System.out.println("3. View Wallet Transaction History");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select Option: ");

            String option = scanner.nextLine();
            switch (option) {
                case "1":
                    System.out.print("Enter amount to add: RM ");
                    try {
                        double amount = Double.parseDouble(scanner.nextLine());
                        paymentService.processTopUp(phoneNumber, username, amount);
                        System.out.println("Money added successfully!");
                    } catch (Exception e) {
                        System.out.println("Invalid input, try again.");
                    }
                    break;

                case "2":
                    System.out.print("Enter recipient phone number: ");
                    String recipientPhone = scanner.nextLine().trim();

                    if (recipientPhone.equals(phoneNumber)) {
                        System.out.println("You cannot send money to yourself!");
                        break;
                    }

                    // Check if recipient exists
                    User recipientUser = walletService.getUserByPhoneNumber(recipientPhone);
                    if (recipientUser == null) {
                        System.out.println("Recipient not found!");
                        break;
                    }

                    System.out.print("Enter amount to send: RM ");
                    double amountToSend;
                    try {
                        amountToSend = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount, try again.");
                        break;
                    }

                    // Show confirmation to user
                    System.out.printf("Confirm sending RM %.2f to %s (%s)? (Y/N): ",
                            amountToSend, recipientUser.getUsername(), recipientPhone);
                    String confirm = scanner.nextLine().trim().toUpperCase();

                    if (confirm.equals("Y")) {
                        boolean success = walletService.sendMoney(phoneNumber, recipientPhone, amountToSend);
                        if (success) {
                            System.out.println("Money sent successfully!");
                        } else {
                            System.out.println("Failed to send money. Check your balance.");
                        }
                    } else {
                        System.out.println("Transaction cancelled.");
                    }
                    break;

                case "3":
                    System.out.println("\n--- TRANSACTION HISTORY ---");
                    if (wallet.getTransactions().isEmpty()) {
                        System.out.println("No transactions yet.");
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        for (Wallet.Transaction t : wallet.getTransactions()) {
                            System.out.printf("[%s] %s RM %.2f - %s%n",
                                    t.getTimestamp().format(formatter), t.getType(), t.getAmount(), t.getDescription());
                        }
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

    private void insuranceMenu(Scanner scanner, String phoneNumber, String username) {
        boolean inInsurance = true;
        while (inInsurance) {
            double balance = walletService.getWallet(phoneNumber).getBalance();
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
                    insuranceService.purchaseMotorPolicy(phoneNumber, username, plate, "Sedan");
                    break;
                case "2":
                    System.out.print("Enter Destination: ");
                    String dest = scanner.nextLine();
                    System.out.print("Enter Pax: ");
                    try {
                        int pax = Integer.parseInt(scanner.nextLine());
                        insuranceService.purchaseTravelPolicy(phoneNumber, username, dest, pax);
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
                        var newClaim = insuranceService.submitClaim(phoneNumber, pid, amount);
                        System.out.println("Claim Submitted Successfully (Pending Review).");
                        System.out.println("Your Claim ID is: " + newClaim.getClaimId());
                        System.out.println("(Please copy this ID to check status)");

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

    private void paymentMenu(Scanner scanner, String phoneNumber, String username) {
        boolean inPayment = true;
        while (inPayment) {
            double balance = walletService.getWallet(phoneNumber).getBalance();
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
                        paymentService.processPayment(phoneNumber, username, amount, merchant);
                    } catch (Exception e) {
                        System.out.println("Invalid amount.");
                    }
                    break;

                case "2": // QR Payment
                    System.out.println("Simulating QR Scan...");
                    System.out.print("Enter QR Data (Format: MERCHANT:AMOUNT): ");
                    // e.g., "Starbucks:15.50"
                    String qrData = scanner.nextLine();
                    paymentService.processQRPayment(phoneNumber, username, qrData);
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
                    paymentService.simulateAutoPayExecution(phoneNumber, username, month);
                    break;

                case "0":
                    inPayment = false;
                    break;
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }

    private void investmentMenu(Scanner scanner, String phoneNumber, String username) {
        boolean inInvestment = true;
        while (inInvestment) {
            System.out.println("\nInvestment Menu");
            System.out.println("1. View Available Investment Funds");
            System.out.println("2. Buy Fund Units");
            System.out.println("3. Sell Fund Units");
            System.out.println("4. View Investment Transaction History");
            System.out.println("5. View Portfolio & Returns");
            System.out.println("6. Take Risk Assessment Quiz");
            System.out.println("7. Simulate Market Fluctuations");
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
                        if (choice == 0)
                            break;

                        Fund selected = investmentService.getFundByIndex(choice);
                        System.out.printf("Current Price for %s: RM %.2f%n", selected.getName(), selected.getPrice());
                        System.out.print("Enter Amount to Invest: RM ");
                        double amount = Double.parseDouble(scanner.nextLine());

                        InvestmentHistory res = investmentService.investInFund(phoneNumber, username,
                                selected.getFundId(), amount);
                        System.out.printf("Success! You now own %.4f units of %s.%n", res.getUnits(),
                                selected.getName());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case "3": // SELL FLOW
                    System.out.println("\n--- SELECT A FUND TO SELL ---");
                    System.out.println(investmentService.getFormattedFundsTable(username));
                    System.out.print("Enter Fund Number to Sell (or 0 to cancel): ");
                    try {
                        int sChoice = Integer.parseInt(scanner.nextLine());
                        if (sChoice == 0)
                            break;

                        Fund toSell = investmentService.getFundByIndex(sChoice);

                        System.out.print("Enter Units to Sell: ");
                        double units = Double.parseDouble(scanner.nextLine());

                        investmentService.sellFund(phoneNumber, username, toSell.getFundId(), units);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
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
                            System.out
                                    .println("\nYour current saved profile is: " + currentPortfolio.getRiskCategory());
                            System.out.print("Would you like to retake the quiz? (Y/N): ");
                            if (!scanner.nextLine().equalsIgnoreCase("Y"))
                                break;
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
                    investmentService.simulateMarketChange(phoneNumber);
                    System.out.println("Market prices updated! Check your Portfolio (Option 5) to see the impact.");
                    break;

                case "0":
                    inInvestment = false;
                    break;

                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }

    private void notificationMenu(Scanner scanner, String phoneNumber) {
        boolean inNotifications = true;
        while (inNotifications) {
            long unreadCount = notificationService.getUnreadCount(phoneNumber);
            System.out.println("\n--- NOTIFICATION CENTER ---");
            System.out.printf("Unread Notifications: %d%n", unreadCount);
            System.out.println("1. View All Notifications");
            System.out.println("2. View Unread Notifications");
            System.out.println("3. Mark All as Read");
            System.out.println("0. Back to Main Menu");
            System.out.print("Select Option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    notificationService.displayNotifications(phoneNumber);
                    if (unreadCount > 0) {
                        System.out.print("\nMark all as read? (y/n): ");
                        String markRead = scanner.nextLine();
                        if (markRead.equalsIgnoreCase("y")) {
                            notificationService.markAllAsRead(phoneNumber);
                            System.out.println("All notifications marked as read.");
                        }
                    }
                    break;

                case "2":
                    var unreadNotifs = notificationService.getUnreadNotifications(phoneNumber);
                    System.out.println("\n=== UNREAD NOTIFICATIONS ===");
                    if (unreadNotifs.isEmpty()) {
                        System.out.println("No unread notifications.");
                    } else {
                        for (int i = 0; i < unreadNotifs.size(); i++) {
                            var notif = unreadNotifs.get(i);
                            System.out.printf("%d. [%s] %s%n", i + 1, notif.getType(),
                                    notif.getTimestamp());
                            System.out.printf("   %s%n", notif.getMessage());
                        }
                    }
                    break;

                case "3":
                    if (unreadCount > 0) {
                        notificationService.markAllAsRead(phoneNumber);
                        System.out.println("All notifications marked as read.");
                    } else {
                        System.out.println("No unread notifications to mark.");
                    }
                    break;

                case "0":
                    inNotifications = false;
                    break;

                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }

}
