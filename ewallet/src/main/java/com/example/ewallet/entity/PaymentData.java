package com.example.ewallet.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "payment_data")
public class PaymentData {
    @Id
    private String paymentId;
    private String userId;
    private double amount;
    private Date date;
    private String status;     // "SUCCESS", "FAILED"
    private String recipientId; // Merchant Name

    public PaymentData() {}

    public PaymentData(String userId, double amount, String recipientId, String status) {
        this.userId = userId;
        this.amount = amount;
        this.date = new Date();
        this.recipientId = recipientId;
        this.status = status;
    }

    // Getters and Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
}