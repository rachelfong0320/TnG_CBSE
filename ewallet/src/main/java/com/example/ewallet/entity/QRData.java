package com.example.ewallet.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "qr_data")
public class QRData {
    @Id
    private String qrId;
    private String userId;
    private String qrString;   // The raw QR code content
    private String recipientId;
    private double amount;
    private Date date;
    private String status;

    public QRData() {}

    public QRData(String userId, String qrString, String recipientId, double amount, String status) {
        this.userId = userId;
        this.qrString = qrString;
        this.recipientId = recipientId;
        this.amount = amount;
        this.date = new Date();
        this.status = status;
    }

    // Getters and Setters
    public String getQrId() { return qrId; }
    public void setQrId(String qrId) { this.qrId = qrId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getQrString() { return qrString; }
    public void setQrString(String qrString) { this.qrString = qrString; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}