package com.example.ewallet.insurance.entity;

import java.util.Date;

public class TravelPolicy extends BasePolicy {
    private String destination;
    private Date startDate;
    private int travelPax;

    // Getter & Setter
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public int getTravelPax() { return travelPax; }
    public void setTravelPax(int travelPax) { this.travelPax = travelPax; }
}