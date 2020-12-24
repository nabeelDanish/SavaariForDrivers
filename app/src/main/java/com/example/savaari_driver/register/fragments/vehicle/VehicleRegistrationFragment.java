package com.example.savaari_driver.register.fragments.vehicle;

import android.os.Bundle;
import android.util.Log;
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
import com.example.savaari_driver.entity.Vehicle;
import com.example.savaari_driver.register.fragments.FragmentClickListener;

public class VehicleRegistrationFragment extends Fragment {

    private final String TAG = this.getClass().getCanonicalName();
    // Main Attributes
    private FragmentClickListener fragmentClickListener;
    private VehicleRegistrationViewModel mViewModel;
    private boolean firstTime = true;
    int position = -1;

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
    public static VehicleRegistrationFragment newInstance(FragmentClickListener fragmentClickListener) {
        return new VehicleRegistrationFragment(fragmentClickListener);
    }

    public VehicleRegistrationFragment() {
        // Empty Constructor
    }
    public VehicleRegistrationFragment(FragmentClickListener fragmentClickListener) {
        this.fragmentClickListener = fragmentClickListener;
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
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    fragmentClickListener.onVehicleMenuClick();
                } else {
                    if (!firstTime)
                        Toast.makeText(getContext(), "Request Sent Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        position = getArguments().getInt("POSITION");
        // Observing already loaded Data
        mViewModel.getFirstTime().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null) {
                if (!aBoolean && position >= 0) {
                    init();
                }
            }
        });
    }

    private void init() {
        // Disabling UI and setting text
        firstTime = false;
        String[] statusCodes = {"", "Request Sent", "Request Rejected", "Request Accepted", "Vehicle Active!"};
        registerButton.setVisibility(View.GONE);

        try {
            Log.d(TAG, "init: position = " + position);
            if (position >= 0) {
                Vehicle vehicle = mViewModel.getDriver().getVehicles().get(position);

                makeText.setKeyListener(null);
                makeText.setText(vehicle.getMake());

                modelText.setKeyListener(null);
                modelText.setText(vehicle.getModel());

                yearText.setKeyListener(null);
                yearText.setText(vehicle.getYear());

                numberPlateText.setKeyListener(null);
                numberPlateText.setText(vehicle.getNumberPlate());

                colorText.setKeyListener(null);
                colorText.setText(vehicle.getColor());

                successText.setText(statusCodes[vehicle.getStatus()]);
                successText.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}