package com.example.savaari_driver;

import com.example.savaari_driver.services.network.NetworkUtil;
import com.example.savaari_driver.services.network.OnDataLoadedListener;
import java.util.concurrent.Executor;

public class Repository
{
    // Main Attributes
    private final Executor executor;

    // Constructor
    Repository(Executor executor) {
        this.executor = executor;
    }

    // ---------------------------------------------------------------------------------------------
    //                                  NETWORK FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    // Sign-Up
    public void signup(OnDataLoadedListener callback, String nickname, String username, String password) {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.signup(nickname, username, password)));
    }
    // Login
    public void login(OnDataLoadedListener callback, String username, String password) {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.login(username, password)));
    }
    // Loading User Data
    public void loadUserData(OnDataLoadedListener callback, int currentUserID) {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.loadUserData(currentUserID)));
    }

    // Get User Locations
    public void getUserLocations(OnDataLoadedListener callback) {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.getUserLocations()));
    }

    // Send Last Location
    public void sendLastLocation(int currentUserID, double latitude, double longitude) {
        executor.execute(() ->
                NetworkUtil.sendLastLocation(currentUserID, latitude, longitude));
    }

    // Set Mark Active
    public void setMarkActive(OnDataLoadedListener callback, int userID, int active_status)
    {
        executor.execute(() ->
                callback.onDataLoaded(NetworkUtil.setMarkActive(userID, active_status)));
    }

    // Check ride status
    public void checkRideStatus(OnDataLoadedListener callback, int userID)
    {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.checkRideStatus(userID)));
    }

    // Confirm Ride Request
    public void confirmRideRequest(OnDataLoadedListener callback, int userID, int found_status, int riderID)
    {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.confirmRideRequest(userID, found_status, riderID)));
    }

    // Marking Arrival
    public void markArrival(OnDataLoadedListener callback, int rideID)
    {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.markArrival(rideID)));
    }

    // Starting Ride
    public void startRide(OnDataLoadedListener callback, int rideID)
    {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.startRide(rideID)));
    }

    // Marking Arrival at destination
    public void markDriverAtDestination(OnDataLoadedListener callback, int rideID, double dist_travelled, int driverID)
    {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.markDriverAtDestination(rideID, dist_travelled, driverID)));
    }

    // Ending Ride with Payment
    public void endRideWithPayment(OnDataLoadedListener callback, int rideID, double amntPaid, int driverID)
    {
        executor.execute(() -> callback.onDataLoaded(NetworkUtil.endRideWithPayment(rideID, amntPaid, driverID)));
    }
}
