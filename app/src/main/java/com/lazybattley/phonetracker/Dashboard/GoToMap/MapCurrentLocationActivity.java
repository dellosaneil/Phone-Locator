package com.lazybattley.phonetracker.Dashboard.GoToMap;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.GlobalVariables.USERS;

public class MapCurrentLocationActivity extends FragmentActivity implements OnMapReadyCallback, CurrentLocationAdapter.OnPersonClick {

    private GoogleMap mMap;
    private RecyclerView currentLocation_summary;
    private DatabaseReference reference;
    public static final String AVAILABLE_LOCATION = "available_location";
    private List<CurrentLocationHelperClass> locationDetails;
    private List<MainPhoneEmailHelperClass> mainPhoneEmail;
    private String encodedUserEmail;
    private CurrentLocationAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean mapReady = false;
    private boolean dataReady = false;
    private boolean exit = false;
    private boolean track = false;
    private int indexNumber;
    private MarkerOptions marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_current_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        encodedUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace('.', ',');
        reference = FirebaseDatabase.getInstance().getReference(USERS);
        currentLocation_summary = findViewById(R.id.currentLocation_summary);
        adapter = new CurrentLocationAdapter(this);
        currentLocation_summary.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(this);
        currentLocation_summary.setLayoutManager(linearLayoutManager);
        initializeRecyclerView();
    }


    private void initializeRecyclerView() {
        new Thread(() -> {
            setMainPhoneEmail(reference);
        }).start();

    }

    private void setMainPhoneEmail(DatabaseReference reference) {
        mainPhoneEmail = new ArrayList<>();
        Query query = reference.child(encodedUserEmail).child(AVAILABLE_LOCATION);
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
                            if (exit) {
                                finalQuery.removeEventListener(this);
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
            indexNumber = position;
            track = true;
            updateMapFocus();
        } else {
            Toast.makeText(this, "Please wait for awhile data is loading", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateMapFocus() {
        LatLng loc = locationDetails.get((indexNumber)).getCoordinates();
        marker = new MarkerOptions().position(loc).title(locationDetails.get(indexNumber).getFullName());
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 20));
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
        // Add a marker in Sydney and move the camera
        mapReady = true;
    }

    @Override
    public void onBackPressed() {
        exit = true;
        super.onBackPressed();

    }
}