package com.lazybattley.phonetracker.HelperClasses;

public class MainPhoneEmailHelperClass {
    private String email;
    private String mainPhone;

    public MainPhoneEmailHelperClass(String email, String mainPhone) {
        this.email = email;
        this.mainPhone = mainPhone;
    }

    public String getEmail() {
        return email;
    }

    public String getMainPhone() {
        return mainPhone;
    }
}
