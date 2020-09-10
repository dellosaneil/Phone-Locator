package com.lazybattley.phonetracker.HelperClasses;

public class MainHelperClass {
    private PhoneTrackHelperClass phoneTrackHelperClass;
    private SignUpHelperClass signUpHelperClass;


    public MainHelperClass(PhoneTrackHelperClass phoneTrackHelperClass, SignUpHelperClass signUpHelperClass) {
        this.phoneTrackHelperClass = phoneTrackHelperClass;
        this.signUpHelperClass = signUpHelperClass;
    }

    public MainHelperClass() {
    }

    public PhoneTrackHelperClass getPhoneTrackHelperClass() {
        return phoneTrackHelperClass;
    }

    public SignUpHelperClass getSignUpHelperClass() {
        return signUpHelperClass;
    }
}
