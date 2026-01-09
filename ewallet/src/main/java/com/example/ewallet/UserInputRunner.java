package com.example.ewallet;

import com.example.ewallet.entity.User;
import com.example.ewallet.service.WalletService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class UserInputRunner implements CommandLineRunner {

    private final WalletService walletService;

    public UserInputRunner(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public void run(String... args) {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            // Find user
            User user = walletService.findUser(username);

            if (user == null) {
                System.out.print("Welcome " + username + ", please enter initial balance (e.g., 100.00): RM");
                double balance = scanner.nextDouble();
                scanner.nextLine();

                user = walletService.createWallet(username, balance);
                System.out.println("User created successfully!");
            } else {
                System.out.println("Hello " + username + "!");
            }

            System.out.println("User: " + username);

            // Enquiry loop
            while (true) {
                System.out.printf("Balance: RM%.2f%n", user.getBalance());
                System.out.println("Enter -1 to exit, or any key to refresh balance:");

                if (scanner.nextLine().equals("-1")) {
                    System.out.println("Exiting... Thank you for using the e-wallet!");
                    break;
                }
            }
        }
    }
}
