package net.griddb.springbootblog;

import com.toshiba.mwcloud.gs.*;
class Device {

    @RowKey String id;
    Double lat;
    Double lon;

    public Device() {

    }
    public Device(String id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Double getLat() {
        return lat;
    }
    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }
    public void setLon(Double lon) {
        this.lon = lon;
    }


    public String toString() {
        return this.id+","+this.lat+","+this.lon;
    }
}

