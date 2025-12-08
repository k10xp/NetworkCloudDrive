package com.cloud.NetworkCloudDrive.Models;

public class JSONErrorResponse extends JSONResponse {
    private String exception_type;

    public JSONErrorResponse(String message, String exception_type, boolean success) {
        super(message, success);
        this.exception_type = exception_type;
    }

    public String getException_type() {
        return exception_type;
    }
    public void setException_type(String exception_type) {
        this.exception_type = exception_type;
    }
}
