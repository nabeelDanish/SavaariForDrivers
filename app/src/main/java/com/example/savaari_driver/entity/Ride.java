package com.example.savaari_driver.entity;

import java.util.ArrayList;

public class Ride extends RideRequest {
    public static final int
            PICKUP = 11,
            DRIVER_ARRIVED = 12,
            CANCELLED = 13,
            STARTED = 14,
            NEAR_DROPFF = 19,
            TAKE_PAYMENT = 15,
            COMPLETED = 20;

    public static final int
            DEFAULT = 0,
            PAIRED = 1,
            ALREADY_PAIRED = 2,
            NOT_PAIRED = 3,
            NOT_FOUND = 4,
            STATUS_ERROR = 5;

    // -------------------------------------------------------------------------
    //                              MAIN ATTRIBUTES
    // -------------------------------------------------------------------------
    private int rideID;
    private Vehicle vehicle;
    private Payment payment;
    private long startTime;
    private long endTime;
    private double distanceTravelled;
    private double estimatedFare;
    private double fare;
    private int rideStatus;
    private int findStatus;
    private ArrayList<Location> stops;

    // --------------------------------------------------------------------------
    //                              MAIN METHODS
    // --------------------------------------------------------------------------
    public Ride() {
        rider = new Rider();
        driver = new Driver();
        payment = new Payment();
        pickupLocation = null;
        dropoffLocation = null;
        stops = new ArrayList<>();
        findStatus = DEFAULT;
    }

    // Getters and Setters
    public int getRideID() {
        return rideID;
    }
    public void setRideID(int rideID) {
        this.rideID = rideID;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    public Payment getPayment() {
        return payment;
    }
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    public double getDistanceTravelled() {
        return distanceTravelled;
    }
    public void setDistanceTravelled(double distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }
    public double getEstimatedFare() {
        return estimatedFare;
    }
    public void setEstimatedFare(double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }
    public double getFare() {
        return fare;
    }
    public void setFare(double fare) {
        this.fare = fare;
    }
    public int getRideStatus() {
        return rideStatus;
    }
    public void setRideStatus(int rideStatus) {
        this.rideStatus = rideStatus;
    }
    public int getFindStatus() {
        return findStatus;
    }
    public void setFindStatus(int findStatus) {
        this.findStatus = findStatus;
    }
    public ArrayList<Location> getStops() {
        return stops;
    }
    public void setStops(ArrayList<Location> stops) {
        this.stops = stops;
    }
}
