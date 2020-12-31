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

    // Constants for Status Flags
    public static final int
            OFFLINE = 0,
            DATA_LOAD_SUCCESS = 1,
            DATA_LOAD_FAILURE = 2,
            VEHICLE_SELECTED_SUCCESS = 3,
            VEHICLE_SELECTED_FAILURE = 4,
            MARKED_ACTIVE_SUCCESS = 5,
            MARKED_ACTIVE_FAILURE = 6,
            MATCHMAKING_STARTED_SUCCESS = 7,
            MATCHMAKING_STARTED_FAILURE = 8,
            RIDE_REQUEST_FOUND = 9,
            CONFIRM_RIDE_SUCCESS = 10,
            CONFIRM_RIDE_FAILURE = 11,
            NEAR_PICKUP = 12,
            PICKUP_MARK_SUCCESS = 13,
            PICKUP_MARK_FAILURE = 14,
            RIDE_STARTED_SUCCESS = 15,
            RIDE_STARTED_FAILURE = 16,
            NEAR_DEST = 17,
            DEST_MARK_SUCCESS = 18,
            DEST_MARK_FAILURE = 19,
            PAYMENT_SUCCESS = 20,
            PAYMENT_FAILURE = 21,
            FEEDBACK_SUCCESS = 22,
            FEEDBACK_FAILURE = 23;

    /* Status Flags */
    private final MutableLiveData<Integer> driverStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userLocationsLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> rideFound = new MutableLiveData<>();
    private final MutableLiveData<Integer> vehicleSelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> giveRiderFeedback = new MutableLiveData<>();
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
        // TODO : Complete syncing with repository
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
    public RideRequest getRideRequest() {
        return rideRequest;
    }

    /* Return LiveData to observe Data Loaded Flags */
    public LiveData<Boolean> isLiveUserLocationsLoaded() { return userLocationsLoaded; }
    public LiveData<Integer> getDriverStatus() {
        return driverStatus;
    }
    public LiveData<Boolean> getIsRideFound() {
        return rideFound;
    }

    // ---------------------------------------------------------------------------------------------
    //                                   LOCATION, DATA, OTHER
    // ---------------------------------------------------------------------------------------------

    // Function to Mark User Offline and start State Machine
    public void markOffline() {
        driverStatus.setValue(OFFLINE);
    }

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
                driverStatus.postValue(DATA_LOAD_FAILURE);
            } else {
                driver = (Driver) r;
                driver.setCurrentLocation(currentLocation);
                Log.d("loadUserData(): ", driver.getUsername() + ", " + driver.getEmailAddress());
                repository.setDriver(driver);

                driverStatus.postValue(DATA_LOAD_SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            driverStatus.postValue(DATA_LOAD_FAILURE);
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
        // Location Checks
        try {
            if (driverStatus.getValue() != null) {
                // Near Pickup Check
                if (driverStatus.getValue() == CONFIRM_RIDE_SUCCESS) {
                    // Saving Location in Driver object
                    driver.getCurrentLocation().setLatLng(new LatLng(latitude, longitude));

                    // Calculating distance to pickup location
                    double distance = Util.distance(latitude, longitude, ride.getRideParameters().getPickupLocation().getLatitude(), ride.getRideParameters().getPickupLocation().getLongitude());
                    Log.d(TAG, "setUserCoordinates: distance to pickup = " + distance);

                    // Distance Check
                    if (distance <= 100) {
                        Log.d(LOG_TAG, "setUserCoordinates(): setting near pickup to true");
                        driverStatus.postValue(NEAR_PICKUP);
                    }
                }
                // Near Destination
                if (driverStatus.getValue() == RIDE_STARTED_SUCCESS) {
                    // Calculating distance from the previous location to update route
                    double distance = Util.distance(latitude, longitude, driver.getCurrentLocation().getLatitude(), driver.getCurrentLocation().getLongitude());
                    driver.getCurrentLocation().setLatLng(new LatLng(latitude, longitude));
                    ride.setDistanceTravelled(ride.getDistanceTravelled() + distance);
                    Log.d(LOG_TAG, "setUserCoordinates(): Distance Travelled = " + ride.getDistanceTravelled());

                    // Check if Near Drop-off Location
                    distance = Util.distance(latitude, longitude, ride.getRideParameters().getDropoffLocation().getLatitude(), ride.getRideParameters().getDropoffLocation().getLongitude());

                    // Distance Check
                    if (distance <= 100) {
                        Log.d(LOG_TAG, "setUserCoordinates(): near drop-off location");
                        driverStatus.postValue(NEAR_DEST);
                    }
                }
                else {
                    saveDriverLocation(latitude, longitude);
                }
            }
            else {
                saveDriverLocation(latitude, longitude);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDriverLocation(double latitude, double longitude) {
        if (driver.getCurrentLocation() != null) {
            driver.getCurrentLocation().setLatLng(new LatLng(latitude, longitude));
        } else {
            Location location = new Location();
            location.setLatLng(new LatLng(latitude, longitude));
            driver.setCurrentLocation(location);
            currentLocation = driver.getCurrentLocation();
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
                        driverStatus.postValue(VEHICLE_SELECTED_SUCCESS);
                    } else {
                        vehicleSelected.postValue(-1);
                        driverStatus.postValue(VEHICLE_SELECTED_FAILURE);
                    }
                } else {
                    vehicleSelected.postValue(-1);
                    driverStatus.postValue(VEHICLE_SELECTED_FAILURE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                driverStatus.postValue(VEHICLE_SELECTED_FAILURE);
                vehicleSelected.postValue(-1);
            }
        }, driver.getUserID(), vehicle.getVehicleID());
    }

    // ---------------------------------------------------------------------------------------------
    //                                   MATCHMAKING
    // ---------------------------------------------------------------------------------------------
    // Function using the repository
    public void getStartingRideForDriver() {
        Log.d(TAG, "getStartingRideForDriver: called!");
        repository.checkRideStatus(object -> {
            try {
                if (object != null) {
                    ride = (Ride) object;
                    beginStartingRide();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, driver.getUserID(), rideRequest.getRider().getUserID(), rideRequest.getRideType().getTypeID());
    }
    private void beginStartingRide() {
        driverStatus.postValue(CONFIRM_RIDE_SUCCESS);
        int rideStatus = ride.getRideStatus();
        switch (rideStatus)
        {
            case Ride.PICKUP:
                break;
            case Ride.DRIVER_ARRIVED:
                driverStatus.postValue(PICKUP_MARK_SUCCESS);
                break;
            case Ride.STARTED:
                driverStatus.postValue(RIDE_STARTED_SUCCESS);
                break;
            case Ride.NEAR_DROPFF:
                driverStatus.postValue(NEAR_DEST);
                break;
            case Ride.TAKE_PAYMENT:
                driverStatus.postValue(DEST_MARK_SUCCESS);
                break;
        }
    }

    public void getRideForDriver()
    {
        Log.d(LOG_TAG, "getRideForDriver(): Called!");
        repository.checkRideStatus(object -> {
            try {
                if (object != null) {
                    ride = (Ride) object;
                    driverStatus.postValue(CONFIRM_RIDE_SUCCESS);
                }
                else {
                    Log.d(LOG_TAG, "getRideForDriver(): object is null!");
                    driverStatus.postValue(CONFIRM_RIDE_FAILURE);
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "getRideStatus(): Exception Thrown!");
                e.printStackTrace();
                driverStatus.postValue(CONFIRM_RIDE_FAILURE);
            }
        }, driver.getUserID(), rideRequest.getRider().getUserID(), rideRequest.getRideType().getTypeID());
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
                            driverStatus.postValue(MARKED_ACTIVE_SUCCESS);
                        } else {
                            Log.d(LOG_TAG, "setMarkActive(): Marked DeActive!");
                            driverStatus.postValue(DATA_LOAD_SUCCESS);
                        }
                    } else {
                        Log.d(LOG_TAG, "setMarkActive(): Marked Active failed!");
                        if (activeStatus == 1)
                            driverStatus.postValue(MARKED_ACTIVE_FAILURE);
                        else
                            driverStatus.postValue(MATCHMAKING_STARTED_SUCCESS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (activeStatus == 1)
                    driverStatus.postValue(MARKED_ACTIVE_FAILURE);
                else
                    driverStatus.postValue(MATCHMAKING_STARTED_SUCCESS);
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
                        getRideForDriver();
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
            driverStatus.postValue(MATCHMAKING_STARTED_SUCCESS);

            repository.startMatchmaking(object -> {
                try {
                    if (object != null) {
                        rideRequest = (RideRequest) object;
                        driverStatus.postValue(RIDE_REQUEST_FOUND);
                    } else {
                        Log.d(TAG, "startMatchMaking: found null");
                        driverStatus.postValue(MATCHMAKING_STARTED_FAILURE);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "startMatchMaking: Exception thrown!");
                    e.printStackTrace();
                    driverStatus.postValue(MATCHMAKING_STARTED_FAILURE);
                } finally {
                    matchmakingStarted = false;
                }
            }, driver.getUserID());
        }
    }
    public void confirmRideRequest(int found_status)
    {
        rideRequest.setDriver(driver);
        repository.confirmRideRequest(object -> {
            if (object != null) {
                boolean aBoolean = (boolean) object;
                if (aBoolean) {
                    if (found_status == 1) {
                        Log.d(TAG, "confirmRideRequest: Ride Confirmed!");
                        getRideForDriver();
                    } else {
                        Log.d(TAG, "confirmRideRequest: Ride Rejected!");
                        driverStatus.postValue(CONFIRM_RIDE_SUCCESS);
                    }
                } else {
                    driverStatus.postValue(CONFIRM_RIDE_FAILURE);
                }
            } else {
                driverStatus.postValue(CONFIRM_RIDE_FAILURE);
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
                    if (aBoolean) {
                        driverStatus.postValue(PICKUP_MARK_SUCCESS);
                    } else {
                        driverStatus.postValue(PICKUP_MARK_FAILURE);
                    }
                } else {
                    Log.d(TAG, "markArrival: found null!");
                    driverStatus.postValue(PICKUP_MARK_FAILURE);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                driverStatus.postValue(PICKUP_MARK_FAILURE);
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
                    if (aBoolean) {
                        driverStatus.postValue(RIDE_STARTED_SUCCESS);
                    } else {
                        driverStatus.postValue(RIDE_STARTED_FAILURE);
                    }
                } else {
                    driverStatus.postValue(RIDE_STARTED_FAILURE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                driverStatus.postValue(RIDE_STARTED_FAILURE);
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
                        driverStatus.postValue(DEST_MARK_SUCCESS);
                    } else {
                        driverStatus.postValue(DEST_MARK_FAILURE);
                    }
                } else {
                    driverStatus.postValue(DEST_MARK_FAILURE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                driverStatus.postValue(DEST_MARK_FAILURE);
            }
        }, ride, ride.getDistanceTravelled(), driver.getUserID());
    }

    public void endRideWithPayment(double amountPaid) {
        Log.d(LOG_TAG, "endRideWithPayment() called!");

        // Storing Payment
        ride.setPayment(new Payment());
        ride.getPayment().setAmountPaid(amountPaid);
        ride.getPayment().setPaymentMode(rideRequest.getPaymentMethod());
        ride.getPayment().setChange(amountPaid - ride.getFare());

        // Calling Network Service
        repository.endRideWithPayment(object -> {
            try {
                if (object != null) {
                    boolean aBoolean = (boolean) object;
                    Log.d(TAG, "endRideWithPayment: jsonObject = " + aBoolean);
                    if (aBoolean) {
                        driverStatus.postValue(PAYMENT_SUCCESS);
                    } else {
                        driverStatus.postValue(PAYMENT_FAILURE);
                    }
                }
                else {
                    Log.d(TAG, "endRideWithPayment: object is null");
                    driverStatus.postValue(PAYMENT_FAILURE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                driverStatus.postValue(PAYMENT_FAILURE);
            }
        }, ride.getRideID(), ride.getPayment(), driver.getUserID());
    }

    public void giveRiderFeedback(float rating) {
        repository.giveFeedbackForRider(object -> {
            try {
                if (object != null) {
                    boolean aBoolean = (boolean) object;
                    if (aBoolean) {
                        driverStatus.postValue(FEEDBACK_SUCCESS);
                    } else {
                        driverStatus.postValue(FEEDBACK_FAILURE);
                    }
                } else {
                    driverStatus.postValue(FEEDBACK_FAILURE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                driverStatus.postValue(FEEDBACK_FAILURE);
            }
        }, ride, rating);
    }

    // ---------------------------------------------------------------------------------------------
    //                                  UTILITY FUNCTIONS
    // ---------------------------------------------------------------------------------------------
    public void resetFlags() {
        driverStatus.postValue(MARKED_ACTIVE_SUCCESS);
    }
}
