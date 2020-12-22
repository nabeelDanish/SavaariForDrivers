package com.example.savaari_driver.register.fragments.driver;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.savaari_driver.Repository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class DriverRegistrationViewModelFactory implements ViewModelProvider.Factory {

    private final Repository repository;

    public DriverRegistrationViewModelFactory(Repository repository) {
        this.repository = repository;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(com.example.savaari_driver.register.fragments.driver.DriverRegistrationViewModel.class)) {
            return (T) new com.example.savaari_driver.register.fragments.driver.DriverRegistrationViewModel(repository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}