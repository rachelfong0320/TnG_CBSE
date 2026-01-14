package com.example.ewallet.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Document(collection = "wallets")
public class Wallet {

    @Id
    private String id;
    private String userId;
    private double balance;

    // Wallet's transaction history
    private List<Transaction> transactions = new ArrayList<>();

    public Wallet() {
    }

    public Wallet(String userId, double initialBalance) {
        this.userId = userId;
        this.balance = initialBalance;
        this.transactions = new ArrayList<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Transaction helper
    public void addTransaction(String type, double amount, String description) {
        Transaction t = new Transaction(type, amount, description, LocalDateTime.now());
        transactions.add(t);
    }

    // Inner Transaction class
    public static class Transaction {
        private String type;
        private double amount;
        private String description;
        private LocalDateTime timestamp;

        public Transaction() {
        }

        public Transaction(String type, double amount, String description, LocalDateTime timestamp) {
            this.type = type;
            this.amount = amount;
            this.description = description;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getType() {
            return type;
        }

        public double getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
