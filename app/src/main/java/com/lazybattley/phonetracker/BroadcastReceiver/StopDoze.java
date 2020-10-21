package com.lazybattley.phonetracker.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.PhoneLocationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;

public class StopDoze extends BroadcastReceiver {
    private PowerManager.WakeLock screenWakeLock;
    public static final String WAKE_LOCK_CONSTANT = "wakeLockAntiDoze:";
    private static final String TAG = "StopDoze";


    @Override
    public void onReceive(Context context, Intent intent){
        if (screenWakeLock == null) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            screenWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_CONSTANT);
            screenWakeLock.acquire();
            Log.i(TAG, "onReceive: ACQUIRED");
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        Map<String, Object> test = new HashMap<>();
        Random r = new Random();
        int numb = r.nextInt(50);
        test.put("fullName", Integer.toString(numb));
        ref.updateChildren(test);
//        Log.i(TAG, "onReceive: TESTING UPDATE");


        Intent serviceIntent = new Intent(context, PhoneLocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        if (screenWakeLock != null){
            Log.i(TAG, "onReceive: RELEASED");
            screenWakeLock.release();
        }

    }
}
