package com.lazybattley.phonetracker.HelperClasses;

public class SentRequestHelperClass {

    private String email;
    private Long timeSent;
    private String status;

    public SentRequestHelperClass(String email, Long timeSent, String status) {
        this.email = email;
        this.timeSent = timeSent;
        this.status = status;
    }

    public SentRequestHelperClass() {
    }

    public String getEmail() {
        return email;
    }

    public Long getTimeSent() {
        return timeSent;
    }

    public String getStatus() {
        return status;
    }
}
