package com.example.savaari_driver.ride;

// Imports
import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savaari_driver.R;
import com.example.savaari_driver.SavaariApplication;
import com.example.savaari_driver.Util;
import com.example.savaari_driver.entity.Driver;
import com.example.savaari_driver.entity.RideRequest;
import com.example.savaari_driver.entity.Vehicle;
import com.example.savaari_driver.register.RegisterActivity;
import com.example.savaari_driver.ride.adapter.OnItemClickListener;
import com.example.savaari_driver.ride.adapter.VehicleSelectAdapter;
import com.example.savaari_driver.ride.adapter.VehicleTypeItem;
import com.example.savaari_driver.services.location.LocationUpdateUtil;
import com.example.savaari_driver.settings.SettingsActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Main Class
public class RideActivity
        extends Util
        implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnInfoWindowClickListener,
        OnItemClickListener {
    // ---------------------------------------------------------------------------------------------
    //                                    MAIN ATTRIBUTES
    // ---------------------------------------------------------------------------------------------

    // View Model
    private RideViewModel rideViewModel = null;
    private com.example.savaari_driver.entity.Location driverLocation;

    // Data Related Variables
    private ArrayList<VehicleTypeItem> vehicleTypeItems;

    // Util Variables
    private final String TAG = RideActivity.this.getClass().getCanonicalName();

    // Flags
    boolean canLoadUserData = false;
    boolean isUserDataLoaded = false;
    boolean matchMakingStarted = false;
    boolean isTakingRide = false;
    boolean isActive = false;

    // Map related Variables
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean locationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final float DEFAULT_ZOOM = 15;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeoApiContext geoApiContext = null;
    private ImageView centerGPSButton;

    /* Drawing the route on Maps*/
    private Polyline destinationPolyline = null;
    private Marker destinationMarker = null;
    private Marker pickupMarker = null;
    private Polyline pickupPolyline = null;

    // UI Related Handlers
    private DrawerLayout drawer;
    private ImageButton menuButton;
    private NavigationView navigationView;
    private View headerView;
    private TextView navUsername, navEmail;
    private Button matchmakingControllerBtn;
    private TextView rideStatusBar;
    private ProgressBar progressBar;
    private LinearLayout rideDetailsPanel;
    private TextView riderNameView;
    private TextView riderRatingBar;
    private LinearLayout vehicleSelectLayout;
    private LinearLayout rateRideCard;
    private RatingBar feedbackRatingBar;
    private Button submitRating;

    // Broadcast Receiver Function
    BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Log.d(TAG, "onReceive(): 10 seconds passed!");
            Bundle bundle = intent.getExtras();
            Location location = (Location) bundle.get("Location");
            moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), false);

            // Saving User Location and check if user data loaded
            saveUserLocation(location);

            // Loading User Data if not already loaded!
            if (!isUserDataLoaded && canLoadUserData)
                loadUserData();
        }
    };

    // ---------------------------------------------------------------------------------------------
    //                                    Main Methods
    // ---------------------------------------------------------------------------------------------

    // --------------------------------------------------------
    // Main onCreate Function to override
    // --------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Setting UI Elements
        themeSelect(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        // Registering Receiver
        LocalBroadcastManager.getInstance(RideActivity.this).registerReceiver(locationUpdateReceiver, new IntentFilter("Update"));

        // Getting Stored Data
        Intent recvIntent = getIntent();
        if (!recvIntent.getBooleanExtra("API_CONNECTION", true)) {
            Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
        }
        int USER_ID = recvIntent.getIntExtra("USER_ID", -1);
        if (USER_ID == -1) {
            SharedPreferences sh
                    = getSharedPreferences("AuthSharedPref",
                    MODE_PRIVATE);

            USER_ID = sh.getInt("USER_ID", -1);
        }

        // Setting Stuff
        if (USER_ID == -1) {
            Toast.makeText(RideActivity.this, "Sorry. We can not authenticate you", Toast.LENGTH_LONG).show();
        }
        else {
            // Checking services and getting permissions
            getLocationPermission();

            // Getting View Model
            rideViewModel = new ViewModelProvider(this, new RideViewModelFactory(USER_ID,
                    ((SavaariApplication) this.getApplication()).getRepository())
            ).get(RideViewModel.class);

            // Calling Main Observer
            observeStatusFlag();
        }// End of Else: Location Permission Granted

    }// ----------------------------------- End of OnCreate() -------------------------------------;

    // --------------------------------------------------------------------------------------------
    //                                     MAIN STATUS LISTENER
    // --------------------------------------------------------------------------------------------
    private void observeStatusFlag() {
        rideViewModel.getDriverStatus().observe(this, status ->
        {
            if (status != null) {
                switch (status) {
                    case RideViewModel.OFFLINE: {
                        loadUserData();
                        break;
                    }
                    case RideViewModel.DATA_LOAD_SUCCESS: {
                        onUserDataLoaded();
                        break;
                    }
                    case RideViewModel.DATA_LOAD_FAILURE: {
                        onUserDataFailure();
                        break;
                    }
                    case RideViewModel.VEHICLE_SELECTED_SUCCESS: {
                        onVehicleSelected();
                        break;
                    }
                    case RideViewModel.VEHICLE_SELECTED_FAILURE: {
                        onVehicleFailure();
                        break;
                    }
                    case RideViewModel.MARKED_ACTIVE_SUCCESS: {
                        onMarkActive();
                        break;
                    }
                    case RideViewModel.MARKED_ACTIVE_FAILURE: {
                        onMarkActiveFailure();
                        break;
                    }
                    case RideViewModel.MATCHMAKING_STARTED_SUCCESS: {
                        onMatchmakingStarted();
                        break;
                    }
                    case RideViewModel.MATCHMAKING_STARTED_FAILURE: {
                        onMatchmakingFailure();
                        break;
                    }
                    case RideViewModel.RIDE_REQUEST_FOUND: {
                        onRideRequestFound();
                        break;
                    }
                    case RideViewModel.CONFIRM_RIDE_SUCCESS: {
                        onConfirmRideSuccess();
                        break;
                    }
                    case RideViewModel.CONFIRM_RIDE_FAILURE: {
                        onConfirmRideFailure();
                        break;
                    }
                    case RideViewModel.NEAR_PICKUP: {
                        onNearPickup();
                        break;
                    }
                    case RideViewModel.PICKUP_MARK_SUCCESS: {
                        onNearPickupSuccess();
                        break;
                    }
                    case RideViewModel.PICKUP_MARK_FAILURE: {
                        onNearPickupFailure();
                        break;
                    }
                    case RideViewModel.RIDE_STARTED_SUCCESS: {
                        onStartRideSuccess();
                        break;
                    }
                    case RideViewModel.RIDE_STARTED_FAILURE: {
                        onStartRideFailure();
                        break;
                    }
                    case RideViewModel.NEAR_DEST: {
                        onNearDestination();
                        break;
                    }
                    case RideViewModel.DEST_MARK_SUCCESS: {
                        onConfirmEndRideSuccess();
                        break;
                    }
                    case RideViewModel.DEST_MARK_FAILURE: {
                        onConfirmEndRideFailure();
                        break;
                    }
                    case RideViewModel.PAYMENT_SUCCESS: {
                        onPaymentSuccess();
                        break;
                    }
                    case RideViewModel.PAYMENT_FAILURE: {
                        onPaymentFailure();
                        break;
                    }
                    case RideViewModel.FEEDBACK_SUCCESS: {
                        onFeedbackSuccess();
                        break;
                    }
                    case RideViewModel.FEEDBACK_FAILURE: {
                        onFeedbackFailure();
                        break;
                    }
                }
            }// End if: non null
        });
    } // End of Observer

    // --------------------------------------------------------------------------------------------
    //                                     UI FUNCTIONS
    // --------------------------------------------------------------------------------------------
    /*
     * Initializes View Objects including:
     * centerGPSButton
     * autocompleteFragment
     * Main Button
     */
    private void initUIElements() {
        Log.d(TAG, "init: initializing");

        // Initializing UI
        initializeNavigationBar();

        // UI Elements
        centerGPSButton = findViewById(R.id.user_location);
        centerGPSButton.setEnabled(true);

        rideStatusBar = findViewById(R.id.bottomAppBar2);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        rideDetailsPanel = findViewById(R.id.ride_detail_sub_panel);
        rideDetailsPanel.setVisibility(View.INVISIBLE);

        riderNameView = findViewById(R.id.rider_name);
        riderRatingBar = findViewById(R.id.rider_rating);
        vehicleSelectLayout = findViewById(R.id.select_vehicle_card);
        rateRideCard = findViewById(R.id.end_of_ride_details_panel);
        feedbackRatingBar = findViewById(R.id.feedback_rating_bar);
        submitRating = findViewById(R.id.submit_rating);

        // moveCamera to user location
        centerGPSButton.setOnClickListener(v -> getDeviceLocation());

        // Main Button to start Search Ride
        initializeMainButton();

        // Marking User Offline
        markOffline();
    }// End of init call

    // Initializing Navigation Bar
    private void initializeNavigationBar() {
        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);
        navUsername = headerView.findViewById(R.id.header_nickname);
        navEmail = headerView.findViewById(R.id.header_email);
        menuButton = findViewById(R.id.menu_btn);

        menuButton.setOnClickListener(v -> {
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
    }

    // ---------------------------------
    //     MAIN BUTTON Listener
    // ---------------------------------
    private void initializeMainButton() {

        matchmakingControllerBtn = findViewById(R.id.go_btn);
        matchmakingControllerBtn.setEnabled(false);
        matchmakingControllerBtn.setOnClickListener(v ->
        {
            // UI
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            progressBar.setVisibility(View.VISIBLE);

            // Flags and Calls
            Object obj = rideViewModel.getDriverStatus().getValue();
            if (obj != null)
            {
                int status = (int) obj;
                switch(status) {
                    case RideViewModel.DATA_LOAD_SUCCESS:
                    case RideViewModel.VEHICLE_SELECTED_FAILURE: {
                        selectVehicle();
                        break;
                    }
                    case RideViewModel.VEHICLE_SELECTED_SUCCESS:
                    case RideViewModel.MARKED_ACTIVE_FAILURE: {
                        setMarkActive();
                        break;
                    }
                    case RideViewModel.MATCHMAKING_STARTED_SUCCESS: {
                        setMarkDeactivate();
                        break;
                    }
                    case RideViewModel.RIDE_REQUEST_FOUND:
                    case RideViewModel.CONFIRM_RIDE_FAILURE: {
                        rejectRideRequest();
                        break;
                    }
                    case RideViewModel.NEAR_PICKUP:
                    case RideViewModel.PICKUP_MARK_FAILURE: {
                        confirmNearPickup();
                        break;
                    }
                    case RideViewModel.PICKUP_MARK_SUCCESS:
                    case RideViewModel.RIDE_STARTED_FAILURE: {
                        startRide();
                        break;
                    }
                    case RideViewModel.NEAR_DEST:
                    case RideViewModel.DEST_MARK_FAILURE: {
                        confirmEndRide();
                        break;
                    }
                    case RideViewModel.DEST_MARK_SUCCESS:
                    case RideViewModel.PAYMENT_FAILURE: {
                        takePayment();
                        break;
                    }
                }
            }
        }); // End of Matchmaking Controller Button
    }

    // --------------------------------------------------------------------------------------------
    //                                     MATCH MAKING FUNCTIONS
    // --------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------
    // Selecting Vehicle Action
    // --------------------------------------------------------------------------------------------
    private void selectVehicle()
    {
        // Setting UI
        progressBar.setVisibility(View.INVISIBLE);
        matchmakingControllerBtn.setEnabled(false);
        vehicleSelectLayout.setAnimation(Util.inFromBottomAnimation(400));
        vehicleSelectLayout.setVisibility(View.VISIBLE);

        // Showing Driver the Vehicle Menu to Select a Vehicle to Mark Active
        vehicleTypeItems = new ArrayList<>();

        // TODO: do something about Vehicle Types list
        String[] ride_types = {"Bike", "Smol", "Med Car", "Big Car"};

        // Adding Data
        ArrayList<Vehicle> vehicles = rideViewModel.getDriver().getVehicles();
        int i = 0;
        for (Vehicle vehicle : vehicles)
        {
            if (vehicle.getStatus() == Vehicle.VH_ACCEPTANCE_ACK)
            {
                int rideType = vehicle.getRideTypeID() - 1;
                String type = ride_types[rideType];
                Log.d(TAG, "selectVehicle: Recycler View: rideType = " + type);
                Log.d(TAG, "selectVehicle: rideType = " + rideType);
                vehicleTypeItems.add(new VehicleTypeItem(i, R.drawable.ic_car,
                        vehicle.getMake() + " " + vehicle.getModel(),
                        type));
            }
            ++i;
        }

        // Calling Adapter
        VehicleSelectAdapter vehicleSelectAdapter = new VehicleSelectAdapter(vehicleTypeItems, this);
        RecyclerView recyclerView = findViewById(R.id.select_vehicle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(vehicleSelectAdapter);
    }

    // Selecting Vehicle Response
    private void onVehicleSelected() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        vehicleSelectLayout.setAnimation(Util.outToBottomAnimation());
        vehicleSelectLayout.setVisibility(View.GONE);
        matchmakingControllerBtn.setEnabled(true);
        matchmakingControllerBtn.setText("ACTIVE");
    }
    private void onVehicleFailure() {

        // UI
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(RideActivity.this, "Vehicle Selection Failed!", Toast.LENGTH_SHORT).show();
    }


    // --------------------------------------------------------------------------------------------
    // Set Mark Active Action
    // --------------------------------------------------------------------------------------------
    private void setMarkActive() {
        // UI
        progressBar.setVisibility(View.VISIBLE);

        // Flags
        rideViewModel.setMarkActive(1);
    }
    private void setMarkDeactivate() {
        // UI
        progressBar.setVisibility(View.VISIBLE);

        // Flags
        rideViewModel.setMarkActive(0);
    }

    // Set Mark Active Response
    private void onMarkActive() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        rideStatusBar.setText("You're Online");
        matchmakingControllerBtn.setVisibility(View.VISIBLE);
        matchmakingControllerBtn.setText("DEACTIVE");

        // Flags
        isActive = true;
        matchMakingStarted = true;
        rideViewModel.startMatchMaking();
    }

    private void onMarkActiveFailure() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        rideStatusBar.setText("You're Offline");
        matchmakingControllerBtn.setVisibility(View.VISIBLE);
        matchmakingControllerBtn.setText("ACTIVE");
        Toast.makeText(RideActivity.this, "Mark Active Failed!", Toast.LENGTH_SHORT).show();

        // Flags
        matchMakingStarted = false;
    }

    // --------------------------------------------------------------------------------------------
    // Matchmaking Response
    // --------------------------------------------------------------------------------------------
    private void onMatchmakingStarted() {
        // UI
        rideStatusBar.setText("Searching for Rides");

        // Flags
    }

    private void onMatchmakingFailure() {
        // UI
        Toast.makeText(RideActivity.this, "Matchmaking failed", Toast.LENGTH_SHORT).show();

        // Flags
        if (isActive)
            rideViewModel.startMatchMaking();
    }

    private void onRideRequestFound() {
        // UI
        rideDetailsPanel.setAnimation(Util.inFromBottomAnimation(400));
        rideDetailsPanel.setVisibility(View.VISIBLE);
        riderNameView.setText(rideViewModel.getRideRequest().getRider().getUsername());
        float rating = rideViewModel.getRideRequest().getRider().getRating();
        String text = String.format("%.1f", rating);
        riderRatingBar.setText(text);
        matchmakingControllerBtn.setVisibility(View.VISIBLE);
        matchmakingControllerBtn.setText("CANCEL");
        rideDetailsPanel.setOnClickListener(view -> {
            confirmRideRequest();
        });
        rideStatusBar.setText("Ride Found");
        // Flags
    }

    // --------------------------------------------------------------------------------------------
    //  Ride Request Confirm or Reject Action
    // --------------------------------------------------------------------------------------------

    // Actions
    private void confirmRideRequest() {
        // UI
        progressBar.setVisibility(View.VISIBLE);

        // Flags
        isTakingRide = true;
        rideViewModel.confirmRideRequest(1);
    }
    private void rejectRideRequest()
    {
        // UI
        progressBar.setVisibility(View.VISIBLE);

        // Flags
        isTakingRide = false;
        rideViewModel.confirmRideRequest(0);
    }

    // Responses
    private void onConfirmRideSuccess() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        rideDetailsPanel.setAnimation(Util.outToBottomAnimation());
        rideDetailsPanel.setVisibility(View.GONE);

        if (isTakingRide) {
            // Create the Map Maker to Rider Location
            MarkerOptions options = new MarkerOptions()
                    .position(rideViewModel.getRide().getRideParameters().getPickupLocation().toLatLng())
                    .title("Pickup");
            pickupMarker = googleMap.addMarker(options);

            // Debugging Part Starts
            Driver temp = rideViewModel.getDriver();
            if (temp == null) {
                Log.w(TAG, "onCreate: Driver is NULL!");
            }
            com.example.savaari_driver.entity.Location tempLocation = temp.getCurrentLocation();
            if (tempLocation == null) {
                Log.w(TAG, "onCreate: Location is NULL!");
            }
            // Debugging Ends

            calculateDirections(driverLocation.toLatLng(), pickupMarker, false);
            setDestination(rideViewModel.getRide().getRideParameters().getDropoffLocation().toLatLng(), "Destination");

            // Disable the button
            matchmakingControllerBtn.setVisibility(View.INVISIBLE);
        }

        // Flags
        if (!isTakingRide) {
            rideViewModel.startMatchMaking();
        }
    }
    private void onConfirmRideFailure() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(RideActivity.this, "Confirm Ride failed!", Toast.LENGTH_SHORT).show();
        rideDetailsPanel.setAnimation(Util.outToBottomAnimation());
        rideDetailsPanel.setVisibility(View.GONE);

        // Flags
        rideViewModel.setMarkActive(1);
    }

    // --------------------------------------------------------------------------------------------
    //  Pickup Request Actions and Response
    // --------------------------------------------------------------------------------------------
    // Action
    private void onNearPickup() {
        // UI
        matchmakingControllerBtn.setVisibility(View.VISIBLE);
        matchmakingControllerBtn.setEnabled(true);
        matchmakingControllerBtn.setText("MARK ARRIVAL");
        rideStatusBar.setText("Near Pickup");

        // Flags

    }
    private void confirmNearPickup() {
        // UI
        progressBar.setVisibility(View.VISIBLE);

        // Flags
        rideViewModel.markArrival();
    }
    // Response
    private void onNearPickupSuccess() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        rideStatusBar.setText("Waiting for Rider");
        matchmakingControllerBtn.setVisibility(View.VISIBLE);
        matchmakingControllerBtn.setEnabled(true);
        matchmakingControllerBtn.setText("START RIDE");

        // Flags
    }
    private void onNearPickupFailure() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(RideActivity.this, "Pickup failed!", Toast.LENGTH_SHORT).show();

        // Flags
    }

    // --------------------------------------------------------------------------------------------
    //  Starting Ride Request and Response
    // --------------------------------------------------------------------------------------------
    // Action
    private void startRide()
    {
        // UI
        progressBar.setVisibility(View.VISIBLE);

        // Flags
        getDeviceLocation();
        rideViewModel.startRide();
    }
    // Response
    private void onStartRideSuccess() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        matchmakingControllerBtn.setVisibility(View.INVISIBLE);
        matchmakingControllerBtn.setEnabled(false);
        rideStatusBar.setText("Ride Started!");
    }
    private void onStartRideFailure() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(RideActivity.this, "Ride Started Failed!", Toast.LENGTH_SHORT).show();
    }

    // --------------------------------------------------------------------------------------------
    //  End Ride Request and Response
    // --------------------------------------------------------------------------------------------
    // Action
    private void onNearDestination() {
        // UI
        matchmakingControllerBtn.setVisibility(View.VISIBLE);
        matchmakingControllerBtn.setEnabled(true);
        matchmakingControllerBtn.setText(R.string.confirmRideEnd);
        rideStatusBar.setText("Near Destination");
    }
    private void confirmEndRide()
    {
        // UI
        progressBar.setVisibility(View.VISIBLE);

        // Flags
        rideViewModel.markDriverAtDestination();
    }
    // Response
    private void onConfirmEndRideSuccess() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        matchmakingControllerBtn.setVisibility(View.VISIBLE);
        matchmakingControllerBtn.setEnabled(true);
        matchmakingControllerBtn.setText("TAKE PAYMENT");
        Double distanceTravelled = (rideViewModel.getRide().getDistanceTravelled()) / 1000;
        String text = String.format("You Travelled %.1f km", distanceTravelled);
        rideStatusBar.setText(text);

        // Flags
    }
    private void onConfirmEndRideFailure() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(RideActivity.this, "End Ride Failed!", Toast.LENGTH_SHORT).show();

        // Flags
    }

    // --------------------------------------------------------------------------------------------
    //  Take Payment Action and Response
    // --------------------------------------------------------------------------------------------
    // Action
    private void takePayment() {
        final String[] m_Text = {""};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ride Completed");
        builder.setMessage("Please Take Payment of " + rideViewModel.getRide().getFare() + " RS");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            // UI
            progressBar.setVisibility(View.VISIBLE);
            // Calling Ride View Model
            m_Text[0] = input.getText().toString();
            rideViewModel.endRideWithPayment(Double.parseDouble(m_Text[0]));
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    // Response
    private void onPaymentSuccess() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        removeMarkersPolyline();
        // Setting UI
        rateRideCard.setAnimation(Util.inFromBottomAnimation(400));
        rateRideCard.setVisibility(View.VISIBLE);

        // Setting onClick Listener
        submitRating.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            float rating = feedbackRatingBar.getRating();

            // Calling Ride View Model
            giveFeedback(rating);
        });
    }
    private void onPaymentFailure() {
        // UI
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(RideActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
    }
    // --------------------------------------------------------------------------------------------
    //  Feedback Action and Response
    // --------------------------------------------------------------------------------------------
    // Action
    private void giveFeedback(float rating)
    {
        rideViewModel.giveRiderFeedback(rating);
    }
    private void onFeedbackSuccess() {
        // Setting UI
        progressBar.setVisibility(View.INVISIBLE);
        rateRideCard.setAnimation(Util.inFromBottomAnimation(400));
        rateRideCard.setVisibility(View.VISIBLE);

        // Flags
        rideViewModel.resetFlags();
    }
    private void onFeedbackFailure() {
        // Setting UI
        progressBar.setVisibility(View.INVISIBLE);
        rateRideCard.setAnimation(Util.inFromBottomAnimation(400));
        rateRideCard.setVisibility(View.VISIBLE);

        // Flags
        rideViewModel.resetFlags();
    }

    // --------------------------------------------------------------------------------------------
    //                                  DATA RELATED OPERATIONS
    // --------------------------------------------------------------------------------------------

    /* Marking User Offline */
    private void markOffline() {
        rideViewModel.markOffline();
    }

    /* Saving User Location */
    private void saveUserLocation(Location location) {
        rideViewModel.setUserCoordinates(location.getLatitude(), location.getLongitude());
    }

    /* Loads user data from database */
    private void loadUserData() {
        rideViewModel.loadUserData();
    }

    /* On User Data Loaded */
    private void onUserDataLoaded() {
        // Setting UI Elements
        navUsername.setText(rideViewModel.getDriver().getUsername());
        navEmail.setText(rideViewModel.getDriver().getEmailAddress());
        matchmakingControllerBtn.setEnabled(true);
        matchmakingControllerBtn.setText("SELECT VEHICLE");
        rideStatusBar.setText("You're Offline");
        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(RideActivity.this, "User data loaded!", Toast.LENGTH_SHORT).show();

        // Flags and Calls
        isUserDataLoaded = true;
        canLoadUserData = false;
        isActive = false;
        checkFlags();
    }

    /* On User Data Failure */
    private void onUserDataFailure() {
        // UI
        Toast.makeText(RideActivity.this, "Data could not be loaded", Toast.LENGTH_SHORT).show();
        matchmakingControllerBtn.setEnabled(false);
        matchmakingControllerBtn.setText("SELECT VEHICLE");
        rideStatusBar.setText("You're Offline");

        // Flags and Calls
        isUserDataLoaded = false;
        canLoadUserData = true;
    }

    /* Checking all the flags to determine status */
    private void checkFlags() {
        // Main Function for Checking all Flags
        Driver tempDriver = rideViewModel.getDriver();
        if (tempDriver.isActive())
        {
            if (tempDriver.getRideRequestStatus() == RideRequest.MS_REQ_ACCEPTED) {
                // Calling Ride View Model to get the Ride Object
                rideViewModel.getStartingRideForDriver();
            } else {
                rideViewModel.setMarkActive(1);
            }
        }
    }

    /* Function for loading User Location Data */
    private void loadUserLocations()
    {
        rideViewModel.isLiveUserLocationsLoaded().observe(this, aBoolean -> {
            if (aBoolean)
            {
                ArrayList<com.example.savaari_driver.entity.Location> mUserLocations = rideViewModel.getUserLocations();
                Log.d(TAG, "loadUserLocations: Started!");

                // Testing Code
                Log.d(TAG, "loadUserLocations: mUserLocations.size(): " + mUserLocations.size());
                for (int i = 0; i < mUserLocations.size(); ++i) {
                    Log.d(TAG, "loadUserLocations: setting Markers");
                    MarkerOptions option = new MarkerOptions()
                            .position(mUserLocations.get(i).toLatLng());
                    googleMap.addMarker(option);
                }
                Toast.makeText(RideActivity.this, "User locations loaded!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(RideActivity.this, "User locations could not be loaded", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // End of Function: loadUserLocations()


    /*
     * Receives autocompleteFragment's result (callback)
     * Gets Place Object using 'getPlaceFromIntent()'
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {

            if (resultCode == AutocompleteActivity.RESULT_OK) {
                assert data != null;
                Place place = Autocomplete.getPlaceFromIntent(data);
//                String title = ((place.getName() == null) ?
//                        ((place.getAddress() == null) ? "" : place.getAddress()) : place.getName());

                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", lat: " + Objects.requireNonNull(place.getLatLng()).latitude
                        + ", lon: " + place.getLatLng().longitude);
                moveCamera(Objects.requireNonNull(place.getLatLng()), true);
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                assert data != null;
                Status status = Autocomplete.getStatusFromIntent(data);
                assert status.getStatusMessage() != null;
                Log.i(TAG, Objects.requireNonNull(status.getStatusMessage()));
            }
            // The user canceled the operation.
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ---------------------------------------------------------------------------------------------
    //                                    MAP FUNCTIONS
    // ---------------------------------------------------------------------------------------------

    /*
     * initMap() if permissions granted
     * else, explicitly ask for permission
     * */
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                initMap();
                return;
            }
        }

        ActivityCompat.requestPermissions(this, permissions,
                LOCATION_PERMISSION_REQUEST_CODE); //Doesn't matter
    }

    /* Callback for when permissions have been granted/denied */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        locationPermissionGranted = false;
                        return;
                    }
                }
                locationPermissionGranted = true;
                initMap();
            }
        }
    }


    /*
     * Prerequisite: Map permissions granted
     * Initializes map fragment
     * */
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(RideActivity.this);

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.directions_api_key))
                    .build();
        }
    }

    /*
     * Callback from initMap()'s getMapAsync()
     * Initialize GoogleMap Object
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(RideActivity.this, "Map is ready", Toast.LENGTH_SHORT).show();
        this.googleMap = googleMap;
        googleMap.setOnPolylineClickListener(this);
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.map_style));

        if (locationPermissionGranted) {

            // Calling the Get Device Location to retrieve the location
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.setOnInfoWindowClickListener(this);

            // Initializing UI
            initUIElements();
        }
    }

    /*
     * Moves camera to param: (latLng, zoom)
     * Adds marker if title specified
     * */
    private void moveCamera(LatLng latLng, boolean oldZoom) {
        if (oldZoom)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, RideActivity.DEFAULT_ZOOM));
        else
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void removeMarkersPolyline() {
        if (pickupMarker != null)
            pickupMarker.remove();
        if (destinationMarker != null)
            destinationMarker.remove();
        if (pickupPolyline != null)
            pickupPolyline.remove();
        if (destinationPolyline != null)
            destinationPolyline.remove();
    }

    /* Method for adding a destination */
    private void setDestination(LatLng latLng, String title)
    {
        moveCamera(latLng, true);

        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        destinationMarker = googleMap.addMarker(options);

        calculateDirections(rideViewModel.getRide().getRideParameters().getPickupLocation().toLatLng(),
                destinationMarker, true);
    }
    /* Get's device location, calls moveCamera()*/
    private void getDeviceLocation() {
        Log.d("getDeviceLocation", "getting device location");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPermissionGranted) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Saving Location
                        Log.d(TAG, "onComplete: found location!");
                        Location currentLocation = (Location) task.getResult();
                        if (currentLocation != null) {
                            driverLocation = new com.example.savaari_driver.entity.Location();
                            driverLocation.setLatitude(currentLocation.getLatitude());
                            driverLocation.setLongitude(currentLocation.getLongitude());
                        }

                        // Calling User Location Save Function
                        try {
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), true);

                            // Saving User Location
                            saveUserLocation(currentLocation);
                            LocationUpdateUtil.saveUserLocation(rideViewModel.getDriver().getCurrentLocation().toLatLng(), RideActivity.this);

                            // Starting Background Location Service
                            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                            LocationUpdateUtil.startLocationService(manager, RideActivity.this);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(RideActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }


    /*
     * Calculates directions from userLocation to marker
     */
    private void calculateDirections(LatLng mUserLocation, Marker marker, boolean sourceToDest){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserLocation.latitude,
                        mUserLocation.longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result, sourceToDest);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }

    /* Given a list of 'checkpoints, this zooms in on the route '*/
    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (googleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    private void addPolylinesToMap(final DirectionsResult result, boolean sourceToDest){

        /*
         * Posting to main thread
         * since this method is called from a different context
         * changes to google map must be made on the same thread as the one it is on
         */
        new Handler(Looper.getMainLooper()).post(() -> {
            Log.d(TAG, "run: result routes: " + result.routes.length);

            /* Loops through possible routes*/
            //for(DirectionsRoute route: result.routes){
            DirectionsRoute route = result.routes[0];
            Log.d(TAG, "run: leg: " + route.legs[0].toString());

            /* get list of LatLng corresponding to each 'checkpoint' along the route */
            List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

            List<LatLng> newDecodedPath = new ArrayList<>();

            // This loops through all the LatLng coordinates of ONE polyline.
            for(com.google.maps.model.LatLng latLng: decodedPath){

                newDecodedPath.add(new LatLng(
                        latLng.lat,
                        latLng.lng
                ));
            }

            /* Add all the 'checkpoints' to the polyline */
            if (sourceToDest) {
                if (destinationPolyline != null) {
                    destinationPolyline.remove();

                    if (pickupPolyline != null) {
                        pickupPolyline.remove();
                    }
                }

                destinationPolyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                destinationPolyline.setColor(ContextCompat.getColor(RideActivity.this, R.color.maps_blue));
                destinationMarker.setSnippet("Duration: " + route.legs[0].duration);
                destinationMarker.showInfoWindow();
            }
            else {
                if (pickupPolyline != null && destinationPolyline != null) {
                    destinationPolyline.remove();
                }
                pickupPolyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                pickupPolyline.setColor(ContextCompat.getColor(RideActivity.this, R.color.success_green));
                pickupMarker.setSnippet("Duration: " + route.legs[0].duration);
                pickupMarker.showInfoWindow();
            }
            zoomRoute(newDecodedPath);
            matchmakingControllerBtn.setEnabled(true);
            //}
        });
    }

    /*
     * Checks if device's Google Play Services are available
     */
    public boolean isServicesOK() {
        Log.d("isServicesOK: ", "checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(RideActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            Log.d("PLAY SERVICES: ", "WORKING");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d("PLAY SERVICES", "ERROR, BUT FIXABLE");

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(RideActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
            return true;
        }
        else {
            Toast.makeText(this, "Error. Map services unavailable", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // ----------------------------------------------------------------------------------------------------
    //                                  IMPLEMENTING INTERFACES
    // ----------------------------------------------------------------------------------------------------
    /* listener for polyline clicks */
    @Override
    public void onPolylineClick(Polyline polyline) {
        // TODO: Highlight more specific details (maybe?)
        //polyline.setColor(ContextCompat.getColor(RideActivity.this, R.color.maps_blue));
        //polyline.setZIndex(1);
    }

    /* Listener for Clicking on Info Window */
    /* Shows user a dialog to open navigation to that marker information window in  google maps */
    @Override
    public void onInfoWindowClick(Marker marker)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(RideActivity.this);
        builder.setMessage("Open Google Maps?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) ->
                {
                    String latitude = String.valueOf(marker.getPosition().latitude);
                    String longitude = String.valueOf(marker.getPosition().longitude);
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    try
                    {
                        if (mapIntent.resolveActivity(RideActivity.this.getPackageManager()) != null)
                        {
                            startActivity(mapIntent);
                        }
                    } catch (NullPointerException e)
                    {
                        Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage() );
                        Toast.makeText(RideActivity.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());

        final AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    // OnNavigationItemSelected Listener from Navigation Bar Click
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawer.closeDrawer(GravityCompat.START);
        Intent i;
        // Main Switch Case for NavBar
        switch (item.getItemId())
        {
            case (R.id.nav_your_trips):
            case (R.id.nav_help):
            case (R.id.nav_wallet):
                break;
            case (R.id.nav_your_documents):
                i = new Intent(RideActivity.this, RegisterActivity.class);
                i.putExtra("FROM_RIDE", true);
                i.putExtra("WHERE", 1);
                startActivity(i);
                break;
            case (R.id.nav_your_vehicles):
                i = new Intent(RideActivity.this, RegisterActivity.class);
                i.putExtra("FROM_RIDE", true);
                i.putExtra("WHERE", 2);
                startActivity(i);
                break;
            case (R.id.nav_settings):
                i = new Intent(RideActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }

    // onClick Listener for Vehicle Selector Adaptor
    @Override
    public void OnClick(int position)
    {
        // Calling RideViewModel to Select this vehicle
        rideViewModel.selectVehicle(vehicleTypeItems.get(position).getIndexInArray());

        // Setting UI
        progressBar.setVisibility(View.VISIBLE);
    } // End of OnClick Function
} // End of Class: RideActivity:
/* Code By Nabeel Danish & Farjad Ilyas */