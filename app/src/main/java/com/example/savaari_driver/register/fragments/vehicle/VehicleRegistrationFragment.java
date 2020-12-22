package com.example.savaari_driver.register.fragments.vehicle;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.savaari_driver.R;
import com.example.savaari_driver.SavaariApplication;
import com.example.savaari_driver.register.fragments.RegistrationClickListener;
import com.example.savaari_driver.register.fragments.driver.DriverRegistrationViewModel;
import com.example.savaari_driver.register.fragments.driver.DriverRegistrationViewModelFactory;

public class VehicleRegistrationFragment extends Fragment {

    // Main Attributes
    private RegistrationClickListener registrationClickListener;
    private VehicleRegistrationViewModel mViewModel;
    private Button driverRegistrationButton;

    // Main Methods
    public static VehicleRegistrationFragment newInstance(RegistrationClickListener registrationClickListener) {
        return new VehicleRegistrationFragment(registrationClickListener);
    }

    public VehicleRegistrationFragment() {
        // Empty Constructor
    }
    public VehicleRegistrationFragment(RegistrationClickListener registrationClickListener) {
        this.registrationClickListener = registrationClickListener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.vehicle_registration_fragment, container, false);
        driverRegistrationButton = view.findViewById(R.id.driver_registration_button);
        driverRegistrationButton.setOnClickListener(view1 -> {
            registrationClickListener.onDriverRegistrationClick();
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, new VehicleRegistrationViewModelFactory(
                ((SavaariApplication) this.getActivity().getApplication()).getRepository())
        ).get(VehicleRegistrationViewModel.class);
    }

}