package net.sofitech.chatview.nearPlaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VenueModel {

  private String venueName;
  private String venueRating;
  private String venueIcon;
  private String categoriesName;
  private String lat,lng;
  private final static String venueIconSize = "64";

  public VenueModel(String venueName, String venueRating, JSONArray venueCategories, JSONObject JSONObject) {
    this.venueName = venueName;
    this.venueRating = venueRating;
    this.venueIcon = buildIconUri(venueCategories);
    this.categoriesName = Cat(venueCategories);
    this.lat=lat(JSONObject);
    this.lng=lng(JSONObject);
  }

  private String buildIconUri(JSONArray venueCategories) {

    try {
      return venueCategories.getJSONObject(0).getJSONObject("icon").getString("prefix") + venueIconSize
          + venueCategories.getJSONObject(0).getJSONObject("icon").getString("suffix");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String Cat(JSONArray venueCategories) {

    try {
      return venueCategories.getJSONObject(0).getString("name");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String lat(JSONObject venueCategories) {

    try {
      return venueCategories.getString("lat");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String lng(JSONObject venueCategories) {

    try {
      return venueCategories.getString("lng");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }
  public String getVenueName() {
    return venueName;
  }

  public String getVenueRating() {
    return venueRating;
  }


  public String getVenueIcon() {
    return venueIcon;
  }

  public String getCategoriesName() {
    return categoriesName;
  }

  public String getLat() {
    return lat;
  }

  public String getLng() {
    return lng;
  }
}