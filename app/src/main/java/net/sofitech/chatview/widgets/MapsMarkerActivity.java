package net.sofitech.chatview.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.sofitech.chatview.GPSTracker;
import net.sofitech.chatview.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by mohsi on 23/08/2017.
 */
public class MapsMarkerActivity extends AppCompatActivity implements OnMapReadyCallback{

    public GPSTracker gps;
    double lat,lng;
    String name;
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences prefs = this.getSharedPreferences("key", Context.MODE_PRIVATE);

        if(prefs.contains("lat"))
        {
           String slat= prefs.getString("lat","");
           String slng= prefs.getString("lng","");
            lat= Double.parseDouble(slat);
            lng= Double.parseDouble(slng);
        }

        gps = new GPSTracker(this);

        if (gps.canGetLocation()) {

            lat = gps.getLatitude();
            lng = gps.getLongitude();

            // \n is for new line
             Toast.makeText(MapsMarkerActivity.this, "Your Location is - \nLat: " + lat + "\nLong: " + lng, Toast.LENGTH_LONG).show();
            Log.e("Gps", " is turned oN");

        } else {

              gps.showSettingsAlert();
            Log.e("Gps", " is turned off");
        }

        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0)
        {

            name=addresses.get(0).getLocality()+","+ addresses.get(0).getCountryName();

        }
        else
        {
            // do your staff
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(lat,lng);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title(name));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        CameraUpdate center= CameraUpdateFactory.newLatLng(new LatLng(lat,lng));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);

    }
}
