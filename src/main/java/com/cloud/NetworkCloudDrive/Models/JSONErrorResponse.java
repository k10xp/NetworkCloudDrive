package com.cloud.NetworkCloudDrive.Models;

public class JSONErrorResponse extends JSONResponse {
    private String exception_type;
    private String exception_message;

    public JSONErrorResponse(Exception exception_type, String message) {
        super(message, false);
        this.exception_type = exception_type.getClass().getName();
        this.exception_message = exception_type.getMessage();
    }

    public JSONErrorResponse(Exception exception_type, String formattedString, Object... args) {
        super(formattedString, args);
        this.exception_type = exception_type.getClass().getName();
        this.exception_message = exception_type.getMessage();
    }

    public JSONErrorResponse(Exception exception_type) {
        super(exception_type.getMessage(), false);
        this.exception_type = exception_type.getClass().getName();
        this.exception_message = exception_type.getMessage();
    }

    public String getException_type() {
        return exception_type;
    }
    public void setException_type(String exception_type) {
        this.exception_type = exception_type;
    }
    public String getException_message() {
        return exception_message;
    }
    public void setException_message(String exception_message) {
        this.exception_message = exception_message;
    }
}
