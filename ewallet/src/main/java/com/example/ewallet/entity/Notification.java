package com.example.ewallet.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    private String userId;
    private String phoneNumber;
    private String username;
    private String type; // PAYMENT, WALLET, INVESTMENT, AUTOPAY, QR, FUND, CLAIM
    private String message;
    private LocalDateTime timestamp;
    private boolean read;

    public Notification() {
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    public Notification(String userId, String username, String phoneNumber, String type, String message) {
        this.userId = userId;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.type = type;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }
}
