package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class PhoneTrackHelperClass {

    private double latitude;
    private double longitude;
    private boolean isActive;
    private String phoneModel;
    private long updatedAt;


    public PhoneTrackHelperClass(LatLng location, boolean isActive, String phoneModel, long updatedAt) {
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.isActive = isActive;
        this.phoneModel = phoneModel;
        this.updatedAt = updatedAt;
    }

    public PhoneTrackHelperClass() {}

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
