package com.cloud.NetworkCloudDrive.Models;

public class JSONErrorResponse extends JSONResponse {
    private String exceptionType;

    public JSONErrorResponse(String message, String exceptionType, boolean success) {
        super(message, success);
        this.exceptionType = exceptionType;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }
}
