package com.lazybattley.phonetracker.HelperClasses;


public class PhoneTrackHelperClass {

    private String email;
    private double latitude;
    private double longitude;
    private boolean active;
    private long updatedAt;
    private int batteryPercent;
    private String deviceName;
    private boolean available;


    public PhoneTrackHelperClass() {
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isActive() {
        return active;
    }

    public String getDeviceName() {
        return deviceName;
    }

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


}
