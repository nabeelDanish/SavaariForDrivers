package com.example.savaari_driver.register;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.savaari_driver.Repository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class RegisterViewModelFactory implements ViewModelProvider.Factory {

    private final Repository repository;

    public RegisterViewModelFactory(Repository repository) {
        this.repository = repository;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(com.example.savaari_driver.register.RegisterViewModel.class)) {
            return (T) new com.example.savaari_driver.register.RegisterViewModel(repository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}