package com.lazybattley.phonetracker.HelperClasses;

import com.google.android.gms.maps.model.LatLng;

public class OwnPhoneDetailsHelperClass {
    private String deviceName;
    private LatLng coordinates;
    private int batteryLevel;

    public OwnPhoneDetailsHelperClass(String deviceName, LatLng coordinates, int batteryLevel) {
        this.deviceName = deviceName;
        this.coordinates = coordinates;
        this.batteryLevel = batteryLevel;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }
}
