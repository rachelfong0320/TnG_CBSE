package com.example.ewallet.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "portfolios")
public class Portfolio {

    @Id
    private String portfolioId;
    private String userId;
    private String riskCategory;
    private double totalUnits;
    private double totalValue;
    private double totalReturns;
    private Map<String, Double> fundHoldings;

    public Portfolio() {
        this.fundHoldings = new HashMap<>();
    }

    public Map<String, Double> getFundHoldings() { 
        if (this.fundHoldings == null) {
            this.fundHoldings = new HashMap<>();
        }
        return fundHoldings; 
    }

    public double getUnitsForFund(String fundId) {
        return getFundHoldings().getOrDefault(fundId, 0.0);
    }

    // Helper to update holdings
    public void updateHoldings(String fundId, double unitChange) {
        Map<String, Double> holdings = getFundHoldings(); 

        double currentUnits = getUnitsForFund(fundId);
        double newUnits = currentUnits + unitChange;        
        holdings.put(fundId, newUnits);
        
        this.totalUnits = holdings.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public void calculateTotalValue(double currentNav) {
        this.totalValue = this.totalUnits * currentNav;
    }

    // Getters and Setters
    public String getPortfolioId() { return portfolioId; }
    public String getUserId() { return userId; }
    public String getRiskCategory() { return riskCategory; }
    public double getTotalUnits() { return totalUnits; }
    public double getTotalValue() { return totalValue; }
    public double getTotalReturns() { return totalReturns; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setTotalUnits(double totalUnits) { this.totalUnits = totalUnits; }
    public void setRiskCategory(String riskCategory) { this.riskCategory = riskCategory; }
    public void setFundHoldings(Map<String, Double> fundHoldings) { this.fundHoldings = fundHoldings; }

}