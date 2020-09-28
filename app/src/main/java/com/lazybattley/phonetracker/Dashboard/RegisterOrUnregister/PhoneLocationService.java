package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.os.HandlerCompat;

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
import com.lazybattley.phonetracker.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ACTIVATED;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.AVAILABLE;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.IS_REGISTERED;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.LOCATION_REQUEST_CODE;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.LOCATION_REQUEST_FOREGROUND_CODE;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.TRACEABLE;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;
import static com.lazybattley.phonetracker.PersistentNotification.CHANNEL_ID;

public class PhoneLocationService extends Service implements BatteryDrainHandler {

    private boolean state;
    private boolean activated;
    private static final String TAG = "PhoneLocationService";
    private PhoneLocationTracker locationTracker;
    @Override
    public void onCreate() {
        super.onCreate();
        @SuppressLint("HardwareIds") String buildId = Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        locationTracker = new PhoneLocationTracker(this, buildId, this);
        trackerActivated();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        state = intent.getBooleanExtra(IS_REGISTERED, false);
        Intent notificationIntent = new Intent(this, RegisterPhoneDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, LOCATION_REQUEST_CODE, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.location_service_turned_on))
                .setContentText(getString(R.string.location_service_tracked))
                .setSmallIcon(R.drawable.current_location)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(LOCATION_REQUEST_FOREGROUND_CODE, notification);

        return START_STICKY;
    }

    private void handleEvent(boolean active) {
        if (active && state) {
            locationTracker.startUpdate();
        } else {
            locationTracker.stopUpdate();
        }

        if (!state) {
            stopForeground(true);
            stopSelf();
        }
    }

    private void trackerActivated() {
        Query query = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpHelperClass getActive = snapshot.getValue(SignUpHelperClass.class);
                    activated = getActive.isActivated();
                    handleEvent(activated);

                } else {
                    Toast.makeText(PhoneLocationService.this, TAG, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PhoneLocationService.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void batteryDrained(boolean off) {
        state = off;
        DatabaseReference update = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        Map<String, Object> unTraceable = new HashMap<>();
        unTraceable.put(TRACEABLE, false);
        unTraceable.put(ACTIVATED, false);
        update.updateChildren(unTraceable);
        handleEvent(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //************************************************************************************************************************************************************

    public static class PhoneLocationTracker {
        private FusedLocationProviderClient fusedLocationProviderClient;
        private LocationRequest locationRequest;
        private Context context;
        private LocationCallback locationCallback;
        private DatabaseReference reference;
        private BatteryManager batteryManager;
        private int batteryLevel;
        private long updatedAt;
        private Map<String, Object> updateDevice;
        private BatteryDrainHandler listener;

        public final String BATTERY_PERCENT = "batteryPercent";
        public final String LATITUDE = "latitude";
        public final String LONGITUDE = "longitude";
        public final String UPDATE_AT = "updatedAt";


        public PhoneLocationTracker(Context context, String buildId, BatteryDrainHandler listener) {
            this.listener = listener;
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
                    if (batteryLevel < 30) {
                        drainedBattery();
                    } else {
                        updateDevice = new HashMap<>();
                        updatedAt = System.currentTimeMillis();
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
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        }

        private void stopUpdate() {
            Log.i(TAG, "stopUpdate: ");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }

        private void drainedBattery() {
            updateDevice = new HashMap<>();
            updateDevice.put(AVAILABLE, false);
            reference.updateChildren(updateDevice);
            listener.batteryDrained(false);
        }
    }
}

interface BatteryDrainHandler {
    void batteryDrained(boolean off);
}
