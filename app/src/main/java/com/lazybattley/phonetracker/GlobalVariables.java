package com.lazybattley.phonetracker;

import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GlobalVariables {


    public static final String REGISTERED = "isRegistered";
    public static final String USERS_REFERENCE = "users";
    public static final String USER_DETAILS = "user_detail";
    public static final String USER_PHONES = "registered_devices";
    public static final String USER_FOLLOWED = "followed_users";
    public static final String BUILD_MODEL = Build.MODEL;
    public static final TimeZone TIME_ZONE = Calendar.getInstance().getTimeZone();


//USED AS CHILD NAME
    public static final String LOG_IN_BUILD_EXTRA = "buildID";

    public static final int LOCATION_REQUEST_CODE = 1;
    public static final int LOCATION_REQUEST_FOREGROUND_CODE = 9;


    public static final String CHANNEL_ID = "Notification";

//    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
//
//    String dateString = formatter.format(new Date(1599262421155L));
//        Log.i(TAG, "onCreate: " + dateString);


}
