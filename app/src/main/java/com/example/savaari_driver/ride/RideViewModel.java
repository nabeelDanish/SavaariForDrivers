package com.example.savaari_driver.ride;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.savaari_driver.Repository;
import com.example.savaari_driver.user.Driver;
import com.google.android.gms.maps.model.LatLng;
import com.example.savaari_driver.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class RideViewModel extends ViewModel {

    // Log Tag
    private static String LOG_TAG = RideViewModel.class.getSimpleName();

    /* Main Data Objects */
    private Driver driver;
    private Ride ride;
    private final ArrayList<LatLng> mUserLocations = new ArrayList<>();

    // Status
    private int ACTIVE_STATUS = 0;

    // Data Repository
    private final Repository repository;

    /* Data Loaded status flags */
    private final MutableLiveData<Integer> IS_TAKING_RIDE = new MutableLiveData<Integer>(0);
    private final MutableLiveData<Boolean> userDataLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> userLocationsLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> markedActive = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> rideFound = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> rideStatus = new MutableLiveData<>(Ride.DEFAULT);

    // Main Constructor
    public RideViewModel(int USER_ID, Repository repository)
    {
        driver = new Driver();
        this.driver.setUserID(USER_ID);
        this.repository = repository;
    }

    // Getters and Setters
    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public ArrayList<LatLng> getUserLocations() {
        return mUserLocations;
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

    /* Need a setter since coordinates are received from activity */
    public void setUserCoordinates(double latitude, double longitude) {
        // Check if the Distance is near Pickup
        try {
            if (IS_TAKING_RIDE.getValue() == 1)
            {
                switch (rideStatus.getValue())
                {
                    case Ride.DEFAULT:
                    {
                        // Calculating distance to pickup location
                        driver.setCurrentLocation(new LatLng(latitude, longitude));
                        double distance = Util.distance(latitude, longitude, ride.getPickupLocation().latitude, ride.getPickupLocation().longitude);
                        if (distance <= 200) {
                            Log.d(LOG_TAG, "setUserCoordinates(): setting near pickup to true");
                            rideStatus.setValue(Ride.PICKUP);
                        }
                        break;
                    }
                    case Ride.STARTED:
                    {
                        // Calculating distance from the previous location to update route
                        double distance = Util.distance(latitude, longitude, driver.getCurrentLocation().latitude, driver.getCurrentLocation().longitude);
                        driver.setCurrentLocation(new LatLng(latitude, longitude));
                        ride.setDistance(ride.getDistance() + distance);
                        Log.d(LOG_TAG, "setUserCoordinates(): Distance Travelled = " + ride.getDistance());

                        // Check if Near Drop-off Location
                        distance = Util.distance(latitude, longitude, ride.getDropoffLocation().latitude, ride.getDropoffLocation().longitude);
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
                driver.setCurrentLocation(new LatLng(latitude, longitude));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function using the repository
    public void getRideStatus()
    {
        Log.d(LOG_TAG, "getRideStatus(): Called!");
        repository.checkRideStatus(object -> {
            try {
                if (object != null) {
                    JSONObject jsonObject = (JSONObject) object;
                    boolean isTakingRide = jsonObject.getBoolean("IS_TAKING_RIDE");
                    if (isTakingRide) {
                        Log.d(LOG_TAG, "getRideStatus(): Ride Found");
                        Log.d(TAG, "Ride = " + jsonObject.toString());
                        setRideData(jsonObject);
                        IS_TAKING_RIDE.postValue(1);
                    }
                    else {
                        Log.d(LOG_TAG, "getRideStatus(): IS_TAKING_RIDE is false");
                    }
                }
                else {
                    Log.d(LOG_TAG, "getRideStatus(): object is null!");
                }
            }
            catch (Exception e)
            {
                Log.d(LOG_TAG, "getRideStatus(): Exception Thrown!");
                e.printStackTrace();
            }
        }, driver.getUserID());
    }
    public void loadUserData()
    {
        if (!userDataLoaded.getValue())
            repository.loadUserData(this::onUserDataLoaded, driver.getUserID());
    }
    public void loadUserLocations()
    {
        if (!userLocationsLoaded.getValue())
            repository.getUserLocations(this::onUserLocationsLoaded);
    }
    public void setMarkActive()
    {
        repository.setMarkActive(object -> {
            try
            {
                if (object != null)
                {
                    JSONObject ride = (JSONObject) object;
                    boolean aBoolean = ride.getInt("STATUS") == 200;
                    if (aBoolean) {
                        Log.d(LOG_TAG, "setMarkActive(): Marked Active!");
                        markedActive.postValue(true);

                        Log.d(TAG, "checkRideStatus(): Ride Found!");
                        Log.d(TAG, "checkRideStatus(): " + ride.toString());
                        setRideData(ride);
                        rideFound.postValue(true);
                    }
                    else {
                        Log.d(LOG_TAG, "setMarkActive(): Marked Active failed!");
                        markedActive.postValue(false);
                    }
                }
            }
            catch (Exception e)
            {
                Log.d(LOG_TAG, "setMarkActive(): Error! Exception Thrown");
                e.printStackTrace();
            }
        }, driver.getUserID(), ACTIVE_STATUS);
    }

    public void confirmRideRequest(int found_status)
    {
        repository.confirmRideRequest(object -> {
            getRideStatus();
        },driver.getUserID(), found_status, ride.getRider().getUserID());
    }

    public void markArrival()
    {
        Log.d(LOG_TAG, "markArrival() called");
        repository.markArrival(object -> {
            try {
                if (object != null) {
                    JSONObject jsonObject = (JSONObject) object;
                    Log.d(LOG_TAG, "markArrival(): jsonObject = " + jsonObject.toString());
                    if (jsonObject.getInt("STATUS") == 200)
                    {
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
                    JSONObject jsonObject = (JSONObject) object;
                    Log.d(LOG_TAG, "startRide(): jsonObject = " + jsonObject.toString());
                    if (jsonObject.getInt("STATUS") == 200)
                    {
                        rideStatus.postValue(Ride.STARTED);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ride.getRideID());
    }
    public void endRide()
    {
        Log.d(LOG_TAG, "endRide() called!");
        repository.endRide(object -> {
            try {
                if (object != null) {
                    JSONObject jsonObject = (JSONObject) object;
                    Log.d(LOG_TAG, "endRide(): jsonObject = " + jsonObject.toString());
                    if (jsonObject.getInt("STATUS") == 200)
                    {
                        rideStatus.postValue(Ride.COMPLETED);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ride.getRideID());
    }

    // Function on User Data Loaded
    public void onUserDataLoaded(Object r) {
        try {
            JSONObject result = (JSONObject) r;
            if (result == null) {
                Log.d(LOG_TAG, "onDataLoaded(): resultString is null");
                userDataLoaded.postValue(false);
            }
            else {
                driver.setName(result.getString("USER_NAME"));
                driver.setEmail(result.getString("EMAIL_ADDRESS"));
                Log.d("loadUserData(): ", driver.getName() + ", " + driver.getEmail());
                userDataLoaded.postValue(true);
                getRideStatus();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            userDataLoaded.postValue(false);
            Log.d(LOG_TAG, "onDataLoaded(): exception thrown");
        }

    }

    // Function on User Locations Loaded
    public void onUserLocationsLoaded(Object r)
    {
        try {
            JSONArray resultArray = (JSONArray) r;
            if (resultArray == null)
            {
                Log.d(TAG, "onUserLocationsLoaded(): resultString is null");
                userLocationsLoaded.postValue(false);
            }
            else {
                Log.d(TAG, "loadUserLocations: " + resultArray.toString());
                Log.d(TAG, "loadUserLocations: found JSON Array");

                // Appending the User Locations in Array
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject obj = resultArray.getJSONObject(i);
                    LatLng userLocation = new LatLng(obj.getDouble("LATITUDE"), obj.getDouble("LONGITUDE"));
                    // Adding Final Object
                    mUserLocations.add(userLocation);
                }
                userLocationsLoaded.postValue(true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "onUserLocationsLoaded(): Exception thrown!");
            userLocationsLoaded.postValue(false);
        }
    }
    // End of Function: onUserLocationsLoaded();

    // Function for Storing Ride Data
    public void setRideData(JSONObject jsonObject)
    {
        ride = new Ride();
        try
        {
            if (jsonObject.has("RIDE_ID"))
            {
                ride.setRideID(jsonObject.getInt("RIDE_ID"));
            }
            ride.getRider().setUserID(jsonObject.getInt("RIDER_ID"));
            ride.getRider().setUsername(jsonObject.getString("RIDER_NAME"));

            double sourceLat = jsonObject.getDouble("SOURCE_LAT");
            double sourceLong = jsonObject.getDouble("SOURCE_LONG");
            double destLat = jsonObject.getDouble("DEST_LAT");
            double destLong = jsonObject.getDouble("DEST_LONG");

            ride.setPickupLocation(new LatLng(sourceLat, sourceLong), "");
            ride.setDropoffLocation(new LatLng(destLat, destLong), "");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "setRideData(): Exception thrown!");
        }
    }

    // Getters and Setters
    public Ride getRide()
    {
        return ride;
    }
    public void setRide(Ride ride)
    {
        this.ride = ride;
    }

    // Observe for IS_TAKING_RIDE
    public LiveData<Integer> getIsTakingRide()
    {
        return IS_TAKING_RIDE;
    }

    public void setIsTakingRide(Integer IS_TAKING_RIDE)
    {
        this.IS_TAKING_RIDE.setValue(IS_TAKING_RIDE);
    }

    public int getACTIVE_STATUS() {
        return ACTIVE_STATUS;
    }

    public void setACTIVE_STATUS(int ACTIVE_STATUS) {
        this.ACTIVE_STATUS = ACTIVE_STATUS;
    }
    public void setRideStatus(int rideStatus)
    {
        this.rideStatus.setValue(rideStatus);
    }
}
