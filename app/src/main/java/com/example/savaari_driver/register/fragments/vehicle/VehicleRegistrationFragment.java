package com.example.savaari_driver.register.fragments.vehicle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.savaari_driver.R;
import com.example.savaari_driver.SavaariApplication;
import com.example.savaari_driver.register.fragments.RegistrationClickListener;

public class VehicleRegistrationFragment extends Fragment {

    // Main Attributes
    private RegistrationClickListener registrationClickListener;
    private VehicleRegistrationViewModel mViewModel;

    // UI Elements
    private EditText makeText;
    private EditText modelText;
    private EditText yearText;
    private EditText numberPlateText;
    private EditText colorText;
    private Button registerButton;
    private ProgressBar loadingCircles;
    private TextView successText;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.vehicle_registration_fragment, container, false);

        // Initializing UI Elements
        makeText = view.findViewById(R.id.make);
        modelText = view.findViewById(R.id.vehicle_model);
        yearText = view.findViewById(R.id.vehicle_year);
        numberPlateText = view.findViewById(R.id.vehicle_number_plate);
        colorText = view.findViewById(R.id.vehicle_color);
        successText = view.findViewById(R.id.vehicle_request_sent_msg);
        loadingCircles = view.findViewById(R.id.vehicle_form_sent_circle);

        // Setting Button
        registerButton = view.findViewById(R.id.vehicle_registration_button);
        registerButton.setOnClickListener(view1 -> {
            // UI
            loadingCircles.setVisibility(View.VISIBLE);

            // Calling View Model
            mViewModel.registerVehicle(makeText.getText().toString(),
                    modelText.getText().toString(),
                    yearText.getText().toString(),
                    numberPlateText.getText().toString(),
                    colorText.getText().toString());
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, new VehicleRegistrationViewModelFactory(
                ((SavaariApplication) this.getActivity().getApplication()).getRepository())
        ).get(VehicleRegistrationViewModel.class);

        // Observing Registration State
        mViewModel.getRegisterVehicleSent().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null) {
                loadingCircles.setVisibility(View.INVISIBLE);
                if (aBoolean) {
                    Toast.makeText(getContext(), "Request Sent Success", Toast.LENGTH_SHORT).show();
                    successText.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "Request Sent Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}