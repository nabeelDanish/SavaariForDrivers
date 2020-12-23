package com.example.savaari_driver.ride;

// Imports

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.savaari_driver.Repository;
import com.example.savaari_driver.Util;
import com.example.savaari_driver.entity.Driver;
import com.example.savaari_driver.entity.Location;
import com.example.savaari_driver.entity.Payment;
import com.example.savaari_driver.entity.Ride;
import com.example.savaari_driver.entity.RideRequest;
import com.example.savaari_driver.entity.Vehicle;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

// Class Declaration
public class RideViewModel extends ViewModel {

    // ---------------------------------------------------------------------------------------------
    //                                      MAIN ATTRIBUTES
    // ---------------------------------------------------------------------------------------------
    // Log Tag
    private static final String LOG_TAG = RideViewModel.class.getSimpleName();

    /* Main Data Objects */
    private Driver driver;
    private RideRequest rideRequest;
    private Ride ride;
    private ArrayList<Location> mUserLocations = new ArrayList<>();
    private Location currentLocation;

    // Data Repository
    private final Repository repository;

    /* Status Flags */
    private final MutableLiveData<Integer> IS_TAKING_RIDE = new MutableLiveData<Integer>(0);
    private final MutableLiveData<Boolean> userDataLoaded = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userLocationsLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> markedActive = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> rideFound = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> rideStatus = new MutableLiveData<>(Ride.DEFAULT);
    private final MutableLiveData<Boolean> nearPickup = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> vehicleSelected = new MutableLiveData<>();
    private boolean locationLoaded = false;
    private boolean matchmakingStarted = false;

    // ---------------------------------------------------------------------------------------------
    //                                      MAIN METHODS
    // ---------------------------------------------------------------------------------------------
    // Main Constructor
    public RideViewModel(int USER_ID, Repository repository)
    {
        currentLocation = new Location();
        this.repository = repository;
        driver = new Driver();
        driver.setUserID(USER_ID);
//        driver = repository.getDriver();
//        if (driver == null) {
//            driver = new Driver();
//            driver.setUserID(USER_ID);
//            userDataLoaded.setValue(false);
//        } else {
//            userDataLoaded.setValue(true);
//        }
    }

    // Getters and Setters
    public Driver getDriver() {
        return driver;
    }
    public void setDriver(Driver driver) {
        this.driver = driver;
    }
    public ArrayList<Location> getUserLocations() {
        return mUserLocations;
    }
    public Ride getRide()
    {
        return ride;
    }
    public void setRide(Ride ride)
    {
        this.ride = ride;
    }
    public void setRideStatus(int rideStatus)
    {
        this.rideStatus.setValue(rideStatus);
    }
    public void setRideFound(boolean foundRide) {
        this.rideFound.setValue(foundRide);
    }
    public RideRequest getRideRequest() {
        return rideRequest;
    }
    public void setRideRequest(RideRequest rideRequest) {
        this.rideRequest = rideRequest;
    }
    public boolean isLocationLoaded() {
        return locationLoaded;
    }
    public void setLocationLoaded(boolean locationLoaded) {
        this.locationLoaded = locationLoaded;
    }

    /* Return LiveData to observe Data Loaded Flags */
    public LiveData<Boolean> isLiveUserDataLoaded() {
        return userDataLoaded;
    }
    public LiveData<Boolean> isLiveUserLocationsLoaded() { return userLocationsLoaded; }
    public LiveData<Boolean> isMarkedActive() {
        return markedActive;
    }
    public LiveData<Boolean> isRideFound() { return rideFound; }
    public LiveData<Integer> RideStatus() { return rideStatus; }
    public LiveData<Integer> getIsTakingRide()
    {
        return IS_TAKING_RIDE;
    }
    public LiveData<Boolean> getNearPickup() {
        return nearPickup;
    }
    public LiveData<Integer> getVehicleSelected() {
        return vehicleSelected;
    }

    // Setting LiveData flags
    public void setIsTakingRide(Integer IS_TAKING_RIDE) { this.IS_TAKING_RIDE.setValue(IS_TAKING_RIDE); }

    // ---------------------------------------------------------------------------------------------
    //                                   LOCATION, DATA, OTHER
    // ---------------------------------------------------------------------------------------------

    // Function to load user data
    public void loadUserData()
    {
        repository.loadUserData(this::onUserDataLoaded, driver.getUserID());
    }
    // Function on User Data Loaded
    public void onUserDataLoaded(Object r) {
        try {
            if (r == null) {
                Log.d(LOG_TAG, "onDataLoaded(): resultString is null");
                userDataLoaded.postValue(false);
            } else {
                driver = (Driver) r;
                driver.setCurrentLocation(currentLocation);
                Log.d("loadUserData(): ", driver.getUsername() + ", " + driver.getEmailAddress());
                userDataLoaded.postValue(true);
                repository.setDriver(driver);
                markedActive.postValue(driver.isActive());
            }
        } catch (Exception e) {
            e.printStackTrace();
            userDataLoaded.postValue(false);
            Log.d(LOG_TAG, "onDataLoaded(): exception thrown");
        }
    }
    // Function to load user locations
    public void loadUserLocations()
    {
        if (!userLocationsLoaded.getValue())
            repository.getUserLocations(this::onUserLocationsLoaded);
    }
    // Function on User Locations Loaded
    public void onUserLocationsLoaded(Object r)
    {
        try {
            if (r == null) {
                Log.d(TAG, "onUserLocationsLoaded(): resultString is null");
                userLocationsLoaded.postValue(false);
            }
            else {
                mUserLocations = (ArrayList<Location>) r;
                Log.d(TAG, "loadUserLocations: found JSON Array");
                userLocationsLoaded.postValue(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onUserLocationsLoaded(): Exception thrown!");
            userLocationsLoaded.postValue(false);
        }
    }
    // End of Function: onUserLocationsLoaded();

    /* Need a setter since coordinates are received from activity */
    public void setUserCoordinates(double latitude, double longitude) {
        // Check if the Distance is near Pickup
        try {
            if (IS_TAKING_RIDE.getValue() == 1)
            {
                switch (rideStatus.getValue())
                {
                    case Ride.PICKUP:
                    {
                        // Calculating distance to pickup location
                        driver.getCurrentLocation().setLatLng(new LatLng(latitude, longitude));
                        double distance = Util.distance(latitude, longitude, ride.getPickupLocation().getLatitude(), ride.getPickupLocation().getLongitude());
                        Log.d(TAG, "setUserCoordinates: distance to pickup = " + distance);
                        if (distance <= 100) {
                            Log.d(LOG_TAG, "setUserCoordinates(): setting near pickup to true");
                            nearPickup.setValue(true);
                        }
                        break;
                    }
                    case Ride.STARTED:
                    {
                        // Calculating distance from the previous location to update route
                        double distance = Util.distance(latitude, longitude, driver.getCurrentLocation().getLatitude(), driver.getCurrentLocation().getLongitude());
                        driver.getCurrentLocation().setLatLng(new LatLng(latitude, longitude));
                        ride.setDistanceTravelled(ride.getDistanceTravelled() + distance);
                        Log.d(LOG_TAG, "setUserCoordinates(): Distance Travelled = " + ride.getDistanceTravelled());

                        // Check if Near Drop-off Location
                        distance = Util.distance(latitude, longitude, ride.getDropoffLocation().getLatitude(), ride.getDropoffLocation().getLongitude());
                        if (distance <= 100) {
                            Log.d(LOG_TAG, "setUserCoordinates(): near drop-off location");
                            rideStatus.setValue(Ride.NEAR_DROPFF);
                        }
                        break;
                    }
                }
            }
            else
            {
                if (driver.getCurrentLocation() != null) {
                    driver.getCurrentLocation().setLatLng(new LatLng(latitude, longitude));
                } else {
                    Location location = new Location();
                    location.setLatLng(new LatLng(latitude, longitude));
                    driver.setCurrentLocation(location);
                    currentLocation = driver.getCurrentLocation();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Selecting a Vehicle
    public void selectVehicle(int position)
    {
        // Getting the Vehicle
        Vehicle vehicle = driver.getVehicles().get(position);
        Log.d(TAG, "selectVehicle: called for Vehicle = " + vehicle.getModel());
        repository.selectActiveVehicle(object -> {
            try {
                if (object != null) {
                    boolean aBoolean = (boolean) object;
                    if (aBoolean) {
                        Log.d(TAG, "selectVehicle: Vehicle Selected!");
                        vehicleSelected.postValue(vehicle.getVehicleID());
                    } else {
                        vehicleSelected.postValue(-1);
                    }
                } else {
                    vehicleSelected.postValue(-1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                vehicleSelected.postValue(-1);
            }
        }, driver.getUserID(), vehicle.getVehicleID());
    }

    // ---------------------------------------------------------------------------------------------
    //                                   MATCHMAKING
    // ---------------------------------------------------------------------------------------------
    // Function using the repository
    public void checkRideStatus()
    {
        Log.d(LOG_TAG, "getRideStatus(): Called!");
        repository.checkRideStatus(object -> {
            try {
                if (object != null) {
                    ride = (Ride) object;
                    IS_TAKING_RIDE.postValue(1);
                    rideStatus.postValue(ride.getRideStatus());
                }
                else {
                    Log.d(LOG_TAG, "getRideStatus(): object is null!");
                    IS_TAKING_RIDE.postValue(0);
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "getRideStatus(): Exception Thrown!");
                e.printStackTrace();
                IS_TAKING_RIDE.postValue(0);
            }
        }, driver.getUserID(), rideRequest.getRider().getUserID());
    }
    public void setMarkActive(int activeStatus)
    {
        repository.setMarkActive(object -> {
            try {
                if (object != null) {
                    boolean aBoolean = (boolean) object;
                    if (aBoolean) {
                        if (activeStatus == 1)
                        {
                            Log.d(LOG_TAG, "setMarkActive(): Marked Active!");
                            markedActive.postValue(true);
                        } else {
                            Log.d(LOG_TAG, "setMarkActive(): Marked DeActive!");
                            vehicleSelected.postValue(-1);
                            markedActive.postValue(false);
                        }
                    } else {
                        Log.d(LOG_TAG, "setMarkActive(): Marked Active failed!");
                        // markedActive.postValue(false);
                    }
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "setMarkActive(): Error! Exception Thrown");
                e.printStackTrace();
                // markedActive.postValue(false);
            }
        }, driver.getUserID(), activeStatus);
    }

    public void checkRideRequestStatus()
    {
        repository.checkRideRequestStatus(object -> {
            try {
                if (object != null) {
                    rideRequest = (RideRequest) object;
                    if (rideRequest.getFindStatus() == RideRequest.MS_REQ_RECEIVED) {
                        rideFound.postValue(true);
                    } else if(rideRequest.getFindStatus() == RideRequest.MS_REQ_ACCEPTED) {
                        checkRideStatus();
                    }
                } else {
                    Log.d(TAG, "checkRideRequestStatus: object was null");
                }
            } catch (Exception e) {
                e.printStackTrace();
                rideFound.postValue(false);
            }
        }, driver.getUserID());
    }

    public void startMatchMaking() {
        // Start Matchmaking
        if (!matchmakingStarted)
        {
            Log.d(TAG, "startMatchMaking: Started!");
            matchmakingStarted = true;
            repository.startMatchmaking(object -> {
            try {
                    if (object != null) {
                        rideRequest = (RideRequest) object;
                        rideFound.postValue(true);
                    } else {
                        Log.d(TAG, "startMatchMaking: found null");
                        rideFound.postValue(false);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "startMatchMaking: Exception thrown!");
                    e.printStackTrace();
                    rideFound.postValue(false);
                }
                matchmakingStarted = false;
            }, driver.getUserID());
        }
    }
    public void confirmRideRequest(int found_status)
    {
        rideRequest.setDriver(driver);
        repository.confirmRideRequest(object -> {
            if (object != null) {
                boolean aBoolean = (boolean) object;
                if (aBoolean && found_status == 1) {
                    rideFound.postValue(false);
                    checkRideStatus();
                } else {
                    rideFound.postValue(false);
                }
            } else {
                rideFound.postValue(false);
                Log.d(TAG, "confirmRideRequest: object was null!");
            }
        },driver.getUserID(), found_status, rideRequest.getRider().getUserID());
    }

    public void markArrival()
    {
        Log.d(LOG_TAG, "markArrival() called");
        repository.markArrival(object -> {
            try {
                if (object != null) {
                    boolean aBoolean = (boolean) object;
                    Log.d(LOG_TAG, "markArrival(): jsonObject = " + aBoolean);
                    if (aBoolean) {
                        rideStatus.postValue(Ride.DRIVER_ARRIVED);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, ride.getRideID());
    }

    public void startRide()
    {
        Log.d(LOG_TAG, "startRide() called");
        repository.startRide(object -> {
            try {
                if (object != null) {
                    boolean aBoolean = (boolean) object;
                    Log.d(LOG_TAG, "startRide(): jsonObject = " + aBoolean);
                    if (aBoolean) {
                        rideStatus.postValue(Ride.STARTED);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ride.getRideID());
    }
    public void markDriverAtDestination()
    {
        Log.d(LOG_TAG, "endRide() called!");
        repository.markDriverAtDestination(object -> {
            try {
                if (object != null) {
                    double fare = (double) object;
                    Log.d(LOG_TAG, "endRide(): jsonObject = " + fare);
                    if (fare > 0) {
                        ride.setFare(fare);
                        rideStatus.postValue(Ride.TAKE_PAYMENT);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ride.getRideID(), ride.getDistanceTravelled(), driver.getUserID());
    }

    public void endRideWithPayment(double amountPaid) {
        Log.d(LOG_TAG, "endRideWithPayment() called!");

        // Storing Payment
        ride.setPayment(new Payment());
        ride.getPayment().setAmountPaid(amountPaid);
        ride.getPayment().setPaymentMode(rideRequest.getPaymentMethod());
        ride.getPayment().setChange(ride.getFare() - amountPaid);

        // Calling Network Service
        repository.endRideWithPayment(object -> {
            try {
                if (object != null) {
                    boolean aBoolean = (boolean) object;
                    Log.d(TAG, "endRideWithPayment: jsonObject = " + aBoolean);
                    if (aBoolean) {
                        resetFlags();
                    }
                }
                else {
                    Log.d(TAG, "endRideWithPayment: object is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ride.getRideID(), ride.getPayment(), driver.getUserID());
    }

    // ---------------------------------------------------------------------------------------------
    //                                  UTILITY FUNCTIONS
    // ---------------------------------------------------------------------------------------------
    public void resetFlags() {
        rideStatus.postValue(Ride.COMPLETED);
        IS_TAKING_RIDE.postValue(0);
        rideStatus.postValue(Ride.DEFAULT);
        markedActive.postValue(true);
        nearPickup.postValue(false);
    }
}
