package com.lazybattley.phonetracker.HelperClasses;

public class AcceptedUsersHelperClass {

    private String fullName;
    private String email;
    private long timeSent;

    public AcceptedUsersHelperClass(String fullName, String email, long timeSent) {
        this.fullName = fullName;
        this.email = email;
        this.timeSent = timeSent;
    }

    public AcceptedUsersHelperClass() {
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
