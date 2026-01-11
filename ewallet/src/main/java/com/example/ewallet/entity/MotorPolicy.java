package com.example.ewallet.entity;

public class MotorPolicy extends BasePolicy {
    private String plateNumber;
    private String carModel;
    private double ncdRate;

    // Getter & Setter
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public double getNcdRate() { return ncdRate; }
    public void setNcdRate(double ncdRate) { this.ncdRate = ncdRate; }
}