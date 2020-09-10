package com.lazybattley.phonetracker.HelperClasses;

public class RequestLocationHelperClass {

    private String email;
    private Long timeSent;
    private String status;

    public RequestLocationHelperClass(String email, Long timeSent, String status) {
        this.email = email;
        this.timeSent = timeSent;
        this.status = status;
    }

    public RequestLocationHelperClass() {
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
