package com.example.savaari_driver.register.fragments.driver;

import androidx.lifecycle.ViewModel;
import com.example.savaari_driver.Repository;
import com.example.savaari_driver.entity.Driver;

public class DriverRegistrationViewModel extends ViewModel {

    // Main Attributes
    private final Repository repository;
    private Driver driver;

    // Main Methods
    public DriverRegistrationViewModel(Repository repository) {
        this.repository = repository;
        driver = repository.getDriver();
    }
}