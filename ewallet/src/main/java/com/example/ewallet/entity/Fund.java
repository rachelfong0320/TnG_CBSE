package com.example.ewallet.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "funds")
public class Fund {

    @Id
    private String fundId;
    private String name;
    private String description;
    private String riskCategory;
    private double nav; // Net Asset Value
    private double price;

    public Fund() {}

    public Fund(String name, String description, String riskCategory, double nav, double price) {
        this.name = name;
        this.description = description;
        this.riskCategory = riskCategory;
        this.nav = nav;
        this.price = price;
    }

    // Getters and Setters
    public String getFundId() { return fundId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getRiskCategory() { return riskCategory; }
    public double getNav() { return nav; }
    public double getPrice() { return price; }

    public void setFundId(String fundId) { this.fundId = fundId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }
    public void setNav(double nav) { this.nav = nav; }
    public void setPrice(double price) { this.price = price; }

    public double getLatestNAV() {
        return this.nav;
    }
}