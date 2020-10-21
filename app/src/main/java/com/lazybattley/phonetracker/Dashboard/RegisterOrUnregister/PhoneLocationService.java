package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.BroadcastReceiver.StopDoze;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.Workers.PhoneLocationWorkManager;

import static com.lazybattley.phonetracker.BroadcastReceiver.StopDoze.WAKE_LOCK_CONSTANT;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;

public class PhoneLocationService extends Service {

    private boolean activated;
    private static final String TAG = "PhoneLocationService";
    public static final String CHANNEL_ID = "Notification";
    private Query activatedStatusQuery;
    private ValueEventListener activatedListener;
    private Notification notification;
    private PendingIntent broadcastIntent;
    private AlarmManager alarmManager;
    private Handler handler;
    private Runnable broadcastRunnable;
    private boolean wakeUp = true;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        activatedStatusQuery = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        Intent notificationIntent = new Intent(this, RegisterPhoneDashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.location_service_turned_on))
                .setContentText(getString(R.string.location_service_tracked))
                .setSmallIcon(R.drawable.current_location)
                .setContentIntent(pendingIntent)
                .build();

        createNotificationChannel();
        initializeListenerCallback();
        activatedStatusQuery.addValueEventListener(activatedListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        test();
        return START_STICKY;
    }


    private void createNotificationChannel() {
        Log.i(TAG, "createNotificationChannel: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracker",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1, notification);
        }
    }


    private void handleEvent(boolean active) {
        Log.i(TAG, "handleEvent: ");
        if (active) {
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(PhoneLocationWorkManager.class).build();
            WorkManager workManager = WorkManager.getInstance(this);
            workManager.enqueue(request);
        }
    }

    private void initializeListenerCallback() {
        Log.i(TAG, "initializeListenerCallback: ");
        activatedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpHelperClass getActive = snapshot.getValue(SignUpHelperClass.class);
                    activated = getActive.isActivated();
                    handleEvent(activated);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeUp = false;
        stopForeground(true);
        activatedStatusQuery.removeEventListener(activatedListener);

        Log.i(TAG, "onDestroy: ");
    }

    private void test(){

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StopDoze.class);
        broadcastIntent = PendingIntent.getBroadcast(this,
                123456, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        handler = new Handler();

        broadcastRunnable = new Runnable() {
            @Override
            public void run() {
                if(wakeUp){
                    if (Build.VERSION.SDK_INT >= 23) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, broadcastIntent);
                        Toast.makeText(PhoneLocationService.this, "FIRST", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, broadcastIntent);
                        Toast.makeText(PhoneLocationService.this, "SECOND", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        handler.postDelayed(broadcastRunnable , 250 * 60);

    }

}





















