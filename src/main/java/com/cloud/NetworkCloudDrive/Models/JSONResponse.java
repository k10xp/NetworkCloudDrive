package com.cloud.NetworkCloudDrive.Models;

import java.time.Clock;
import java.time.Instant;

public class JSONResponse {
    private String message;
    private String endpoint;
    private Instant dateTime;
    private boolean success;

    public JSONResponse(String message, String endpoint, boolean success) {
        this.message = message;
        this.endpoint = endpoint;
        this.success = success;
        Clock clock = Clock.systemUTC();
        this.dateTime = Instant.now(clock);
    }

    public JSONResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
        Clock clock = Clock.systemUTC();
        this.dateTime = Instant.now(clock);
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

    public Instant getDateTime() {
        return dateTime;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
