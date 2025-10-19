package com.cloud.NetworkCloudDrive.Models;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class JSONResponse {
    private String message;
    private String endpoint;
    private ZonedDateTime dateTime;
    private boolean success;

    public JSONResponse(String message, String endpoint, boolean success) {
        this.message = message;
        this.endpoint = endpoint;
        this.success = success;
        this.dateTime = ZonedDateTime.now(ZoneId.systemDefault());
    }

    public JSONResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
        this.dateTime = ZonedDateTime.now(ZoneId.systemDefault());
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

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
