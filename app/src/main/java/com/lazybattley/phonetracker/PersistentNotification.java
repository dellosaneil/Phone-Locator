package com.lazybattley.phonetracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class PersistentNotification extends Application {

    public static final String CHANNEL_ID = "Notification";

    @Override
    public void onCreate() {
        super.onCreate();
        notificationChannel();
    }

    private void notificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.persistent_notification_some_string),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }



}


