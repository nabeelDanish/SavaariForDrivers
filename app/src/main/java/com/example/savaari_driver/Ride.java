package com.example.savaari_driver;

import com.google.android.gms.maps.model.LatLng;

public class Ride
{
    // Main Attributes
    int riderID;
    int rideStatus;
    String userName;
    LatLng pickupLocation;
    LatLng destinationLocation;

    // Main Methods
    public Ride()
    {
        // Empty Constructor
        riderID = -1;
        userName = "";
        pickupLocation = null;
        destinationLocation = null;
    }
    public Ride(int riderID, String userName, LatLng pickupLocation, LatLng destinationLocation)
    {
        this.riderID = riderID;
        this.userName = userName;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
    }

    // Getters and Setters
    public int getRiderID()
    {
        return riderID;
    }
    public void setRiderID(int riderID)
    {
        this.riderID = riderID;
    }
    public String getUserName()
    {
        return userName;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    public LatLng getPickupLocation()
    {
        return pickupLocation;
    }
    public void setPickupLocation(LatLng pickupLocation)
    {
        this.pickupLocation = pickupLocation;
    }
    public LatLng getDestinationLocation()
    {
        return destinationLocation;
    }
    public void setDestinationLocation(LatLng destinationLocation)
    {
        this.destinationLocation = destinationLocation;
    }
    public int getRideStatus() {
        return rideStatus;
    }
    public void setRideStatus(int rideStatus) {
        this.rideStatus = rideStatus;
    }
}
