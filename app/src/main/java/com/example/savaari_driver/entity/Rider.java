package com.example.savaari_driver.entity;

public class Rider extends User {

    // ----------------------------------------------------------------
    //                          Main Attributes
    // ----------------------------------------------------------------
    int findStatus;

    // ----------------------------------------------------------------
    //                          Methods
    // ----------------------------------------------------------------
    public Rider() {
        // Empty Constructor
    }
    // Getters and Setters
    public int getFindStatus() {
        return findStatus;
    }
    public void setFindStatus(int findStatus) {
        this.findStatus = findStatus;
    }
}
