package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.os.HandlerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazybattley.phonetracker.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RegisterPhoneDashboardActivity.BUILD_ID;
import static com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RegisterPhoneDashboardActivity.FROM_ACTIVITY;
import static com.lazybattley.phonetracker.GlobalVariables.IS_REGISTERED;
import static com.lazybattley.phonetracker.GlobalVariables.LOCATION_REQUEST_CODE;
import static com.lazybattley.phonetracker.GlobalVariables.LOCATION_REQUEST_FOREGROUND_CODE;
import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.GlobalVariables.USERS;
import static com.lazybattley.phonetracker.PersistentNotification.CHANNEL_ID;

public class PhoneLocationService extends Service implements BatteryDrainHandler {

    private volatile static boolean state;
    private ExecutorService executorService;
    private Handler handler;
    private PhoneLocationTracker locationTracker;

    public static final String ACTIVE = "active";
    public static final String BATTERY_PERCENT = "batteryPercent";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String UPDATE_AT = "updatedAt";

    private static final String TAG = "PhoneLocationService";
    
    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newFixedThreadPool(1);
        handler = HandlerCompat.createAsync(Looper.getMainLooper());

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isFromActivity = intent.getBooleanExtra(FROM_ACTIVITY, false);
        Log.i(TAG, "onStartCommand: " + isFromActivity);
        if(isFromActivity){
            state = intent.getBooleanExtra(IS_REGISTERED, false);
            String buildId = intent.getStringExtra(BUILD_ID);
            locationTracker = new PhoneLocationTracker(this, buildId, executorService, handler, this, this);
            Intent notificationIntent = new Intent(this, RegisterPhoneDashboardActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, LOCATION_REQUEST_CODE, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.location_service_turned_on))
                    .setContentText(getString(R.string.location_service_tracked))
                    .setSmallIcon(R.drawable.current_location)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(LOCATION_REQUEST_FOREGROUND_CODE, notification);
            handleEvent();
        }
        return START_STICKY;
    }

    private void handleEvent() {
        if (state) {
            locationTracker.runThread();
        } else {
            state = false;
            stopSelf();
            executorService.shutdownNow();
            stopForeground(true);
        }
    }


    @Override
    public void batteryDrained(boolean off) {
        state = off;
        handleEvent();
    }

//************************************************************************************************************************************************************

    public static class PhoneLocationTracker implements Runnable {
        private FusedLocationProviderClient fusedLocationProviderClient;
        private LocationRequest locationRequest;
        private Context context;
        private LocationCallback locationCallback;
        private DatabaseReference reference;
        private BatteryManager batteryManager;
        private int batteryLevel;
        private long updatedAt;
        private Executor executor;
        private Handler handler;
        private Map<String, Object> updateDevice;
        private BatteryDrainHandler listener;

        @Override
        public void run() {
            if (state) {
                startUpdate();
            } else {
                stopUpdate();
            }
        }

        public void runThread() {
            executor.execute(this);
        }


        public PhoneLocationTracker(Context context, String buildId, Executor executor, Handler handler, PhoneLocationService phoneLocationService, BatteryDrainHandler listener) {
            this.listener = listener;
            this.executor = executor;
            this.handler = handler;
            this.context = context;
            batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            reference = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(REGISTERED_DEVICES).child(buildId);
            locationRequest = new LocationRequest();
            locationRequest.setInterval(3000);
            locationRequest.setFastestInterval(2500);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            callback();
        }


        private void callback() {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    if (!state) {
                        stopUpdate();
                    } else if (batteryLevel < 30) {
                        drainedBattery();
                    } else {
                        updateDevice = new HashMap<>();
                        updatedAt = System.currentTimeMillis();
                        updateDevice.put(ACTIVE, true);
                        updateDevice.put(BATTERY_PERCENT, batteryLevel);
                        updateDevice.put(LATITUDE, locationResult.getLastLocation().getLatitude());
                        updateDevice.put(LONGITUDE, locationResult.getLastLocation().getLongitude());
                        updateDevice.put(UPDATE_AT, updatedAt);
                        reference.updateChildren(updateDevice);
                    }
                }
            };
        }


        private void startUpdate() {
            handler.post(() -> {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (state) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                } else {
                    stopUpdate();
                }
            });
        }

        private void stopUpdate() {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }

        private void drainedBattery() {
            updateDevice = new HashMap<>();
            updateDevice.put(ACTIVE, false);
            reference.updateChildren(updateDevice);
            listener.batteryDrained(false);
        }
    }
}

interface BatteryDrainHandler {
    void batteryDrained(boolean off);
}


