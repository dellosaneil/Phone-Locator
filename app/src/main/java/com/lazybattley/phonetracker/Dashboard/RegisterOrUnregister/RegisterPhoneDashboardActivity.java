package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.DialogClasses.RegisterPhoneDialog;
import com.lazybattley.phonetracker.HelperClasses.OwnPhoneDetailsHelperClass;
import com.lazybattley.phonetracker.HelperClasses.PhoneTrackHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.RegisteredPhoneAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ACTIVATED;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ACTIVE;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.AVAILABLE;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.IS_REGISTERED;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.TRACEABLE;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;


public class RegisterPhoneDashboardActivity extends AppCompatActivity implements RegisterPhoneDialog.PhoneDialogListener, RegisteredPhoneAdapter.OnFinishedLoading {

    public static final String MAIN_PHONE = "mainPhone";
    private MaterialButton registerPhone_registerOrUnregisterButton;
    private boolean state;
    private TextView registerPhone_isRegistered;
    private RecyclerView registerPhone_phonesRegistered;
    private List<OwnPhoneDetailsHelperClass> phoneDetails;
    private ProgressBar registerPhone_progressBar;
    public static final String PHONE_REGISTRATION = "isRegistered";
    private SharedPreferences preferences;
    private boolean isRegistered;
    public static String BUILD_ID;
    public static final String DEVICE_NAME = "deviceName";
    public static final String EMAIL = "email";
    private static final String TAG = "RegisterPhoneDashboardA";
    private boolean isMainDevice;
    private LocationManager locationManager;
    private Toast toast;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_dashboard);
        BUILD_ID = Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        mainDeviceCheck();
        isRegistered();
        requestPermission();
        registerPhone_progressBar = findViewById(R.id.registerPhone_progressBar);
        registerPhone_phonesRegistered = findViewById(R.id.registerPhone_phonesRegistered);
        registerPhone_registerOrUnregisterButton = findViewById(R.id.registerPhone_registerOrUnregisterButton);
        registerPhone_isRegistered = findViewById(R.id.registerPhone_isRegistered);
        getCurrentStatus();             //checks if the phone is available
        cardViewRegistration();         //checks whether the device is registered.
        initRecyclerView();             //initialize the recyclerview
    }


    private void cardViewRegistration() {
        if (isRegistered) {
            String deviceName = PreferenceManager.getDefaultSharedPreferences(this).getString(DEVICE_NAME, null);
            if (deviceName == null) {
                isRegistered = false;
                cardViewRegistration();
            }
            registerPhone_isRegistered.setText(getApplicationContext().getString(R.string.register_or_unregister_currently_registered, deviceName));
            registerPhone_isRegistered.setTextColor(Color.BLACK);
        } else {
            registerPhone_isRegistered.setText(getText(R.string.register_or_unregister_currently_unregistered));
            registerPhone_isRegistered.setTextColor(Color.RED);
            registerPhone_registerOrUnregisterButton.setText(getText(R.string.register_or_unregister_register_device));
        }
    }

    private void updateActiveStatus() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        Map<String, Object> updateActiveStatus = new HashMap<>();
        updateActiveStatus.put(ACTIVATED, false);
        reference.updateChildren(updateActiveStatus);
    }


    private void mainDeviceCheck() {
        Query mainDeviceID = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        mainDeviceID.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpHelperClass user_details = snapshot.getValue(SignUpHelperClass.class);
                    if (BUILD_ID.equals(user_details.getMainPhone())) {
                        isMainDevice = true;
                    }
                } else {
                    if(toast != null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(RegisterPhoneDashboardActivity.this, TAG, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void isRegistered() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isRegistered = preferences.getBoolean(PHONE_REGISTRATION, false);
    }

    private void registerMainPhone() {
        DatabaseReference query = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SignUpHelperClass userDetails = snapshot.getValue(SignUpHelperClass.class);
                if (userDetails.getMainPhone().equals("No Phone")) {
                    if(toast != null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(RegisterPhoneDashboardActivity.this, "This phone is the 'Main Phone'.", Toast.LENGTH_SHORT);
                    toast.show();
                    Map<String, Object> updateMainPhone = new HashMap<>();
                    updateMainPhone.put(MAIN_PHONE, BUILD_ID);
                    query.updateChildren(updateMainPhone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getCurrentStatus() {
        if (isRegistered) {
            Query availability = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(REGISTERED_DEVICES).child(BUILD_ID);
            availability.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        PhoneTrackHelperClass currentState = snapshot.getValue(PhoneTrackHelperClass.class);
                        state = currentState.isAvailable();
                        if (state) {
                            startLocationTrackingService();
                            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_untrack_phone));
                        } else {
                            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_track_phone));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }


    private void startLocationTrackingService() {
        DatabaseReference update = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(REGISTERED_DEVICES).child(BUILD_ID);
        Map<String, Object> deviceStatusUpdate = new HashMap<>();
        Intent serviceIntent = new Intent(this, PhoneLocationService.class);
        if (!state) {
            //Phone is currently not tracked
            stopService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_track_phone));
            deviceStatusUpdate.put(AVAILABLE, false);
            updateActiveStatus();

        } else {
            if(checkGPSStatus()){
                //Phone is currently tracked
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                deviceStatusUpdate.put(AVAILABLE, true);
                registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_untrack_phone));
            }else{
                state = false;
                if(toast != null){
                    toast.cancel();
                }
                toast = Toast.makeText(this, "Please enable your GPS.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if (isMainDevice) {
            traceableHandler(state);
        }
        update.updateChildren(deviceStatusUpdate);
    }

    private boolean checkGPSStatus() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    private void traceableHandler(boolean traceability) {
        DatabaseReference update = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        Map<String, Object> traceable = new HashMap<>();
        traceable.put(TRACEABLE, traceability);
        update.updateChildren(traceable);
    }


    public void changeState(View view) {
        if (!isRegistered) {
            RegisterPhoneDialog dialog = new RegisterPhoneDialog();
            dialog.show(getSupportFragmentManager(), "Get Device Name");
        } else {
            state = !state;
            startLocationTrackingService();
        }
    }

    private void initRecyclerView() {
        DatabaseReference phoneReference = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(REGISTERED_DEVICES);
        phoneReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                phoneDetails = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot details : snapshot.getChildren()) {
                        PhoneTrackHelperClass singlePhone = details.getValue(PhoneTrackHelperClass.class);
                        LatLng phoneLocation = new LatLng(singlePhone.getLatitude(), singlePhone.getLongitude());
                        int battery = singlePhone.getBatteryPercent();
                        phoneDetails.add(new OwnPhoneDetailsHelperClass(singlePhone.getDeviceName(), phoneLocation, battery));
                    }
                } else {
                    if(toast != null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(RegisterPhoneDashboardActivity.this, "No device registered", Toast.LENGTH_SHORT);
                    toast.show();
                }
                RegisteredPhoneAdapter adapter = new RegisteredPhoneAdapter(phoneDetails, RegisterPhoneDashboardActivity.this);
                registerPhone_phonesRegistered.setAdapter(adapter);
                registerPhone_phonesRegistered.setLayoutManager(new LinearLayoutManager(RegisterPhoneDashboardActivity.this));
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(registerPhone_phonesRegistered.getContext(),
                        new LinearLayoutManager(RegisterPhoneDashboardActivity.this).getOrientation());
                registerPhone_phonesRegistered.addItemDecoration(dividerItemDecoration);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Cannot use application without permission.", Toast.LENGTH_LONG).show();
            onBackPressed();
        }

        if(requestCode == 2 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Cannot use application without permission.", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }

    }

    @Override
    public void phoneName(String phone) {
        isRegistered = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(REGISTERED_DEVICES).child(BUILD_ID);
        Map<String, Object> initializePhone = new HashMap<>();
        initializePhone.put(EMAIL, ENCODED_EMAIL);
        initializePhone.put(DEVICE_NAME, phone);
        initializePhone.put(AVAILABLE, false);
        initializePhone.put(ACTIVE, false);
        ref.updateChildren(initializePhone);
        registerMainPhone();
        SharedPreferences.Editor editor = preferences.edit();
        registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_track_phone));
        editor.putBoolean(PHONE_REGISTRATION, true);
        editor.putString(DEVICE_NAME, phone);
        editor.apply();
        cardViewRegistration();
    }

    public void backButton(View view) {
        onBackPressed();
    }

    @Override
    public void dataFinishedLoading() {
        registerPhone_progressBar.setVisibility(View.INVISIBLE);
        registerPhone_phonesRegistered.setVisibility(View.VISIBLE);
    }
}