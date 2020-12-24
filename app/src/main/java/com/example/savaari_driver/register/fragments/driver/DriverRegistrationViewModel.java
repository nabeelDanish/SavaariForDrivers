package com.example.savaari_driver.register.fragments.driver;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.savaari_driver.Repository;
import com.example.savaari_driver.entity.Driver;

public class DriverRegistrationViewModel extends ViewModel {
    private final String TAG = this.getClass().getCanonicalName();

    // Main Attributes
    private final Repository repository;
    private Driver driver;

    // Flags
    private final MutableLiveData<Boolean> isRequestSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFirstTime = new MutableLiveData<>();

    // ----------------------------------------------------------------------------------------------

    // Constructor
    public DriverRegistrationViewModel(Repository repository) {
        this.repository = repository;
        driver = repository.getDriver();
        if (driver != null) {
            if (driver.getStatus() > Driver.DV_DEFAULT) {
                isRequestSent.setValue(driver.getStatus() == Driver.DV_REQ_SENT);
                isFirstTime.setValue(false);
            }
        }
    }

    // Getters and Setters
    public LiveData<Boolean> getIsRequestSent() {
        return isRequestSent;
    }
    public Driver getDriver() {
        return driver;
    }
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    // Main Functions
    public void registerDriver(String firstName, String lastName, String dob, String phoneNumber, String cnic, String licenseNumber)
    {
        // Setting Values
        driver.setFirstName(firstName);
        driver.setLastName(lastName);
        driver.setPhoneNo(phoneNumber);
        driver.setCNIC(cnic);
        driver.setLicenseNumber(licenseNumber);

        isFirstTime.setValue(true);

        repository.sendRegisterDriverRequest(object -> {
            if (object != null) {
                boolean aBoolean = (boolean) object;
                isRequestSent.postValue(aBoolean);
                driver.setStatus(Driver.DV_REQ_SENT);
            } else {
                Log.d(TAG, "registerDriver: object was null!");
                isRequestSent.postValue(false);
            }
        }, driver);
    }

    public MutableLiveData<Boolean> getIsFirstTime() {
        return isFirstTime;
    }
}