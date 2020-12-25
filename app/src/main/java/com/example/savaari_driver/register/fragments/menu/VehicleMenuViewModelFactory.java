package com.example.savaari_driver.register.fragments.menu;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.savaari_driver.Repository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class VehicleMenuViewModelFactory implements ViewModelProvider.Factory {

    private final Repository repository;

    public VehicleMenuViewModelFactory(Repository repository) {
        this.repository = repository;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(VehicleMenuViewModel.class)) {
            return (T) new VehicleMenuViewModel(repository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}