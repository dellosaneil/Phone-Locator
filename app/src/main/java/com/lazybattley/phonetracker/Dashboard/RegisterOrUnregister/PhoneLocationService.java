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

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RegisterPhoneDashboardActivity.BUILD_ID;
import static com.lazybattley.phonetracker.GlobalVariables.BUILD_MODEL;
import static com.lazybattley.phonetracker.GlobalVariables.LOCATION_REQUEST_CODE;
import static com.lazybattley.phonetracker.GlobalVariables.LOCATION_REQUEST_FOREGROUND_CODE;
import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;
import static com.lazybattley.phonetracker.GlobalVariables.USER_PHONES;
import static com.lazybattley.phonetracker.PersistentNotification.CHANNEL_ID;

public class PhoneLocationService extends Service {

    private PhoneLocationTracker locationTracker;
    private volatile static boolean state;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        state = intent.getBooleanExtra(REGISTERED, false);
        String buildId = intent.getStringExtra(BUILD_ID);
        locationTracker = new PhoneLocationTracker(this, buildId);
        Thread thread = new Thread(locationTracker);
        Intent notificationIntent = new Intent(this, RegisterPhoneDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, LOCATION_REQUEST_CODE, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.location_service_turned_on))
                .setContentText(getString(R.string.location_service_tracked))
                .setSmallIcon(R.drawable.current_location)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(LOCATION_REQUEST_FOREGROUND_CODE, notification);
        if (state) {
            thread.start();
        } else {
            state = false;
            stopSelf();
            stopForeground(true);
        }
        return START_NOT_STICKY;
    }


    public static class PhoneLocationTracker implements Runnable {
        private FusedLocationProviderClient fusedLocationProviderClient;
        private LocationRequest locationRequest;
        private Context context;
        private LocationCallback locationCallback;
        private FirebaseUser user;
        private DatabaseReference reference;
        private LatLng loc;
        private BatteryManager batteryManager;
        private int batteryLevel;
        private long updatedAt;


        @Override
        public void run() {
            if (state) {
                startUpdate();
            } else {
                stopUpdate();
            }
        }

        public PhoneLocationTracker(Context context, String buildId) {
            batteryManager = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            this.context = context;
            user = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(user.getUid()).child(USER_PHONES);
            locationRequest = new LocationRequest();
            locationRequest.setInterval(3000);
            locationRequest.setFastestInterval(2500);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (!state) {
                        stopUpdate();
                    }else{
                        updatedAt = System.currentTimeMillis();
                        loc = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                        batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                        reference.child(buildId).setValue(new PhoneTrackHelperClass(loc, true, BUILD_MODEL, updatedAt, batteryLevel));
                    }
                }
            };
        }


        private void startUpdate() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (state) {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        stopUpdate();
                    }
                }
            });
        }


        private void stopUpdate() {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

}
