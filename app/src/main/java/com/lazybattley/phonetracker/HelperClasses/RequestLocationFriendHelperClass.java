package com.lazybattley.phonetracker.HelperClasses;

public class RequestLocationFriendHelperClass {

    private String currentEmail;
    private long timeSent;

    public RequestLocationFriendHelperClass(String currentEmail, long timeSent) {
        this.currentEmail = currentEmail;
        this.timeSent = timeSent;
    }

    public RequestLocationFriendHelperClass() {
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public long getTimeSent() {
        return timeSent;
    }
}
