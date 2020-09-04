package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import com.google.android.gms.maps.model.LatLng;

public class PhoneTrackHelperClass {

    private LatLng location;
    private boolean isActive;
    private String phoneModel;

    public PhoneTrackHelperClass(LatLng location, boolean isActive,String phoneModel) {
        this.location = location;
        this.isActive = isActive;
        this.phoneModel = phoneModel;
    }

    public PhoneTrackHelperClass() {}

    public String getPhoneModel() {
        return phoneModel;
    }

    public LatLng getLocation(){
        return location;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
