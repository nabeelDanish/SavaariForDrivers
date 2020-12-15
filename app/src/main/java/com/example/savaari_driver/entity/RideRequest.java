package com.example.savaari_driver.entity;

public class RideRequest {

    // ----------------------------------------------------------------
    //                         MAIN ATTRIBUTES
    // ----------------------------------------------------------------
    protected Rider rider;
    protected Driver driver;
    protected Location pickupLocation;
    private String pickupTitle;
    protected Location dropoffLocation;
    private String dropoffTitle;
    protected int findStatus;

    // ----------------------------------------------------------------
    //                          MAIN METHODS
    // ----------------------------------------------------------------

    // Default Constructor
    public RideRequest() {

    }

    // Getters and Setters
    public Rider getRider() {
        return rider;
    }
    public void setRider(Rider rider) {
        this.rider = rider;
    }
    public Driver getDriver() {
        return driver;
    }
    public void setDriver(Driver driver) {
        this.driver = driver;
    }
    public Location getPickupLocation() {
        return pickupLocation;
    }
    public void setPickupLocation(Location pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
    public Location getDropoffLocation() {
        return dropoffLocation;
    }
    public void setDropoffLocation(Location dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }
    public String getPickupTitle() {
        return pickupTitle;
    }
    public void setPickupTitle(String pickupTitle) {
        this.pickupTitle = pickupTitle;
    }
    public String getDropoffTitle() {
        return dropoffTitle;
    }
    public void setDropoffTitle(String dropoffTitle) {
        this.dropoffTitle = dropoffTitle;
    }
    public int getFindStatus() {
        return findStatus;
    }
    public void setFindStatus(int findStatus) {
        this.findStatus = findStatus;
    }
}
