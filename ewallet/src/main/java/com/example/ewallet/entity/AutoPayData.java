package com.example.ewallet.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "autopay_data")
public class AutoPayData {
    @Id
    private String autoPayId;
    private String userId;
    private String recipientId; 
    private double amount;
    private int billingDay;     
    private Date lastExecuted;
    private String status;     

    public AutoPayData() {}

    public AutoPayData(String userId, String recipientId, double amount, int billingDay) {
        this.userId = userId;
        this.recipientId = recipientId;
        this.amount = amount;
        this.billingDay = billingDay;
        this.status = "Active";
        // lastExecuted is null initially because it hasn't run yet
    }

    // Getters and Setters
    public String getAutoPayId() { return autoPayId; }
    public void setAutoPayId(String autoPayId) { this.autoPayId = autoPayId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public int getBillingDay() { return billingDay; }
    public void setBillingDay(int billingDay) { this.billingDay = billingDay; }
    
    public Date getLastExecuted() { return lastExecuted; }
    public void setLastExecuted(Date lastExecuted) { this.lastExecuted = lastExecuted; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}