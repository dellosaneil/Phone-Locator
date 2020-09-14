package com.lazybattley.phonetracker.HelperClasses;

import com.google.android.gms.maps.model.LatLng;

public class CurrentLocationHelperClass {

    private String fullName;
    private LatLng coordinates;
    private long lastUpdated;

    public CurrentLocationHelperClass(String fullName, LatLng coordinates, long lastUpdated) {
        this.fullName = fullName;
        this.coordinates = coordinates;
        this.lastUpdated = lastUpdated;
    }

    public String getFullName() {
        return fullName;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
