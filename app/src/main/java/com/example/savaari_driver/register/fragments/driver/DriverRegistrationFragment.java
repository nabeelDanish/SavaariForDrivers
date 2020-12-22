package com.example.savaari_driver.register.fragments.driver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.savaari_driver.R;
import com.example.savaari_driver.SavaariApplication;
import com.example.savaari_driver.register.RegisterViewModel;
import com.example.savaari_driver.register.RegisterViewModelFactory;
import com.example.savaari_driver.register.fragments.RegistrationClickListener;

public class DriverRegistrationFragment extends Fragment {

    // Main Attributes
    private DriverRegistrationViewModel mViewModel;
    private RegistrationClickListener registrationClickListener;
    private Button registrationButton;

    public static DriverRegistrationFragment newInstance(RegistrationClickListener registrationClickListener) {
        return new DriverRegistrationFragment(registrationClickListener);
    }

    public DriverRegistrationFragment() {
        // Empty Constructor
    }
    public DriverRegistrationFragment(RegistrationClickListener registrationClickListener)
    {
        this.registrationClickListener = registrationClickListener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.driver_registration_fragment, container, false);
        registrationButton = view.findViewById(R.id.vehicle_registration_button);
        registrationButton.setOnClickListener(view1 -> {
            registrationClickListener.onVehicleRegistrationClick();
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, new DriverRegistrationViewModelFactory(
                ((SavaariApplication) this.getActivity().getApplication()).getRepository())
        ).get(DriverRegistrationViewModel.class);
    }
}