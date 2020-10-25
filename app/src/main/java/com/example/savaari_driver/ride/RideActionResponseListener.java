package com.example.savaari_driver.ride;

import android.content.Intent;

public interface RideActionResponseListener {
    void onDataLoaded(Intent intent);
    void onLocationsLoaded(Intent intent);
}
