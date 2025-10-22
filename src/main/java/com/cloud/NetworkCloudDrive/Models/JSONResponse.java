package com.cloud.NetworkCloudDrive.Models;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class JSONResponse {
    private String message;
    private final ZonedDateTime dateTime;
    private boolean success;

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
}
