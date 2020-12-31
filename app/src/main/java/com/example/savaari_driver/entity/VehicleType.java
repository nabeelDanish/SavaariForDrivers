package com.example.savaari_driver.entity;

public class VehicleType {
    private static final int DEFAULT_ID = -1;

    // Main Attributes
    private int vehicleTypeID;
    private String make;
    private String model;
    private String year;
    private RideType rideType;

    // Main Constructor
    public VehicleType() {
        // Empty
    }

    public VehicleType(int vehicleTypeID) {
        this.vehicleTypeID = vehicleTypeID;
    }

    public VehicleType(int vehicleTypeID, String make, String model, String year) {
        this.vehicleTypeID = vehicleTypeID;
        this.make = make;
        this.model = model;
        this.year = year;
    }

    // Getters and Setters
    public int getVehicleTypeID() {
        return vehicleTypeID;
    }
    public void setVehicleTypeID(int vehicleTypeID) {
        this.vehicleTypeID = vehicleTypeID;
    }
    public String getMake() {
        return make;
    }
    public void setMake(String make) {
        this.make = make;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getYear() {
        return year;
    }
    public void setYear(String year) {
        this.year = year;
    }
    public RideType getRideType() {
        return rideType;
    }
    public void setRideType(RideType rideType) {
        this.rideType = rideType;
    }
}
