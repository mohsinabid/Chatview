package net.sofitech.chatview;

/**
 * Created by mohsi on 21/11/2015.
 */
public class Location {


    public String latituded,longitude,title;
    public int range = 100;

    public Location(String latituded, String longitude, String title, int range) {
        this.latituded = latituded;
        this.longitude = longitude;
        this.title = title;
        this.range = range;
    }


    public void setLatituded(String latituded) {
        this.latituded = latituded;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public String getLatituded() {
        return latituded;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getTitle() {
        return title;
    }

    public int getRange() {
        return range;
    }
}
