package com.lazybattley.phonetracker.HelperClasses;

import com.google.android.gms.maps.model.LatLng;

public class CurrentLocationHelperClass {

    private String fullName;
    private LatLng coordinates;
    private long lastUpdated;
    private boolean traceable;


    public CurrentLocationHelperClass(String fullName, LatLng coordinates, long lastUpdated, boolean traceable) {
        this.fullName = fullName;
        this.coordinates = coordinates;
        this.lastUpdated = lastUpdated;
        this.traceable = traceable;
    }

    public boolean isTraceable() {
        return traceable;
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

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setTraceable(boolean traceable) {
        this.traceable = traceable;
    }
}
