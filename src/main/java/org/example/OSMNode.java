package org.example;

import java.util.Map;

public class OSMNode {

    private String id;

    private String lat;

    private String lon;

    private Map<String, String> tags;

    private String version;

    public String getId() {
        return id;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getVersion() {
        return version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}
