package com.lazybattley.phonetracker.HelperClasses;

public class SignUpHelperClass {

    private String email;
    private String fullName;
    private String mainPhone;
    private boolean traceable;
    private boolean activated;

    public SignUpHelperClass(String email, String fullName, String mainPhone, boolean traceable, boolean activated) {
        this.email = email;
        this.fullName = fullName;
        this.mainPhone = mainPhone;
        this.traceable = traceable;
        this.activated = activated;
    }

    public SignUpHelperClass() {
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isTraceable() {
        return traceable;
    }

    public String getMainPhone() {
        return mainPhone;
    }


    public String getEmail() {
        return email;
    }


    public String getFullName() {
        return fullName;
    }

}
