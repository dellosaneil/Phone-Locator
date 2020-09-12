package com.lazybattley.phonetracker.HelperClasses;

public class RequestLocationFriendHelperClass {

    private String email;
    private long timeSent;

    public RequestLocationFriendHelperClass(String email, long timeSent) {
        this.email = email;
        this.timeSent = timeSent;
    }

    public RequestLocationFriendHelperClass() {
    }

    public String getEmail() {
        return email;
    }

    public long getTimeSent() {
        return timeSent;
    }
}
