package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.DialogClasses.RegisterPhoneDialog;
import com.lazybattley.phonetracker.HelperClasses.PhoneTrackHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.RegisteredPhoneAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.GlobalVariables.ACTIVE;
import static com.lazybattley.phonetracker.GlobalVariables.IS_REGISTERED;
import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.GlobalVariables.USERS;
import static com.lazybattley.phonetracker.GlobalVariables.USER_DETAIL;

public class RegisterPhoneDashboardActivity extends AppCompatActivity implements RegisterPhoneDialog.PhoneDialogListener {

    public static final String MAIN_PHONE = "mainPhone";
    private MaterialButton registerPhone_registerOrUnregisterButton;
    private DatabaseReference isActive;
    private boolean state;
    private TextView registerPhone_isRegistered;
    private RecyclerView registerPhone_phonesRegistered;
    private List<LatLng> coordinates;
    private List<String> deviceName;
    private List<Integer> batteryLevel;
    private ProgressBar registerPhone_progressBar;
    public static final String PHONE_REGISTRATION = "isRegistered";
    private SharedPreferences preferences;
    private boolean isRegistered;
    private String buildId;
    public static final String BUILD_ID = "buildId";
    public static final String DEVICE_NAME = "deviceName";
    public static final String EMAIL = "email";


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_dashboard);
        buildId = Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        isRegistered();
        requestPermission();
        registerPhone_progressBar = findViewById(R.id.registerPhone_progressBar);
        registerPhone_phonesRegistered = findViewById(R.id.registerPhone_phonesRegistered);
        registerPhone_registerOrUnregisterButton = findViewById(R.id.registerPhone_registerOrUnregisterButton);
        registerPhone_isRegistered = findViewById(R.id.registerPhone_isRegistered);
        coordinates = new ArrayList<>();
        batteryLevel = new ArrayList<>();
        deviceName = new ArrayList<>();
        isActive = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(REGISTERED_DEVICES).child(buildId);    //checks whether the user is tracking the device
        cardViewRegistration();         //checks whether the device is registered.
        initRecyclerView();             //initialize the recyclerview
        getCurrentStatus();             //checks if the phone is actively tracked.
    }

    private void cardViewRegistration() {
        if (isRegistered) {
            String deviceName = PreferenceManager.getDefaultSharedPreferences(this).getString(DEVICE_NAME, null);
            if(deviceName == null){
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

    private void isRegistered() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isRegistered = preferences.getBoolean(PHONE_REGISTRATION, false);
    }

    private void registerMainPhone() {
        DatabaseReference query = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAIL);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SignUpHelperClass userDetails = snapshot.getValue(SignUpHelperClass.class);
                if (userDetails.getMainPhone().equals("No Phone")) {
                    Toast.makeText(RegisterPhoneDashboardActivity.this, "This phone is the 'Main Phone'.", Toast.LENGTH_SHORT).show();
                }
                Map<String, Object> updateMainPhone = new HashMap<>();
                updateMainPhone.put(MAIN_PHONE, buildId);
                query.updateChildren(updateMainPhone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getCurrentStatus() {
        if (isRegistered) {
            isActive.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        PhoneTrackHelperClass curLocation = snapshot.getValue(PhoneTrackHelperClass.class);
                        state = curLocation.isActive();
                    }
                    startLocationTrackingService();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void startLocationTrackingService() {
        Intent serviceIntent = new Intent(this, PhoneLocationService.class);
        serviceIntent.putExtra(IS_REGISTERED, state);
        serviceIntent.putExtra(BUILD_ID, buildId);
        if (!state) {
            //Phone is currently not tracked
            startService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_track_phone));
            DatabaseReference update = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(REGISTERED_DEVICES).child(buildId);
            Map<String, Object> deviceStatusUpdate = new HashMap<>();
            deviceStatusUpdate.put(ACTIVE, false);
            update.updateChildren(deviceStatusUpdate);
        } else {
            //Phone is currently tracked
            startService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_untrack_phone));
        }
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
        DatabaseReference phoneDetails = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(REGISTERED_DEVICES);
        phoneDetails.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot details : snapshot.getChildren()) {
                        PhoneTrackHelperClass phoneDetails = details.getValue(PhoneTrackHelperClass.class);
                        LatLng temp = new LatLng(phoneDetails.getLatitude(), phoneDetails.getLongitude());
                        int battery = phoneDetails.getBatteryPercent();
                        deviceName.add(phoneDetails.getDeviceName());
                        coordinates.add(temp);
                        batteryLevel.add(battery);
                    }
                } else {
                    Toast.makeText(RegisterPhoneDashboardActivity.this, "No device registered", Toast.LENGTH_SHORT).show();
                }
                RegisteredPhoneAdapter adapter = new RegisteredPhoneAdapter(deviceName,
                        coordinates, batteryLevel, registerPhone_phonesRegistered, registerPhone_progressBar);
                registerPhone_phonesRegistered.setAdapter(adapter);
                registerPhone_phonesRegistered.setLayoutManager(new LinearLayoutManager(RegisterPhoneDashboardActivity.this));
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(registerPhone_phonesRegistered.getContext(),
                        new LinearLayoutManager(RegisterPhoneDashboardActivity.this).getOrientation());
                registerPhone_phonesRegistered.addItemDecoration(dividerItemDecoration);
                registerPhone_progressBar.setVisibility(View.INVISIBLE);
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Cannot use application without permission.", Toast.LENGTH_LONG).show();
            onBackPressed();
            finish();
        }
    }

    @Override
    public void phoneName(String phone) {
        isRegistered = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(REGISTERED_DEVICES).child(buildId);
        Map<String, Object> initializePhone = new HashMap<>();
        initializePhone.put(EMAIL, ENCODED_EMAIL);
        initializePhone.put(DEVICE_NAME, phone);
        ref.updateChildren(initializePhone);

        registerMainPhone();
        SharedPreferences.Editor editor = preferences.edit();
        registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_track_phone));
        editor.putBoolean(PHONE_REGISTRATION, true);
        editor.putString(DEVICE_NAME, phone);
        editor.apply();
        cardViewRegistration();
    }
}