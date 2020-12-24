package com.example.savaari_driver.register.fragments.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savaari_driver.R;
import com.example.savaari_driver.SavaariApplication;
import com.example.savaari_driver.entity.Vehicle;
import com.example.savaari_driver.register.fragments.FragmentClickListener;
import com.example.savaari_driver.register.fragments.menu.adapter.VehicleRegistrationAdapter;
import com.example.savaari_driver.ride.adapter.OnItemClickListener;
import com.example.savaari_driver.ride.adapter.VehicleTypeItem;

import java.util.ArrayList;
import java.util.Objects;

public class VehicleMenuFragment extends Fragment implements OnItemClickListener {

    // Main Attributes
    private VehicleMenuViewModel mViewModel;
    private FragmentClickListener fragmentClickListener;

    // UI Elements
    private ImageButton addVehicleBtn;
    private RecyclerView recyclerView;

    // Main Constructor
    public VehicleMenuFragment() {
        // Empty Constructor
    }

    public VehicleMenuFragment(FragmentClickListener fragmentClickListener) {
        this.fragmentClickListener = fragmentClickListener;
    }

    // Main newInstance Method
    public static VehicleMenuFragment newInstance(FragmentClickListener fragmentClickListener) {
        return new VehicleMenuFragment(fragmentClickListener);
    }


    // Main Methods
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.registration_menu_fragment, container, false);
        recyclerView = view.findViewById(R.id.vehicle_list_recycler);
        addVehicleBtn = view.findViewById(R.id.add_vehicle_btn);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Getting View Model
        mViewModel = ViewModelProviders.of(this, new VehicleMenuViewModelFactory(
                ((SavaariApplication) Objects.requireNonNull(this.getActivity()).getApplication()).getRepository())
        ).get(VehicleMenuViewModel.class);
        setAdapter();

        // Setting Button
        addVehicleBtn.setOnClickListener(view -> {
            fragmentClickListener.onVehicleRegistrationClick(-1);
        });
    }

    private void setAdapter() {
        // Assigning the Recycler
        ArrayList<Vehicle> vehicleArrayList = mViewModel.getDriver().getVehicles();
        ArrayList<VehicleTypeItem> vehicleTypeItems = new ArrayList<>();

        // String mappings
        String[] statusCode = {"", "Request Sent", "Request Rejected", "Request Accepted", "Active"};

        int index = 0;
        for (Vehicle vehicle : vehicleArrayList)
        {
            VehicleTypeItem vehicleTypeItem = new VehicleTypeItem();

            vehicleTypeItem.setIndexInArray(index++);
            vehicleTypeItem.setVehicleMakeModel(vehicle.getMake() + " " + vehicle.getModel());
            vehicleTypeItem.setStatus(statusCode[vehicle.getStatus()]);
            if (vehicle.getRideTypeID() == 1) {
                vehicleTypeItem.setVehicleImage(R.drawable.ic_rtype_bike);
            } else {
                vehicleTypeItem.setVehicleImage(R.drawable.ic_car);
            }
            vehicleTypeItems.add(vehicleTypeItem);
        }

        // Calling Adapter
        VehicleRegistrationAdapter vehicleRegistrationAdapter = new VehicleRegistrationAdapter(vehicleTypeItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(vehicleRegistrationAdapter);
    }

    // Implementing OnClick Listener
    @Override
    public void OnClick(int position) {
        fragmentClickListener.onVehicleRegistrationClick(position);
    }
}