package com.lazybattley.phonetracker.Dashboard.GoToMap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.AvailableLocationHelperClass;
import com.lazybattley.phonetracker.HelperClasses.CurrentLocationHelperClass;
import com.lazybattley.phonetracker.HelperClasses.MainPhoneEmailHelperClass;
import com.lazybattley.phonetracker.HelperClasses.PhoneTrackHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.CurrentLocationAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.GlobalVariables.USERS;

public class MapCurrentLocationActivity extends FragmentActivity implements OnMapReadyCallback, CurrentLocationAdapter.OnPersonClick {

    private GoogleMap mMap;
    private DatabaseReference reference;
    public static final String AVAILABLE_LOCATION = "available_location";
    private List<CurrentLocationHelperClass> locationDetails;
    private List<MainPhoneEmailHelperClass> mainPhoneEmail;
    private CurrentLocationAdapter adapter;
    private boolean mapReady = false;
    private boolean dataReady = false;
    private boolean exit = false;
    private boolean track = false;
    private int indexNumber;
    private MarkerOptions marker;
    private volatile int zoomLevel;
    public static final String ZOOM_LEVEL = "zoom";
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_current_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        zoomLevel = sharedPreferences.getInt(ZOOM_LEVEL, 16);
        reference = FirebaseDatabase.getInstance().getReference(USERS);
        RecyclerView currentLocation_summary = findViewById(R.id.currentLocation_summary);
        adapter = new CurrentLocationAdapter(this);
        currentLocation_summary.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        currentLocation_summary.setLayoutManager(linearLayoutManager);
        initializeRecyclerView();
        initializeSeekBar();
    }

    private void initializeSeekBar() {
        SeekBar currentLocation_zoom = findViewById(R.id.currentLocation_zoom);
        currentLocation_zoom.setProgress(zoomLevel);
        currentLocation_zoom.setMax(19);
        currentLocation_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                zoomLevel = i + 1;
                editor.putInt(ZOOM_LEVEL, zoomLevel);
                editor.apply();
                updateMapFocus();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void initializeRecyclerView() {
        new Thread(() -> {
            setMainPhoneEmail(reference);
        }).start();
    }

    private void setMainPhoneEmail(DatabaseReference reference) {
        mainPhoneEmail = new ArrayList<>();
        Query query = reference.child(ENCODED_EMAIL).child(AVAILABLE_LOCATION);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot phoneEmail : snapshot.getChildren()) {
                        AvailableLocationHelperClass availableLocations = phoneEmail.getValue(AvailableLocationHelperClass.class);
                        mainPhoneEmail.add(new MainPhoneEmailHelperClass(availableLocations.getEmail(), availableLocations.getMainPhone()));
                    }
                    setCoordinates(reference);
                } else {
                    Toast.makeText(MapCurrentLocationActivity.this, "No available users.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setCoordinates(DatabaseReference reference) {
        locationDetails = new ArrayList<>();
        Query query;
        for (MainPhoneEmailHelperClass essentials : mainPhoneEmail) {
            if (!essentials.getMainPhone().equals("No Phone")) {
                query = reference.child(essentials.getEmail()).child(REGISTERED_DEVICES).child(essentials.getMainPhone());
                Query finalQuery = query;
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (exit) {
                                finalQuery.removeEventListener(this);
                            }
                            PhoneTrackHelperClass currentLocations = snapshot.getValue(PhoneTrackHelperClass.class);
                            int index = checkIndexNumber(currentLocations.getEmail());
                            if (locationDetails.size() > index) {
                                locationDetails.remove(index);
                            }
                            locationDetails.add(index, new CurrentLocationHelperClass(currentLocations.getEmail(),
                                    new LatLng(currentLocations.getLatitude(), currentLocations.getLongitude()),
                                    currentLocations.getUpdatedAt()));
                            if (locationDetails.size() == mainPhoneEmail.size()) {
                                adapter.setData(locationDetails);
                                dataReady = true;
                            }
                            if (mapReady && track && (index == indexNumber)) {
                                if (marker != null) {
                                    mMap.clear();
                                }
                                updateMapFocus();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MapCurrentLocationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "No Main Phone Found", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onPersonClick(int position) {
        if (dataReady) {
            mMap.clear();
            indexNumber = position;
            track = true;
            updateMapFocus();
        } else {
            Toast.makeText(this, R.string.current_location_summary_loading, Toast.LENGTH_SHORT).show();
        }

    }

    private void updateMapFocus() {
        LatLng loc = locationDetails.get((indexNumber)).getCoordinates();
        marker = new MarkerOptions().position(loc).title(locationDetails.get(indexNumber).getFullName());
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
    }


    private int checkIndexNumber(String coordinateEmail) {
        for (int i = 0; i < mainPhoneEmail.size(); i++) {
            if (coordinateEmail.equals(mainPhoneEmail.get(i).getEmail())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mapReady = true;
    }

    @Override
    public void onBackPressed() {
        exit = true;
        finish();
        super.onBackPressed();

    }
}