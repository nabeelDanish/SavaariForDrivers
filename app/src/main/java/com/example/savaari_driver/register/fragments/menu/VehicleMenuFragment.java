package com.example.savaari_driver.register.fragments.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.savaari_driver.R;
import com.example.savaari_driver.SavaariApplication;
import com.example.savaari_driver.entity.*;
import com.google.common.collect.ArrayTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VehicleMenuFragment extends ListFragment
{
    // Main Data
    private VehicleMenuViewModel mViewModel;
    private Driver driver;

    // Main Constructor
    public static VehicleMenuFragment newInstance() {
        return new VehicleMenuFragment();
    }

    // Override Methods
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // View view = inflater.inflate(R.layout.vehicle_menu_fragment, container, false);

        // Loading Vehicle Data
        List<HashMap<String, String>> hashMaps = new ArrayList<>();

        // Assigning Vehicle Text
        ArrayList<Vehicle> vehicles = driver.getVehicles();
        for (Vehicle vehicle : vehicles)
        {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("vehicle_textPrimary", vehicle.getMake() + " " + vehicle.getModel());
            hashMaps.add(hashMap);
        }

        // Keys used
        String[] from = {"vehicle_textPrimary"};
        int[] to = {R.id.vehicle_textPrimary};

        // Creating the Adapter and setting it to the view
        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), hashMaps, R.layout.vehicle_card, from, to);
        setListAdapter(simpleAdapter);

        // Return this layout
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Getting the View Model
        mViewModel = ViewModelProviders.of(this, new VehicleMenuViewModelFactory(
                ((SavaariApplication) Objects.requireNonNull(this.getActivity()).getApplication()).getRepository())
        ).get(VehicleMenuViewModel.class);

        driver = mViewModel.getDriver();
    }
}