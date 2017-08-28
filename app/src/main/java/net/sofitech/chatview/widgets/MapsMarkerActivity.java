package net.sofitech.chatview.widgets;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chootdev.recycleclick.RecycleClick;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.AddressComponent;
import com.seatgeek.placesautocomplete.model.AddressComponentType;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import net.sofitech.chatview.GPSTracker;
import net.sofitech.chatview.MainActivity;
import net.sofitech.chatview.R;
import net.sofitech.chatview.model.Location;
import net.sofitech.chatview.nearPlaces.MainController;
import net.sofitech.chatview.nearPlaces.MainControllerImpl;
import net.sofitech.chatview.nearPlaces.VenueModel;
import net.sofitech.chatview.nearPlaces.VenueRecyclerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by mohsi on 23/08/2017.
 */
public class MapsMarkerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public GPSTracker gps;
    double lat, lng;
    String name;
    PlacesAutocompleteTextView placesAutocomplete;
    Button btnproceed, searchBtn;
    Geocoder geocoder;
    String city;
    private GoogleMap map;
    TextView sendCurrentLocation;
    ArrayList<Location> locations;
    @Override
    protected void onResume() {
        super.onResume();

    }

    private static final String[] COUNTRIES = new String[]{
            "Belgium", "France", "Italy", "Germany", "Spain"
    };

    private List<VenueModel> venuesList;
    private RecyclerView.Adapter venuesRecyclerAdapter;
    private MainController mainController;
    RecyclerView mRecyclerView;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locations= new ArrayList<>();

         mRecyclerView = (RecyclerView) findViewById(R.id.venues_recycler_view);
        sendCurrentLocation=(TextView)findViewById(R.id.text);
        linearLayoutManager = new LinearLayoutManager(this);


        SharedPreferences prefs = this.getSharedPreferences("key", Context.MODE_PRIVATE);
        placesAutocomplete = (PlacesAutocompleteTextView) findViewById(R.id.searchloc);
        searchBtn = (Button) findViewById(R.id.Bsearch);
        btnproceed = (Button) findViewById(R.id.proceed);

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
//
//        placesAutocomplete.setAdapter(adapter);

        if (prefs.contains("lat")) {
            String slat = prefs.getString("lat", "");
            String slng = prefs.getString("lng", "");
            lat = Double.parseDouble(slat);
            lng = Double.parseDouble(slng);
            Log.e("Sharedlat", String.valueOf(lat));
            Log.e("Sharedlng", String.valueOf(lng));
        }

        gps = new GPSTracker(this);
        sendCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locations.size()>0)
                {
                    MainActivity.sendLocation=locations;
                    MapsMarkerActivity.this.finish();
                } else
                {
                    Toast.makeText(MapsMarkerActivity.this,"Current Location Incorrect. Try Again!",Toast.LENGTH_LONG).show();
                }

            }
        });
        btnproceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locations.size()>0)
                {
                    MainActivity.sendLocation=locations;
                    MapsMarkerActivity.this.finish();
                } else
                {
                    Toast.makeText(MapsMarkerActivity.this,"Current Location Incorrect. Try Again!",Toast.LENGTH_LONG).show();
                }
            }
        });

        if (gps.canGetLocation()) {

            lat = gps.getLatitude();
            lng = gps.getLongitude();

            Log.e("lat", String.valueOf(lat));
            Log.e("lng", String.valueOf(lng));

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
            Log.e("Error2",e.getLocalizedMessage());
        }

        try {
            if (addresses.size() > 0 && addresses!=null) {

                name = addresses.get(0).getSubLocality() + "," + addresses.get(0).getAddressLine(0).toString();
                String s="Local "+addresses.get(0).getLocality()+
                        " Feature "+addresses.get(0).getFeatureName()+
                " subAd "+addresses.get(0).getSubAdminArea()+
                " subLoc "+addresses.get(0).getSubLocality()+
                " AdLine "+addresses.get(0).getAddressLine(0).toString()+
                " prem "+addresses.get(0).getPremises();
                Log.e("NAme",name);
                Log.e("Data",s);
                venuesList = new ArrayList<>();
                mainController = new MainControllerImpl();
                String sendingName=addresses.get(0).getSubLocality()+", "+addresses.get(0).getFeatureName()+", "+ addresses.get(0).getSubAdminArea();
               Location locationObject=new Location(String.valueOf(lat),String.valueOf(lng),sendingName);
                locations.add(locationObject);


                assert mRecyclerView != null;
                mRecyclerView.setLayoutManager(linearLayoutManager);
                venuesRecyclerAdapter = new VenueRecyclerAdapter(venuesList, MapsMarkerActivity.this);
                mRecyclerView.setAdapter(venuesRecyclerAdapter);

                mainController.getVenuesData(addresses.get(0).getSubAdminArea(), venuesList, venuesRecyclerAdapter, MapsMarkerActivity.this); //initialize the list in an arbitrary location

                RecycleClick.addTo(mRecyclerView).setOnItemClickListener(new RecycleClick.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // YOUR CODE
                        Location locationObject=new Location(venuesList.get(position).getLat(),venuesList.get(position).getLng(),venuesList.get(position).getVenueName());
                        locations.add(locationObject);

                        Toast.makeText(MapsMarkerActivity.this,venuesList.get(position).getCategoriesName(),Toast.LENGTH_LONG).show();
                        MapsMarkerActivity.this.finish();
                    }
                });


            } else {
                // do your staff
                Log.e("Nothing","Found");
            }


        } catch (NullPointerException e1) {
            e1.printStackTrace();
            Log.e("Error", e1.getLocalizedMessage());
        }


        placesAutocomplete.setOnPlaceSelectedListener(
                new OnPlaceSelectedListener() {
                    @Override
                    public void onPlaceSelected(final Place place) {

                        placesAutocomplete.getDetailsFor(place, new DetailsCallback() {
                            @Override
                            public void onSuccess(final PlaceDetails details) {
                                Log.e("test", "details " + details);
                                placesAutocomplete.setText(details.name);
                                for (AddressComponent component : details.address_components) {
                                    for (AddressComponentType type : component.types) {
                                        switch (type) {
                                            case STREET_NUMBER:
                                                break;
                                            case ROUTE:
                                                break;
                                            case NEIGHBORHOOD:
                                                break;
                                            case SUBLOCALITY_LEVEL_1:
                                                break;
                                            case SUBLOCALITY:
                                                break;
                                            case LOCALITY:
                                                Log.e("LongName", component.long_name);
                                                break;
                                            case ADMINISTRATIVE_AREA_LEVEL_1:
                                                Log.e("short Name", component.short_name);
                                                break;
                                            case ADMINISTRATIVE_AREA_LEVEL_2:
                                                break;
                                            case COUNTRY:
                                                break;
                                            case POSTAL_CODE:
                                                Log.e("POst", component.long_name);
                                                break;
                                            case POLITICAL:
                                                break;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(final Throwable failure) {
                                Log.d("test", "failure " + failure);
                            }
                        });

                    }
                }
        );

        placesAutocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (placesAutocomplete.length() > 0) {
                    searchBtn.setEnabled(true);
                    btnproceed.setEnabled(true);
                } else {
                    searchBtn.setEnabled(false);
                    btnproceed.setEnabled(false);
                }
                // Log.e("xxx", "fn called");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title(name));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        map = googleMap;
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);


    }

    public void onSearch(View view) {

        try {
            String location = placesAutocomplete.getText().toString();
            List<Address> addressList = null;
            if (location != null || !location.equals("")) {
                geocoder = new Geocoder(MapsMarkerActivity.this);
                try {
                    addressList = geocoder.getFromLocationName(location, 16);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (addressList != null) {


                    Address address = addressList.get(0);
                    Log.e("Main", address.getAddressLine(0));
                    Log.e("Full Address:", " Admin: " + address.getAdminArea() + " SubAdmin: " + address.getSubAdminArea() + " Local: " + address.getLocality());

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    map.addMarker(new MarkerOptions().position(latLng).title(placesAutocomplete.getText().toString()));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    lat = latLng.latitude;
                    lng = latLng.longitude;
                    Log.e("object", String.valueOf(lat));
                    Log.e("object", String.valueOf(lng));
                     mainController.getVenuesData(address.getLocality(), venuesList, venuesRecyclerAdapter, MapsMarkerActivity.this); //initialize the list in an arbitrary location

                    String sendingName=address.getSubLocality()+", "+address.getFeatureName()+", "+ address.getSubAdminArea();
                    Location locationObject=new Location(String.valueOf(lat),String.valueOf(lng),sendingName);
                    locations.add(locationObject);


                }
                //  Service.datavalue = data;


            } else if (addressList == null) {
                Toast.makeText(MapsMarkerActivity.this, "Location not Found", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapsMarkerActivity.this, "Location not Found", Toast.LENGTH_SHORT).show();

            }


        } catch (NullPointerException e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

}
