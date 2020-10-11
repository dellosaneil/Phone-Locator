
package com.lazybattley.phonetracker.Dashboard.GoToMap;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.CurrentLocationHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.CurrentLocationAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ACTIVATED;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;


public class MapCurrentLocationActivity extends FragmentActivity implements OnMapReadyCallback, CurrentLocationAdapter.OnPersonClick, MapViewCurrentLocationInterface, PhoneCurrentLocationInterface {


    private static final String TAG = "MapCurrentLocationActiv";
    private GoogleMap mMap;
    private List<CurrentLocationHelperClass> locationDetails;
    private CurrentLocationAdapter adapter;
    private MarkerOptions[] markers;
    private final String ZOOM_LEVEL = "zoom";
    private SharedPreferences.Editor editor;
    private MapCurrentLocationRecyclerView recyclerViewData;
    private Toast toast;
    private ProgressBar currentLocation_progressBar;
    private int prevActivated = -1, activatedUserIndex = -1 ,zoomLevel;
    private CurrentPhoneLocationMap currentPhoneLocationMap;
    private boolean multipleMarker, swapButtonImage, activatedAUser, tracking, dataReady;
    private FloatingActionButton currentLocation_getCurrentLocation;
    private boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_current_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        currentLocation_getCurrentLocation = findViewById(R.id.currentLocation_getCurrentLocation);
        currentPhoneLocationMap = new CurrentPhoneLocationMap(this, this);
        markers = new MarkerOptions[2];
        currentLocation_progressBar = findViewById(R.id.currentLocation_progressBar);
        Log.i(TAG, "onCreate: ");
        locationDetails = new ArrayList<>();
        recyclerViewInit();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        zoomLevel = preferences.getInt(ZOOM_LEVEL, 16);
        editor = preferences.edit();
        initializeSeekBar();
    }


    @Override
    public void ownerCurrentLocation(LatLng location) {
        markers[1] = new MarkerOptions().position(location).title("Current Location");
        markers[1].icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        multipleMarkerManager();
    }

    public void ownPhoneLocationTracker(View view) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            handleClickCurrentLocation();
        }else{
            if(toast != null){
                toast.cancel();
            }
            toast = Toast.makeText(this, "Please turn on your GPS", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    private void handleClickCurrentLocation() {
        if (!swapButtonImage) {
            currentLocation_getCurrentLocation.setImageResource(R.drawable.cancel_button);
            swapButtonImage = true;
            currentPhoneLocationMap.trackOwnDevice();
            multipleMarker = true;
        } else {
            currentLocation_getCurrentLocation.setImageResource(R.drawable.ic_current_location);
            swapButtonImage = false;
            currentPhoneLocationMap.stopTrackingOwnDevice();
            multipleMarker = false;
        }
    }


    private void multipleMarkerManager() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        mMap.clear();
        mMap.addMarker(markers[1]);
        if (markers[0] != null) {
            mMap.addMarker(markers[0]);
            builder.include(markers[0].getPosition());
        }
        builder.include(markers[1].getPosition());
        LatLngBounds bounds = builder.build();
        if (activatedAUser) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 2000, null);
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(markers[1].getPosition()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markers[1].getPosition(), zoomLevel));
        }
    }


    private void recyclerViewInit() {
//        Log.i(TAG, "recyclerViewInit: ");
        RecyclerView currentLocation_summary = findViewById(R.id.currentLocation_summary);
        adapter = new CurrentLocationAdapter(this);
        currentLocation_summary.setAdapter(adapter);
        currentLocation_summary.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewData = new MapCurrentLocationRecyclerView(this);
        recyclerViewData.beginInitializeRecyclerViewData();


    }


    private void initializeSeekBar() {
//        Log.i(TAG, "initializeSeekBar: ");
        SeekBar currentLocation_zoom = findViewById(R.id.currentLocation_zoom);
        currentLocation_zoom.setProgress(zoomLevel);
        currentLocation_zoom.setMax(19);
        currentLocation_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                zoomLevel = i + 1;
                editor.putInt(ZOOM_LEVEL, zoomLevel);
                editor.apply();
                if (activatedUserIndex != -1) {
                    updateMapFocus();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void setRecyclerViewData(List<CurrentLocationHelperClass> newCurrentLocation) {
        currentLocation_progressBar.setVisibility(View.INVISIBLE);
        locationDetails.clear();
        locationDetails.addAll(newCurrentLocation);
        adapter.updateRecyclerView(locationDetails);
        dataReady = true;
        if (tracking) {
            updateMapFocus();
        }
    }

    @Override
    public void onPersonClick(int position) {
//        Log.i(TAG, "onPersonClick: ");
        if (dataReady) {
//            mMap.clear();
            activatedUserIndex = position;
            updateMapFocus();
            tracking = true;
            activateTracking();
            recyclerViewData.trackDevice(position);
        } else {
            Toast.makeText(this, R.string.current_location_summary_loading, Toast.LENGTH_SHORT).show();
        }
    }

    private void activateTracking() {
//        Log.i(TAG, "activateTracking: ");
        if (prevActivated != -1) {
            deactivateTracker(prevActivated);
            recyclerViewData.removeTracking(prevActivated);
        }
        prevActivated = activatedUserIndex;
        activatedAUser = true;
        String email = locationDetails.get(activatedUserIndex).getFullName();
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference(USERS).child(email).child(USER_DETAIL);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SignUpHelperClass userDetail = snapshot.getValue(SignUpHelperClass.class);
                boolean isTraceable = userDetail.isTraceable();
                if (isTraceable) {
                    Map<String, Object> tempMap = new HashMap<>();
                    tempMap.put(ACTIVATED, true);
                    reference.updateChildren(tempMap);
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(MapCurrentLocationActivity.this, userDetail.getFullName() + " is now activated.", Toast.LENGTH_SHORT);
                    adapter.setLiveTracking(activatedUserIndex);
                } else {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(MapCurrentLocationActivity.this, userDetail.getFullName() + " did not turn on application.", Toast.LENGTH_SHORT);
                    adapter.setLiveTracking(-1);
                }
                toast.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deactivateTracker() {
        Log.i(TAG, "deactivateTracker: ");
        String email = locationDetails.get(activatedUserIndex).getFullName();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(email).child(USER_DETAIL);
        Map<String, Object> deactivate = new HashMap<>();
        deactivate.put(ACTIVATED, false);
        reference.updateChildren(deactivate);
    }

    private void deactivateTracker(int prevNumber) {
        Log.i(TAG, "deactivateTracker: ");
        String email = locationDetails.get(prevNumber).getFullName();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(email).child(USER_DETAIL);
        Map<String, Object> deactivate = new HashMap<>();
        deactivate.put(ACTIVATED, false);
        reference.updateChildren(deactivate);
    }


    private void updateMapFocus() {
        LatLng loc = locationDetails.get((activatedUserIndex)).getCoordinates();
        markers[0] = new MarkerOptions().position(loc).title(locationDetails.get(activatedUserIndex).getFullName());
        if (!multipleMarker) {
            mMap.clear();
            mMap.addMarker(markers[0]);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
        }
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
        Log.i(TAG, "onMapReady: ");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "onBackPressed: ");

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activatedAUser) {
            deactivateTracker();
            activatedAUser = false;
        }
        if(prevActivated != -1){
            recyclerViewData.removeTracking(prevActivated);
        }
        recyclerViewData.removeAvailableLocationCallback();
        recyclerViewData.removeAvailabilityChecker();
        currentPhoneLocationMap.stopTrackingOwnDevice();
        prevActivated = -1;
        Log.i(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentPhoneLocationMap.stopTrackingOwnDevice();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!activatedAUser && activatedUserIndex != -1) {
            activateTracking();
        }
        if (multipleMarker) {
            currentPhoneLocationMap.trackOwnDevice();
        }
        if(!first && activatedUserIndex != -1){
            recyclerViewData.trackDevice(activatedUserIndex);
        }
        if(!first){
            recyclerViewData.attachAvailabilityChecker();
        }
        first = false;
        Log.i(TAG, "onResume: ");
    }

    public void backPressed(View view) {
        Log.i(TAG, "backPressed: ");
        onBackPressed();
    }


    //    ******************************************************* ******************************************************* *******************************************************

    public static class CurrentPhoneLocationMap {
        private FusedLocationProviderClient fusedLocationProviderClient;
        private LocationRequest locationRequest;
        private Context context;
        private LocationCallback locationCallback;
        private PhoneCurrentLocationInterface currentLocation;

        public CurrentPhoneLocationMap(Context context, PhoneCurrentLocationInterface currentLocation) {
            this.currentLocation = currentLocation;
            this.context = context;
            locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            callback();
        }

        private void callback() {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    currentLocation.ownerCurrentLocation(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
                }
            };
        }

        private void trackOwnDevice() {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        }

        private void stopTrackingOwnDevice() {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}

interface MapViewCurrentLocationInterface {
    void setRecyclerViewData(List<CurrentLocationHelperClass> currentLocationList);
}

interface PhoneCurrentLocationInterface {
    void ownerCurrentLocation(LatLng location);
}


