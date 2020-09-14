package com.lazybattley.phonetracker.HelperClasses;

public class AvailableLocationHelperClass {

    private String fullName;
    private String email;
    private long timeSent;

    public AvailableLocationHelperClass(String fullName, String email, long timeSent) {
        this.fullName = fullName;
        this.email = email;
        this.timeSent = timeSent;
    }

    public AvailableLocationHelperClass() {
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public long getTimeSent() {
        return timeSent;
    }
}
