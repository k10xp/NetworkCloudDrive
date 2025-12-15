package com.cloud.NetworkCloudDrive.Models;

public class JSONErrorResponse extends JSONResponse {
    private String exception_type;

    public JSONErrorResponse(String message, Exception exception_type) {
        super(message, false);
        this.exception_type = exception_type.getClass().getName();
    }

    public String getException_type() {
        return exception_type;
    }
    public void setException_type(String exception_type) {
        this.exception_type = exception_type;
    }
}
