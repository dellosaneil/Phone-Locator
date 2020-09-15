package com.lazybattley.phonetracker.HelperClasses;

public class AvailableLocationHelperClass {

    private String fullName;
    private String email;
    private long timeSent;
    private String mainPhone;

    public AvailableLocationHelperClass(String fullName, String email, long timeSent, String mainPhone) {
        this.fullName = fullName;
        this.email = email;
        this.timeSent = timeSent;
        this.mainPhone = mainPhone;
    }

    public AvailableLocationHelperClass() {}

    public String getMainPhone() {
        return mainPhone;
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
