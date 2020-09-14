package com.lazybattley.phonetracker.HelperClasses;

public class PendingRequestHelperClass {
    private String email;
    private long time;

    public PendingRequestHelperClass() {
    }

    public PendingRequestHelperClass(String email, long time) {
        this.email = email;
        this.time = time;
    }

    public String getEmail() {
        return email;
    }

    public long getTime() {
        return time;
    }
}
