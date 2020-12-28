package com.example.savaari_driver.register;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.savaari_driver.Repository;
import com.example.savaari_driver.entity.Driver;

import java.util.concurrent.atomic.AtomicBoolean;

public class RegisterViewModel extends ViewModel
{
    // ---------------------------------------------
    //              Main Attributes
    // ---------------------------------------------

    // Data Attributes
    private final Repository repository;
    private Driver driver;

    // Logging Tags
    private final String TAG = this.getClass().getCanonicalName();

    // Flags
    private final MutableLiveData<Boolean> userdataLoaded = new MutableLiveData<>();
    private final MutableLiveData<Boolean> persistConnection = new MutableLiveData<>();

    // -------------------------------------------------
    //              Main Methods
    // -------------------------------------------------

    // Main Constructor
    public RegisterViewModel(Repository repository) {
        this.repository = repository;
        driver = repository.getDriver();
        if (driver != null) {
            userdataLoaded.setValue(true);
        } else {
            userdataLoaded.setValue(false);
        }
    }

    // Getters and Setters
    public Driver getDriver() {
        return driver;
    }
    public LiveData<Boolean> getUserdataLoaded() {
        return userdataLoaded;
    }
    public LiveData<Boolean> getPersistConnection() {
        return persistConnection;
    }
    // --------------------------------------------------
    //                  Main Methods
    // --------------------------------------------------

    // Persisting Connection
    public void persistConnection(int userID) {
        repository.persistLogin(object -> {
            if (object != null) {
                persistConnection.postValue((boolean) object);
            } else {
                persistConnection.postValue(false);
            }
        }, userID);
    } // end of function

    // Loading User Data
    public void loadUserData(int userID) {
        repository.loadUserData(object -> {
            try {
                if (object != null) {
                    repository.setDriver((Driver) object);
                    driver = repository.getDriver();
                    Log.d(TAG, "loadDriverData: User Data Loaded!");
                    userdataLoaded.postValue(true);
                } else {
                    userdataLoaded.postValue(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                userdataLoaded.postValue(false);
            }
        }, userID);
    } // End of function
}
