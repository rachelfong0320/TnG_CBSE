// package com.example.ewallet.insurance;

// import com.example.ewallet.insurance.entity.BasePolicy;
// import com.example.ewallet.insurance.service.InsuranceService;
// import com.example.ewallet.service.WalletService;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.core.annotation.Order;
// import org.springframework.stereotype.Component;

// import java.util.List;
// import java.util.Scanner;

// @Component
// @Order(2)
// public class InsuranceTestRunner implements CommandLineRunner {

//     private final InsuranceService insuranceService;
//     private final WalletService walletService;

//     public InsuranceTestRunner(InsuranceService insuranceService, WalletService walletService) {
//         this.insuranceService = insuranceService;
//         this.walletService = walletService;
//     }

//     @Override
//     public void run(String... args) throws Exception {
//         Scanner scanner = new Scanner(System.in);

//         // The logic here is: after the wallet menu finishes, proceed to the insurance section.
//         System.out.println("\n\n[INSURANCE MODULE ACTIVATED]");

//         // Reconfirm identity (because these are two separate Runners, it's necessary to confirm who is operating them).
//         System.out.print(">> To access GOprotect, please confirm your username: ");
//         String username = scanner.nextLine();

//         // Check if this person is in your wallet (to verify if the account was successfully created with the team leader).
//         if (walletService.getWallet(username) == null) {
//             System.out.println("User not found! Please create account in Wallet menu first.");
//             return;
//         }

//         // Access insurance menu.
//         boolean active = true;
//         while (active) {
//             // Get the balance you just recharged in real time
//             double balance = walletService.getWallet(username).getBalance();
//             System.out.printf("GOprotect DASHBOARD (User: %s | Balance: RM %.2f)%n", username, balance);
//             System.out.println("1. Buy Motor Insurance (RM 500)");
//             System.out.println("2. Buy Travel Insurance (RM 80/pax)");
//             System.out.println("3. View My Policies");
//             System.out.println("4. Submit Claim");
//             System.out.println("5. Check Claim Status");
//             System.out.println("0. Finish & Exit");
//             System.out.print("Select Option: ");

//             String option = scanner.nextLine();

//             switch (option) {
//                 case "1":
//                     System.out.print("Enter Car Plate: ");
//                     String plate = scanner.nextLine();
//                     insuranceService.purchaseMotorPolicy(username, plate, "Sedan");
//                     break;
//                 case "2":
//                     System.out.print("Enter Destination: ");
//                     String dest = scanner.nextLine();
//                     System.out.print("Enter Pax: ");
//                     try {
//                         int pax = Integer.parseInt(scanner.nextLine());
//                         insuranceService.purchaseTravelPolicy(username, dest, pax);
//                     } catch (Exception e) {
//                         System.out.println("Invalid input");
//                     }
//                     break;
//                 case "3":
//                     List<BasePolicy> policies = insuranceService.getPolicyList(username);
//                     if (policies.isEmpty()) System.out.println("No policies found.");
//                     for (BasePolicy p : policies) {
//                         System.out.println("[" + p.getPolicyType() + "] ID: " + p.getPolicyId() + " | Status: " + p.getStatus());
//                     }
//                     break;
//                 case "4":
//                     System.out.print("Enter Policy ID: ");
//                     String pid = scanner.nextLine();
//                     System.out.print("Enter Claim Amount: ");
//                     try {
//                         double amount = Double.parseDouble(scanner.nextLine());
//                         insuranceService.submitClaim(pid, amount);
//                     } catch (Exception e) { System.out.println("Error"); }
//                     break;
//                 case "5":
//                     System.out.print("Enter Claim ID: ");
//                     String cid = scanner.nextLine();
//                     System.out.println("Status: " + insuranceService.getClaimStatus(cid));
//                     break;
//                 case "0":
//                     active = false;
//                     System.out.println("Exiting Insurance Module. See you!");
//                     break;
//                 default:
//                     System.out.println("Invalid option");
//             }
//         }
//     }
// }