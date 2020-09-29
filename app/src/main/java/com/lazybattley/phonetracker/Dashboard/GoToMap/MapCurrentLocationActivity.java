package com.lazybattley.phonetracker.Dashboard.GoToMap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.CurrentLocationHelperClass;
import com.lazybattley.phonetracker.HelperClasses.MainPhoneEmailHelperClass;
import com.lazybattley.phonetracker.HelperClasses.PhoneTrackHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.CurrentLocationAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ACTIVATED;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;


public class MapCurrentLocationActivity extends FragmentActivity implements OnMapReadyCallback, CurrentLocationAdapter.OnPersonClick, MapViewCurrentLocationInterface {


    private static final String TAG = "MapCurrentLocationActiv";
    private GoogleMap mMap;
    private List<CurrentLocationHelperClass> locationDetails;
    private CurrentLocationAdapter adapter;
    private boolean dataReady = false;
    private int indexNumber;
    private MarkerOptions marker;
    private int zoomLevel;
    private final String ZOOM_LEVEL = "zoom";
    private SharedPreferences.Editor editor;
    private boolean tracking;
    private MapCurrentLocationRecyclerView recyclerView;
    private Toast toast;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_current_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.i(TAG, "onCreate: ");
        handler = HandlerCompat.createAsync(Looper.getMainLooper());
        locationDetails = new ArrayList<>();
        recyclerViewInit();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        zoomLevel = preferences.getInt(ZOOM_LEVEL, 16);
        editor = preferences.edit();
        initializeSeekBar();
    }


    private void recyclerViewInit() {
        Log.i(TAG, "recyclerViewInit: ");
        RecyclerView currentLocation_summary = findViewById(R.id.currentLocation_summary);
        adapter = new CurrentLocationAdapter(this);
        currentLocation_summary.setAdapter(adapter);
        currentLocation_summary.setLayoutManager(new LinearLayoutManager(this));
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        recyclerView = new MapCurrentLocationRecyclerView(this, handler, executorService);
        recyclerView.executeThread();
//        new Thread(recyclerView).start();
//        executorService.execute(recyclerView);

    }


    private void initializeSeekBar() {
        Log.i(TAG, "initializeSeekBar: ");
        SeekBar currentLocation_zoom = findViewById(R.id.currentLocation_zoom);
        currentLocation_zoom.setProgress(zoomLevel);
        currentLocation_zoom.setMax(19);
        currentLocation_zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                zoomLevel = i + 1;
                editor.putInt(ZOOM_LEVEL, zoomLevel);
                editor.apply();
                if (locationDetails.size() != 0) {
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
    public void setRecyclerView(List<CurrentLocationHelperClass> newCurrentLocation) {
        Log.i(TAG, "setRecyclerView: ");
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
        Log.i(TAG, "onPersonClick: ");
        if (dataReady) {
            mMap.clear();
            indexNumber = position;
            updateMapFocus();
            tracking = true;
            activateTracking();
        } else {
            Toast.makeText(this, R.string.current_location_summary_loading, Toast.LENGTH_SHORT).show();
        }
    }

    private void activateTracking() {
        Log.i(TAG, "activateTracking: ");
        String email = locationDetails.get(indexNumber).getFullName();
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
                } else {
                    toast = Toast.makeText(MapCurrentLocationActivity.this, userDetail.getFullName() + " did not turn on application.", Toast.LENGTH_SHORT);
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
        String email = locationDetails.get(indexNumber).getFullName();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(email).child(USER_DETAIL);
        Map<String, Object> deactivate = new HashMap<>();
        deactivate.put(ACTIVATED, false);
        reference.updateChildren(deactivate);
    }


    private void updateMapFocus() {
        Log.i(TAG, "updateMapFocus: ");
        LatLng loc = locationDetails.get((indexNumber)).getCoordinates();
        marker = new MarkerOptions().position(loc).title(locationDetails.get(indexNumber).getFullName());
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
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
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deactivateTracker();
        recyclerView.removeListener();
        Log.i(TAG, "onPause: ");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    //    ******************************************************* ******************************************************* *******************************************************
    private static class MapCurrentLocationRecyclerView implements Runnable {
        private DatabaseReference reference;
        private List<String> availableLocationEmailList;
        private List<MainPhoneEmailHelperClass> mainPhoneEmailList;
        private List<CurrentLocationHelperClass> currentLocationList;
        private MapViewCurrentLocationInterface mapViewInterface;
        private ValueEventListener locationCallback;
        private Query[] queries;
        private Handler handler;
        private Executor executor;


        public MapCurrentLocationRecyclerView(MapViewCurrentLocationInterface mapViewInterface, Handler handler, Executor executor) {
            this.reference = FirebaseDatabase.getInstance().getReference(USERS);
            this.mapViewInterface = mapViewInterface;
            this.handler = handler;
            this.executor = executor;
//            test();
        }


        private void beginThread(DatabaseReference reference) {
            String AVAILABLE_LOCATION = "available_location";
            Log.i(TAG, "beginThread: " + (Looper.myLooper() == Looper.getMainLooper()));
            Query query = reference.child(ENCODED_EMAIL).child(AVAILABLE_LOCATION);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        availableLocationEmailList = new ArrayList<>();
                        for (DataSnapshot emailList : snapshot.getChildren()) {
                            availableLocationEmailList.add(emailList.getKey());
                        }
                        setPhoneEmailList(reference, availableLocationEmailList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void setPhoneEmailList(DatabaseReference reference, List<String> emailList) {
            final int[] count = {0};
            Log.i(TAG, "setPhoneEmailList: " + (Looper.myLooper() == Looper.getMainLooper()));
            mainPhoneEmailList = new ArrayList<>();
            Query query;
            for (String email : emailList) {
                query = reference.child(email).child(USER_DETAIL);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i(TAG, "onDataChange: " + (Looper.myLooper() == Looper.getMainLooper()));
                        SignUpHelperClass friendDetail = snapshot.getValue(SignUpHelperClass.class);
                        String fullName = friendDetail.getFullName();
                        String friendEmail = friendDetail.getEmail().replace('.', ',');
                        String mainPhone = friendDetail.getMainPhone();
                        boolean canTrack = friendDetail.isTraceable();
                        mainPhoneEmailList.add(new MainPhoneEmailHelperClass(fullName, friendEmail, mainPhone, canTrack));
                        count[0]++;
                        if (count[0] == emailList.size()) {
                            recyclerViewData(reference, mainPhoneEmailList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

        private void recyclerViewData(DatabaseReference reference, List<MainPhoneEmailHelperClass> mainPhoneEmailList) {
            currentLocationList = new ArrayList<>();
            queries = new Query[mainPhoneEmailList.size()];
            for (int i = 0; i < mainPhoneEmailList.size(); i++) {
                queries[i] = reference.child(mainPhoneEmailList.get(i).getEmail())
                        .child(REGISTERED_DEVICES)
                        .child(mainPhoneEmailList.get(i).getMainPhone());
                queries[i].addValueEventListener(locationCallback);
            }
        }


        private void removeListener() {
            for (Query q : queries) {
                q.removeEventListener(locationCallback);
            }
        }

        private void initializeCallback() {
            Log.i(TAG, "Thread: " + (Looper.myLooper() == Looper.getMainLooper()));
            locationCallback = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    new Thread(() -> {
                        Log.i(TAG, "Thread: " + (Looper.myLooper() == Looper.getMainLooper()));
                        PhoneTrackHelperClass userLocationDetails = snapshot.getValue(PhoneTrackHelperClass.class);
                        String fullName = userLocationDetails.getEmail();
                        LatLng coordinates = new LatLng(userLocationDetails.getLatitude(), userLocationDetails.getLongitude());
                        long updatedAt = userLocationDetails.getUpdatedAt();
                        boolean traceable = userLocationDetails.isAvailable();
                        if (currentLocationList.size() == mainPhoneEmailList.size()) {
                            int replaceIndex = checkIndexNumber(userLocationDetails.getEmail());
                            currentLocationList.set(replaceIndex, new CurrentLocationHelperClass(fullName, coordinates, updatedAt, traceable));
                        } else {
                            currentLocationList.add(new CurrentLocationHelperClass(fullName, coordinates, updatedAt, traceable));
                        }
                        handler.post(() -> {
                                mapViewInterface.setRecyclerView(currentLocationList);
                        });

                    }).start();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
        }


        private int checkIndexNumber(String email) {
            for (int i = 0; i < mainPhoneEmailList.size(); i++) {
                if (email.equals(mainPhoneEmailList.get(i).getEmail())) {
                    return i;
                }
            }
            return -1;
        }


        @Override
        public void run() {
            Log.i(TAG, "Thread RUN: " + (Looper.myLooper() == Looper.getMainLooper()));
            initializeCallback();
            beginThread(reference);
        }

        private void executeThread() {
            executor.execute(this);
        }
    }
}

interface MapViewCurrentLocationInterface {
    void setRecyclerView(List<CurrentLocationHelperClass> currentLocationList);
}


