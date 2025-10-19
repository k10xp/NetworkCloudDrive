package com.cloud.NetworkCloudDrive.Models;

public class JSONResponse {
    private String message;
    private String endpoint;
    private String dateTime;
    private boolean success;

    public JSONResponse(String message, String endpoint, boolean success) {
        this.message = message;
        this.endpoint = endpoint;
        this.success = success;
    }

    public JSONResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
