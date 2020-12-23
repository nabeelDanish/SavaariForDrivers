package com.example.savaari_driver.ride.adapter;

public class VehicleTypeItem
{
    // Main Attributes
    private int indexInArray;
    private int vehicleImage;
    private String vehicleMakeModel;
    private String vehicleRideType;

    // Constructors
    public VehicleTypeItem() {
        // Empty
    }
    public VehicleTypeItem(int indexInArray, int vehicleImage, String vehicleMakeModel, String vehicleRideType) {
        this.indexInArray = indexInArray;
        this.vehicleImage = vehicleImage;
        this.vehicleMakeModel = vehicleMakeModel;
        this.vehicleRideType = vehicleRideType;
    }

    // Getters and Setters
    public int getVehicleImage() {
        return vehicleImage;
    }
    public void setVehicleImage(int vehicleImage) {
        this.vehicleImage = vehicleImage;
    }
    public String getVehicleMakeModel() {
        return vehicleMakeModel;
    }
    public void setVehicleMakeModel(String vehicleMakeModel) {
        this.vehicleMakeModel = vehicleMakeModel;
    }
    public String getVehicleRideType() {
        return vehicleRideType;
    }
    public void setVehicleRideType(String vehicleRideType) {
        this.vehicleRideType = vehicleRideType;
    }
    public int getIndexInArray() {
        return indexInArray;
    }
    public void setIndexInArray(int indexInArray) {
        this.indexInArray = indexInArray;
    }
}
