package com.lazybattley.phonetracker.HelperClasses;

public class RequestLocationCurrentHelperClass {

    private String email;
    private Long timeSent;
    private String status;

    public RequestLocationCurrentHelperClass(String email, Long timeSent, String status) {
        this.email = email;
        this.timeSent = timeSent;
        this.status = status;
    }

    public RequestLocationCurrentHelperClass() {
    }

    public void setStatus(String status) {
        this.status = status;
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
