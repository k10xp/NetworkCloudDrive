package com.cloud.NetworkCloudDrive.Models;

public class JSONObjectResponse extends JSONResponse {
    private Object object;

    public JSONObjectResponse(String message, Object object,boolean success) {
        super(message, success);
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
