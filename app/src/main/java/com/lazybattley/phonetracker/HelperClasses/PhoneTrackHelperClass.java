package com.lazybattley.phonetracker.HelperClasses;

import com.google.android.gms.maps.model.LatLng;

public class PhoneTrackHelperClass {

    private String email;
    private double latitude;
    private double longitude;
    private boolean isActive;
    private long updatedAt;
    private int batteryPercent;
    private String deviceName;


    public PhoneTrackHelperClass(String email, LatLng location, boolean isActive, long updatedAt, int batteryPercent) {
        this.email = email;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
        this.batteryPercent = batteryPercent;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public PhoneTrackHelperClass() {}

    public String getEmail() {
        return email;
    }

    public int getBatteryPercent() {
        return batteryPercent;
    }

    public long getUpdatedAt() {
        return updatedAt;
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
