package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.makeText;
import static com.lazybattley.phonetracker.GlobalVariables.BUILD_MODEL;
import static com.lazybattley.phonetracker.GlobalVariables.CHANNEL_ID;
import static com.lazybattley.phonetracker.GlobalVariables.LOCATION_REQUEST_CODE;
import static com.lazybattley.phonetracker.GlobalVariables.LOCATION_REQUEST_FOREGROUND_CODE;
import static com.lazybattley.phonetracker.GlobalVariables.LOG_IN_BUILD_EXTRA;
import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED;
import static com.lazybattley.phonetracker.GlobalVariables.TIME_ZONE;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;
import static com.lazybattley.phonetracker.GlobalVariables.USER_PHONES;
import static com.lazybattley.phonetracker.SplashScreen.BUILD_ID;

public class PhoneLocationService extends Service {

    private PhoneLocationTracker locationTracker;
    private volatile static boolean state;


    public PhoneLocationService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        state = intent.getBooleanExtra(REGISTERED, false);
        locationTracker = new PhoneLocationTracker(this);
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

        private static final String TAG = "PhoneLocationTracker";

        private FusedLocationProviderClient fusedLocationProviderClient;
        private LocationRequest locationRequest;
        private Context context;
        private LocationCallback locationCallback;
        private FirebaseUser user;
        private DatabaseReference reference;
        private LatLng loc;
        private Calendar calendar;
        private Date currentTime;
        private Date updatedAt;


        @Override
        public void run() {
            if (state) {
                startUpdate();
            } else {
                stopUpdate();
            }
        }

        public PhoneLocationTracker(Context context) {
            calendar = new GregorianCalendar(TIME_ZONE);
            this.context = context;
            user = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(user.getUid()).child(USER_PHONES);
            locationRequest = new LocationRequest();
            locationRequest.setInterval(3000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    currentTime = new Date();
                    calendar.setTime(currentTime);
                    updatedAt = calendar.getTime();
                    loc = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                    reference.child(BUILD_ID).setValue(new PhoneTrackHelperClass(loc, true, BUILD_MODEL, updatedAt.getTime()));
                    if (!state) {
                        stopUpdate();
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
            reference.child(BUILD_ID).setValue(new PhoneTrackHelperClass(loc, false, BUILD_MODEL, updatedAt.getTime()));
        }
    }
}
