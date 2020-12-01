package com.example.savaari_driver.auth.signup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.savaari_driver.Repository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class SignUpViewModelFactory implements ViewModelProvider.Factory {

    private final Repository repository;

    public SignUpViewModelFactory(Repository repository) {
        this.repository = repository;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(com.example.savaari_driver.auth.signup.SignUpViewModel.class)) {
            return (T) new com.example.savaari_driver.auth.signup.SignUpViewModel(repository);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}