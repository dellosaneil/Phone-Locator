package com.lazybattley.phonetracker.LogInSignUp.SignUp;

public class SignUpHelperClass {

    private String uniqueID;
    private String email;
    private String fullName;

    public SignUpHelperClass(String uniqueID, String email, String fullName) {
        this.uniqueID = uniqueID;
        this.email = email;
        this.fullName = fullName;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
