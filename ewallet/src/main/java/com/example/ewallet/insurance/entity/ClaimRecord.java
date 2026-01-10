package com.example.ewallet.insurance.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "claim_records")
public class ClaimRecord {
    @Id
    private String claimId;
    private String policyId; // Link Specific policies
    private Date incidentDate;
    private double amount;
    private String status; // "Pending", "Approved"

    // Getter & Setter
    public String getClaimId() { return claimId; }
    public void setClaimId(String claimId) { this.claimId = claimId; }
    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }
    public Date getIncidentDate() { return incidentDate; }
    public void setIncidentDate(Date incidentDate) { this.incidentDate = incidentDate; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}