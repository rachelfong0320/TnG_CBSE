package com.example.ewallet.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "investments")
public class InvestmentHistory {

    @Id
    private String investmentHistoryId;
    private String fundId;
    private String userId; // Linking to the User
    private double amount;
    private double units;
    private String status;
    private Date timestamp;

    public InvestmentHistory() {
        this.timestamp = new Date();
    }

    public double calculateUnits(double price) {
        if (price <= 0) return 0;
        this.units = this.amount / price;
        return this.units;
    }

    // Getters and Setters
    public String getInvestmentHistoryId() { return investmentHistoryId; }
    public String getFundId() { return fundId; }
    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
    public double getUnits() { return units; }
    public String getStatus() { return status; }
    public Date getTimestamp() { return timestamp; }

    public void setFundId(String fundId) { this.fundId = fundId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setUnits(double units) { this.units = units; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

}