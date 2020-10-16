package com.lazybattley.phonetracker.Workers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.BATTERY_SERVICE;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;
import static com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RegisterPhoneDashboardActivity.BUILD_ID;

public class PhoneLocationWorkManager extends Worker {
    private static final String TAG = "PhoneLocationWorkManage";
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Context context;
    private LocationCallback locationCallback;
    private DatabaseReference reference;
    private BatteryManager batteryManager;
    private int batteryLevel;
    private long updatedAt;
    private Map<String, Object> updateDevice;
    private ValueEventListener eventListener;
    private boolean activated;
    private  Query query;

    public final String BATTERY_PERCENT = "batteryPercent";
    public final String LATITUDE = "latitude";
    public final String LONGITUDE = "longitude";
    public final String UPDATE_AT = "updatedAt";

    public PhoneLocationWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i(TAG, "PhoneLocationWorkManager: ");
        this.context = context;
        batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL)
                .child(REGISTERED_DEVICES)
                .child(BUILD_ID);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        callback();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork: ");
        startUpdate();
        initializeListener();
        query = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(USER_DETAIL);
        query.addValueEventListener(eventListener);

        return Result.success();
    }

    private void callback() {
        Log.i(TAG, "callback: ");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

                    Log.i(TAG, "onLocationResult: " + Thread.currentThread().getName());
                    updateDevice = new HashMap<>();
                    updatedAt = System.currentTimeMillis();
                    updateDevice.put(BATTERY_PERCENT, batteryLevel);
                    updateDevice.put(LATITUDE, locationResult.getLastLocation().getLatitude());
                    updateDevice.put(LONGITUDE, locationResult.getLastLocation().getLongitude());
                    updateDevice.put(UPDATE_AT, updatedAt);
                    reference.updateChildren(updateDevice);

            }
        };
    }

    private void initializeListener(){
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "onDataChange: ");
                if (snapshot.exists()) {
                    SignUpHelperClass getActive = snapshot.getValue(SignUpHelperClass.class);
                    activated = getActive.isActivated();
                    if(!activated){
                        stopUpdate();
                        query.removeEventListener(eventListener);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }
//




    private void startUpdate() {
        Log.i(TAG, "startUpdate: ");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopUpdate() {
        Log.i(TAG, "stopUpdate: ");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

}


