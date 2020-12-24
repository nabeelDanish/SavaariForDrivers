package com.example.savaari_driver.register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;

import com.example.savaari_driver.R;
import com.example.savaari_driver.SavaariApplication;
import com.example.savaari_driver.Util;
import com.example.savaari_driver.entity.Driver;
import com.example.savaari_driver.entity.Vehicle;
import com.example.savaari_driver.register.fragments.FragmentClickListener;
import com.example.savaari_driver.register.fragments.driver.DriverRegistrationFragment;
import com.example.savaari_driver.register.fragments.menu.VehicleMenuFragment;
import com.example.savaari_driver.register.fragments.vehicle.VehicleRegistrationFragment;
import com.example.savaari_driver.ride.RideActivity;
import com.example.savaari_driver.settings.SettingsActivity;
import com.example.savaari_driver.settings.SettingsFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RegisterActivity
        extends Util
        implements FragmentClickListener, NavigationView.OnNavigationItemSelectedListener
{
    private final String LOG_TAG = this.getClass().getCanonicalName();
    // Main Attributes
    private RegisterViewModel registerViewModel;
    private ProgressBar loadingCircle;
    private ScheduledFuture<?> future = null;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private View headerView;
    private TextView navUsername;
    private TextView navEmail;
    private Toolbar myToolbar;


    // ------------------------------------------------------------------------------------------
    //                                      MAIN METHODS
    // ------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        themeSelect(this);
        super.onCreate(savedInstanceState);

        // Setting UI Elements
        setContentView(R.layout.activity_register);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        navUsername = headerView.findViewById(R.id.header_nickname);
        navEmail = headerView.findViewById(R.id.header_email);
        myToolbar = findViewById(R.id.toolbar);
        myToolbar.setTitle("Settings");
        setSupportActionBar(myToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        // loadingCircle = findViewById(R.id.progressBar_2);

        // Getting Stored Data
        Intent recvIntent = getIntent();
        if (!recvIntent.getBooleanExtra("API_CONNECTION", true)) {
            Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
        }
        int USER_ID = recvIntent.getIntExtra("USER_ID", -1);

        // Creating View Model
        registerViewModel = ViewModelProviders.of(this, new RegisterViewModelFactory(
                ((SavaariApplication) this.getApplication()).getRepository())
        ).get(RegisterViewModel.class);

        // Calling the Service again to Load Driver if not already
        if (registerViewModel.getDriver() == null) {
            future = ((SavaariApplication) getApplication()).scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> registerViewModel.loadUserData(USER_ID),
                    0L, 8L, TimeUnit.SECONDS);
        }
        // On Data Loaded
        registerViewModel.getUserdataLoaded().observe(this, aBoolean -> {
            if (aBoolean != null) {
                // loadingCircle.setVisibility(View.INVISIBLE);
                if (aBoolean) {
                    Toast.makeText(this, "User Data Loaded!", Toast.LENGTH_SHORT).show();
                    if (future != null) {
                        future.cancel(true);
                    }
                    checkDriverStatus();
                } else {
                    Toast.makeText(this, "User Data could not be loaded!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Main Function for driver account logic
    private void checkDriverStatus()
    {
        // Some UI Stuff
        navUsername.setText(registerViewModel.getDriver().getUsername());
        navEmail.setText(registerViewModel.getDriver().getEmailAddress());

        Intent i = getIntent();
        boolean fromRideActivity = i.getBooleanExtra("FROM_RIDE", false);
        int where = i.getIntExtra("WHERE", -1);
        // Main Check depending on Driver Account Status
        switch (registerViewModel.getDriver().getStatus())
        {
            case Driver.DV_DEFAULT:
            {
                // Driver needs to fill the form and send it
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.register_frame, DriverRegistrationFragment.newInstance(this))
                        .commit();
                break;
            }
            case Driver.DV_REQ_SENT:
            {
                // Request is sent, Driver can add vehicles or see vehicles status
                launchVehicleMenuFragment();
            }
            case Driver.DV_REQ_REJECTED:
            {
                // Request Rejected: handle later
                break;
            }
            case Driver.DV_REQ_APPROVED:
            {
                // Request Approved, check vehicles
                if (fromRideActivity)
                {
                    switch (where)
                    {
                        case 1:
                            launchDocumentFragment();
                            break;
                        case 2:
                            launchVehicleMenuFragment();
                            break;
                    }
                }
                else
                {
                    if (registerViewModel.getDriver().getActiveVehicleID() != Vehicle.DEFAULT_ID) {
                        // Goto Ride Activity
                        launchRideActivity();
                    } else {
                        launchVehicleMenuFragment();
                    }
                }
                break;
            }
        }
    }

    private void launchVehicleMenuFragment() {
        myToolbar.setTitle("VEHICLES");
        Log.d(LOG_TAG, "launchVehicleMenuFragment: launching Vehicle Menu Fragment");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.register_frame, VehicleMenuFragment.newInstance(this))
                .commit();
    }
    private void launchDocumentFragment() {
        myToolbar.setTitle("DOCUMENTS");
        // Driver needs to fill the form and send it
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.register_frame, DriverRegistrationFragment.newInstance(this))
                .commit();
    }

    private void launchRideActivity() {
        Log.d(LOG_TAG, "launchRideActivity: Launching Ride Activity");
        Intent i = new Intent(RegisterActivity.this, RideActivity.class);
        i.putExtra("API_CONNECTION", true);
        i.putExtra("USER_ID", registerViewModel.getDriver().getUserID());
        startActivity(i);
        finish();
    }

    // Interface Implementation
    @Override
    public void onDriverRegistrationClick() {
        // Driver needs to fill the form and send it
        myToolbar.setTitle("DOCUMENT");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.register_frame, DriverRegistrationFragment.newInstance(this))
                .commit();
    }

    @Override
    public void onVehicleRegistrationClick() {
        myToolbar.setTitle("VEHICLES");
        // Driver needs to fill the form and send it
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.register_frame, VehicleRegistrationFragment.newInstance(this))
                .commit();
    }

    @Override
    public void onVehicleRegistrationClick(int position) {
        VehicleRegistrationFragment fragment = VehicleRegistrationFragment.newInstance(this);
        myToolbar.setTitle("VEHICLES");
        Bundle bundle = new Bundle();
        bundle.putInt("POSITION", position);
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.register_frame, fragment)
                .commit();
    }

    @Override
    public void onBackToRideClick() {
        // Going back to Ride Activity
        launchRideActivity();
    }

    @Override
    public void onVehicleMenuClick() {
        launchVehicleMenuFragment();
    }

    // OnNavigationItemSelected Listener from Navigation Bar Click
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        Intent i;
        // Main Switch Case for NavBar
        switch (item.getItemId()) {
            case (R.id.nav_your_trips):
            case (R.id.nav_help):
            case (R.id.nav_wallet):
                break;
            case (R.id.nav_your_documents):
                launchDocumentFragment();
                break;
            case (R.id.nav_your_vehicles):
                launchVehicleMenuFragment();
                break;
            case (R.id.nav_settings):
                i = new Intent(RegisterActivity.this, SettingsActivity.class);
                startActivity(i);
                finish();
                break;
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return(super.onOptionsItemSelected(item));
    }
    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(RegisterActivity.this);
    }
}
