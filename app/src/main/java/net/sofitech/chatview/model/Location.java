package net.sofitech.chatview.model;

/**
 * Created by mohsi on 25/08/2017.
 */
public class Location {
   public String lat,lng,name;

    public Location(String lat, String lng, String name) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public String getName() {
        return name;
    }
}
