package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.ServiceHelper;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.GlobalVariables.CHANNEL_ID;

public class PersistentNotification extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        notificationChannel();
    }

    private void notificationChannel() {
        if(Build.VERSION.SDK_INT > 26){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, getString(R.string.persistent_notification_some_string), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
