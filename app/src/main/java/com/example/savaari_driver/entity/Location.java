package com.example.savaari_driver.entity;

import com.google.android.gms.maps.model.LatLng;

public class Location
{
    // Main Attributes
    private Double latitude;
    private Double longitude;
    private Long timestamp;

    // Main Constructors
    public Location() {
        super();
    }
    public Location(Double latitude, Double longitude, Long timestamp) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    // LatLng conversion
    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }
    public void setLatLng(LatLng latLng) {
        latitude = latLng.latitude;
        longitude = latLng.longitude;
    }
}
