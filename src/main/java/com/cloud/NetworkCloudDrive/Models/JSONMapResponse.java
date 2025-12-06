package com.cloud.NetworkCloudDrive.Models;

import java.util.Map;

public class JSONMapResponse extends JSONResponse {
    private Map<String, ?> map_Of;

    public JSONMapResponse(String message, boolean success, Map<String, ?> map_Of) {
        super(message, success);
        this.map_Of = map_Of;
    }

    public Map<String, ?> getMap_Of() {
        return map_Of;
    }
    public void setMap_Of(Map<String, ?> map_Of) {
        this.map_Of = map_Of;
    }
}
