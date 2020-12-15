package com.example.savaari_driver.entity;

import com.google.android.gms.maps.model.LatLng;

public class Driver extends User
{
    // ----------------------------------------------------------
    //                      Main Attributes
    // ----------------------------------------------------------
    private static final String LOG_TAG = Driver.class.getSimpleName();
    private Boolean active;
    private Boolean takingRide;
    private int rideRequestStatus;

    // ----------------------------------------------------------
    //                        Main Methods
    // ----------------------------------------------------------
    public Driver() {
        reset();
    }
    public Driver(int userID, String name, LatLng currentLocation) {
        Initialize(userID, name, currentLocation);
    }
    public void Initialize(int userID, String name, LatLng currentLocation) {
        this.userID = userID;
        this.username = name;
        this.currentLocation.setLatLng(currentLocation);
    }
    public void reset() {
        userID = -1;
        username = "";
        currentLocation = new Location();
    }

    // Getters and Setters
    public Boolean isActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    public Boolean getTakingRide() {
        return takingRide;
    }
    public void isTakingRide(Boolean takingRide) {
        this.takingRide = takingRide;
    }
    public int getRideRequestStatus() {
        return rideRequestStatus;
    }
    public void setRideRequestStatus(int rideRequestStatus) {
        this.rideRequestStatus = rideRequestStatus;
    }
}
