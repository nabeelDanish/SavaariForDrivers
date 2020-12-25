package com.example.savaari_driver.register.fragments.vehicle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.savaari_driver.Repository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class VehicleRegistrationViewModelFactory implements ViewModelProvider.Factory {

    private final Repository repository;

    public VehicleRegistrationViewModelFactory(Repository repository) {
        this.repository = repository;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(VehicleRegistrationViewModel.class)) {
            return (T) new VehicleRegistrationViewModel(repository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}