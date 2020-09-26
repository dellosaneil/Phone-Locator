package com.lazybattley.phonetracker.HelperClasses;

public class MainPhoneEmailHelperClass {
    private String fullName;
    private String email;
    private String mainPhone;
    private boolean traceable;

    public MainPhoneEmailHelperClass(String fullName, String email, String mainPhone, boolean traceable) {
        this.fullName = fullName;
        this.email = email;
        this.mainPhone = mainPhone;
        this.traceable = traceable;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isTraceable() {
        return traceable;
    }

    public String getEmail() {
        return email;
    }

    public String getMainPhone() {
        return mainPhone;
    }
}
