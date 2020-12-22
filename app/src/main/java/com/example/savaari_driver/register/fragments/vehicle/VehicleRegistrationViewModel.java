package com.example.savaari_driver.register.fragments.vehicle;

import androidx.lifecycle.ViewModel;

import com.example.savaari_driver.Repository;
import com.example.savaari_driver.entity.Driver;

public class VehicleRegistrationViewModel extends ViewModel {
    // Main Attributes
    private Repository repository;
    private Driver driver;

    public VehicleRegistrationViewModel(Repository repository) {
        this.repository = repository;
        driver = repository.getDriver();
    }
}