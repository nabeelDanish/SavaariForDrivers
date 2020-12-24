package com.example.savaari_driver.register.fragments.menu;

import androidx.lifecycle.ViewModel;

import com.example.savaari_driver.Repository;
import com.example.savaari_driver.entity.*;

public class VehicleMenuViewModel extends ViewModel
{
    // Main Attributes
    private final Repository repository;
    private Driver driver;

    // Main Constructor
    public VehicleMenuViewModel(Repository repository) {
        this.repository = repository;
        driver = repository.getDriver();
    }

    // Getters and Setters
    public Driver getDriver() {
        return driver;
    }
    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}