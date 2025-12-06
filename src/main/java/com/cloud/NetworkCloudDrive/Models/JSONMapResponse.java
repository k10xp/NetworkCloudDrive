package com.cloud.NetworkCloudDrive.Models;

import java.util.Map;

public class JSONMapResponse extends JSONResponse {
    private Map<String, ?> map_of;

    public JSONMapResponse(String message, boolean success, Map<String, ?> map_of) {
        super(message, success);
        this.map_of = map_of;
    }

    public Map<String, ?> getMap_of() {
        return map_of;
    }
    public void setMap_of(Map<String, ?> map_of) {
        this.map_of = map_of;
    }
}
