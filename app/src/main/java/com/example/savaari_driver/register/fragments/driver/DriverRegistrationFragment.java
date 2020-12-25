package com.example.savaari_driver.register.fragments.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.savaari_driver.R;
import com.example.savaari_driver.SavaariApplication;
import com.example.savaari_driver.Util;
import com.example.savaari_driver.register.fragments.DatePickerFragment;
import com.example.savaari_driver.register.fragments.FragmentClickListener;
import com.google.common.base.Strings;

import static android.content.ContentValues.TAG;

public class DriverRegistrationFragment extends Fragment
{
    private static final int REQUEST_CODE = 11;
    // Main Attributes
    private DriverRegistrationViewModel mViewModel;
    private FragmentClickListener fragmentClickListener;
    private int formNumber = 0;
    private boolean dataLoaded = false;
    private String[] statusCodes = {"", "Request Sent", "Unfortunately, your Request is Rejected!", "You are Registered!"};

    // UI Elements
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText dobText;
    private EditText phoneNumberText;
    private EditText cnicText;
    private EditText licenseNumberText;
    private LinearLayout firstForm;
    private LinearLayout secondForm;
    private Button navFormButton;
    private Button registerButton;
    private ProgressBar loadingCircle;
    private TextView requestSentText;

    // Data Attributes
    String dateOfBirth;

    // ----------------------------------------------------------------------------------------------
    //                                   MAIN METHODS
    // ----------------------------------------------------------------------------------------------
    public static DriverRegistrationFragment newInstance(FragmentClickListener fragmentClickListener) {
        return new DriverRegistrationFragment(fragmentClickListener);
    }

    public DriverRegistrationFragment() {
        // Empty Constructor
    }
    public DriverRegistrationFragment(FragmentClickListener fragmentClickListener)
    {
        this.fragmentClickListener = fragmentClickListener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check for the results
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // get date from string
            int year = data.getIntExtra("YEAR", 0);
            int month = data.getIntExtra("MONTH", 0);
            int day = data.getIntExtra("DAY", 0);

            if (year != 0) {
                dateOfBirth = padWithZeroes(Integer.toString(year), 4) + "/" + ((month + 1 < 10) ? "0" + (month + 1) : "" + (month + 1))
                        + "/" + ((day < 10) ? "0" + day : "" + day);

                dobText.setText(dateOfBirth);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Get View
        View view = inflater.inflate(R.layout.driver_registration_fragment, container, false);

        // Setting UI Elements
        // Setting Forms
        firstForm = view.findViewById(R.id.first_form);
        secondForm = view.findViewById(R.id.second_form);

        // Setting Text Views
        firstNameText = view.findViewById(R.id.first_name);
        lastNameText = view.findViewById(R.id.last_name);
        dobText = view.findViewById(R.id.dob);
        phoneNumberText = view.findViewById(R.id.phone_number);
        cnicText = view.findViewById(R.id.CNIC);
        licenseNumberText = view.findViewById(R.id.licenseNumber);
        requestSentText = view.findViewById(R.id.request_sent_msg);

        // Setting Loading Circle
        loadingCircle = view.findViewById(R.id.form_sent_circle);

        // Setting Date of Birth
        dobText.setOnClickListener(view1 -> {

            // create the datePickerFragment
            AppCompatDialogFragment newFragment = new DatePickerFragment();
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(DriverRegistrationFragment.this, REQUEST_CODE);
            // show the datePicker
            newFragment.show(this.getActivity().getSupportFragmentManager(), "datePicker");
        });

        // Setting Buttons
        navFormButton = view.findViewById(R.id.form_nav_button);
        navFormButton.setOnClickListener(view1 -> {
            formNumber = (formNumber + 1) % 2;
            toggleForms();
        });

        registerButton = view.findViewById(R.id.driver_registration_button);
        registerButton.setOnClickListener(view1 -> {

            loadingCircle.setVisibility(View.VISIBLE);

            // Calling View Model Function
            mViewModel.registerDriver(firstNameText.getText().toString(),
                    lastNameText.getText().toString(),
                    dobText.getText().toString(),
                    phoneNumberText.getText().toString(),
                    cnicText.getText().toString(),
                    licenseNumberText.getText().toString());
        });

        return view;
    }

    // Function to perform checks from viewModel
    private void init()
    {
        Log.d(TAG, "init: Called!");
        dataLoaded = true;

        // Disabling UI
        registerButton.setEnabled(false);
        registerButton.setVisibility(View.GONE);

        // Setting Data
        requestSentText.setText(statusCodes[mViewModel.getDriver().getStatus()]);

        firstNameText.setKeyListener(null);
        firstNameText.setText(mViewModel.getDriver().getFirstName());

        lastNameText.setKeyListener(null);
        lastNameText.setText(mViewModel.getDriver().getLastName());

        phoneNumberText.setKeyListener(null);
        phoneNumberText.setText(mViewModel.getDriver().getPhoneNo());

        dobText.setKeyListener(null);
        dobText.setText("DATE OF BIRTH");

        cnicText.setKeyListener(null);
        cnicText.setText(mViewModel.getDriver().getCNIC());

        licenseNumberText.setKeyListener(null);
        licenseNumberText.setText(mViewModel.getDriver().getLicenseNumber());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, new DriverRegistrationViewModelFactory(
                ((SavaariApplication) this.getActivity().getApplication()).getRepository())
        ).get(DriverRegistrationViewModel.class);

        // Setting Action for Request Sent
        mViewModel.getIsRequestSent().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null) {
                loadingCircle.setVisibility(View.INVISIBLE);
                if (aBoolean)
                {
                    fragmentClickListener.onVehicleRegistrationClick(-1);
                } else {
                    if (!dataLoaded)
                        Toast.makeText(getContext(), "Request Sent Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setting Init
        mViewModel.getIsFirstTime().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null) {
                if (!aBoolean) {
                    init();
                }
            }
        });
    }

    // Function to pad a String with zeros
    public String padWithZeroes(String text, int length) {
        String pad = Strings.repeat("0", length);
        return (pad + text).substring(text.length());
    }

    private void toggleForms()
    {
        if (formNumber == 0) {
            firstForm.setAnimation(Util.inFromLeftAnimation(400));
            firstForm.setVisibility(View.VISIBLE);

            secondForm.setAnimation(Util.outToRightAnimation(400));
            secondForm.setVisibility(View.GONE);

            navFormButton.setText(R.string.next_form);
            requestSentText.setAnimation(Util.outToBottomAnimation());
            requestSentText.setVisibility(View.INVISIBLE);

        } else {
            firstForm.setAnimation(Util.outToLeftAnimation(400));
            firstForm.setVisibility(View.GONE);

            secondForm.setAnimation(Util.inFromRightAnimation(400));
            secondForm.setVisibility(View.VISIBLE);

            navFormButton.setText(R.string.previous_form);

            requestSentText.setAnimation(Util.inFromBottomAnimation(400));
            requestSentText.setVisibility(View.VISIBLE);
        }
    }
}