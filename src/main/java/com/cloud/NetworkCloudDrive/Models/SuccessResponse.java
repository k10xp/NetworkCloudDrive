package com.cloud.NetworkCloudDrive.Models;

public class SuccessResponse {
    private String message;
    private String endpoint;
    private String DateTime;

    public SuccessResponse(String message, String endpoint) {
        this.message = message;
        this.endpoint = endpoint;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
