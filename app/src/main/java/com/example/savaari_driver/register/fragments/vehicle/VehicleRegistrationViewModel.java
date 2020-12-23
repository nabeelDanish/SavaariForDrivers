package com.example.savaari_driver.register.fragments.vehicle;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.savaari_driver.Repository;
import com.example.savaari_driver.entity.Driver;
import com.example.savaari_driver.entity.Vehicle;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class VehicleRegistrationViewModel extends ViewModel {
    // Main Attributes
    private final Repository repository;
    private Driver driver;
    private Vehicle vehicle;

    // Flags
    private MutableLiveData<Boolean> registerVehicleSent = new MutableLiveData<>();

    // Main Constructor
    public VehicleRegistrationViewModel(Repository repository) {
        this.repository = repository;
        driver = repository.getDriver();
    }

    // Getters and Setters
    public LiveData<Boolean> getRegisterVehicleSent() {
        return registerVehicleSent;
    }

    // Main Methods
    public void registerVehicle(String make, String model, String year, String numberPlate, String color) {
        if (driver != null) {
            // Mapping
            vehicle = new Vehicle();
            vehicle.setMake(make);
            vehicle.setModel(model);
            vehicle.setYear(year);
            vehicle.setNumberPlate(numberPlate);
            vehicle.setColor(color);
            vehicle.setStatus(Vehicle.DEFAULT_ID);

            // Sending Request and Handling Callback
            repository.sendVehicleRegistrationRequest(object -> {
                try {
                    if (object != null) {
                        boolean aBoolean = (boolean) object;
                        if (aBoolean) {
                            // Success
                            Log.d(TAG, "registerVehicle: Vehicle Registration Success");
                            registerVehicleSent.postValue(true);
                        } else {
                            // Failure
                            Log.d(TAG, "registerVehicle: Vehicle Registration Failed!!!");
                            registerVehicleSent.postValue(false);
                        }
                    } else {
                        Log.d(TAG, "registerVehicle: object was null!");
                        registerVehicleSent.postValue(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    registerVehicleSent.postValue(false);
                }
            }, driver, vehicle);
        }
    }
}