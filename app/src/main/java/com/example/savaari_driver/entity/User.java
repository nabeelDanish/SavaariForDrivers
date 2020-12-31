package com.example.savaari_driver.entity;

import com.google.android.gms.maps.model.LatLng;

public abstract class User
{
    // Flags
    public static final int DEFAULT_ID = -1;

    // ----------------------------------------------------------------
    //                          Main Attributes
    // ----------------------------------------------------------------
    protected int userID;
    protected String username;
    protected String password;
    protected String emailAddress;
    protected String firstName;
    protected String lastName;
    protected String phoneNo;
    protected Location currentLocation;
    protected float rating;

    // ----------------------------------------------------------------
    //                          Methods
    // ----------------------------------------------------------------

    // Default Constructor
    public User()
    {
        // dummy
        userID = -1;
        rating = (float) 4.4;
    }

    // Getters and Setters
    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmailAddress() {
        return emailAddress;
    }
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPhoneNo() {
        return phoneNo;
    }
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public Location getCurrentLocation() {
        return currentLocation;
    }
    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }
    public float getRating() {
        return rating;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }
}
