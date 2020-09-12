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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity;
import com.lazybattley.phonetracker.RecyclerViewAdapters.RegisteredPhoneAdapter;
import com.lazybattley.phonetracker.HelperClasses.PhoneTrackHelperClass;
import com.lazybattley.phonetracker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.GlobalVariables.IS_ACTIVE;
import static com.lazybattley.phonetracker.GlobalVariables.STATE;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;
import static com.lazybattley.phonetracker.GlobalVariables.USER_PHONES;

public class RegisterPhoneDashboardActivity extends AppCompatActivity {

    private MaterialButton registerPhone_registerOrUnregisterButton;
    private DatabaseReference isActive;
    private FirebaseUser user;
    private boolean state;
    private TextView registerPhone_isRegistered;
    private RecyclerView registerPhone_phonesRegistered;
    private List<String> phoneModel;
    private List<LatLng> coordinates;
    private DatabaseReference phoneDetails;
    private List<Integer> batteryLevel;
    private ProgressBar registerPhone_progressBar;
    private CardView registerPhone_cardView;
    public static final String PHONE_REGISTRATION = "isRegistered";
    private SharedPreferences preferences;
    private boolean isRegistered;
    private String buildId;
    public static final String BUILD_ID = "buildId";

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_dashboard);
        buildId = Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        isRegistered();
        requestPermission();
        registerPhone_cardView = findViewById(R.id.registerPhone_cardView);
        registerPhone_progressBar = findViewById(R.id.registerPhone_progressBar);
        registerPhone_phonesRegistered = findViewById(R.id.registerPhone_phonesRegistered);
        registerPhone_registerOrUnregisterButton = findViewById(R.id.registerPhone_registerOrUnregisterButton);
        registerPhone_isRegistered = findViewById(R.id.registerPhone_isRegistered);
//        hideViews();
        phoneModel = new ArrayList<>();
        coordinates = new ArrayList<>();
        batteryLevel = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();             // gets the current user
        isActive = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(ENCODED_EMAIL).child(USER_PHONES).child(buildId);    //checks whether the user is tracking the device
        cardViewRegistration();         //checks whether the device is registered.
        initRecyclerView();             //initialize the recyclerview
        getCurrentStatus();             //checks if the phone is actively tracked.
    }

    private void cardViewRegistration() {
        if (isRegistered) {
            registerPhone_isRegistered.setText(getText(R.string.register_or_unregister_currently_registered));
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


    private void getCurrentStatus() {
        if (isRegistered) {
            isActive.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            state = data.getValue(Boolean.class);
                            break;
                        }
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
        serviceIntent.putExtra(STATE, state);
        serviceIntent.putExtra(BUILD_ID, buildId);
        Log.i("", "startLocationTrackingService: " + state);
        if (!state) {
            //Phone is currently not tracked
            startService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_track_phone));
            DatabaseReference update = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(ENCODED_EMAIL).child(USER_PHONES).child(buildId);
            Map<String, Object> hopperUpdates = new HashMap<>();
            hopperUpdates.put(IS_ACTIVE, false);
            update.updateChildren(hopperUpdates);
        } else {
            //Phone is currently tracked
            startService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_untrack_phone));
        }
    }

    public void changeState(View view) {
        if (!isRegistered) {
            isRegistered = true;
            cardViewRegistration();
            SharedPreferences.Editor editor = preferences.edit();
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_track_phone));
            editor.putBoolean(PHONE_REGISTRATION, true);
            editor.apply();
        } else {
            state = !state;
            startLocationTrackingService();
        }
    }

    private void initRecyclerView() {
        phoneDetails = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(ENCODED_EMAIL).child(USER_PHONES);
        phoneDetails.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot details : snapshot.getChildren()) {
                        PhoneTrackHelperClass s = details.getValue(PhoneTrackHelperClass.class);
                        LatLng temp = new LatLng(s.getLatitude(), s.getLongitude());
                        String model = s.getPhoneModel();
                        int battery = s.getBatteryPercent();
                        coordinates.add(temp);
                        phoneModel.add(model);
                        batteryLevel.add(battery);
                    }
                } else {
                    Toast.makeText(RegisterPhoneDashboardActivity.this, "No device registered", Toast.LENGTH_SHORT).show();
                }
                RegisteredPhoneAdapter adapter = new RegisteredPhoneAdapter(RegisterPhoneDashboardActivity.this,
                        phoneModel, coordinates, batteryLevel,registerPhone_phonesRegistered,registerPhone_progressBar);
                registerPhone_phonesRegistered.setAdapter(adapter);
                registerPhone_phonesRegistered.setLayoutManager(new LinearLayoutManager(RegisterPhoneDashboardActivity.this));
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(registerPhone_phonesRegistered.getContext(),
                        new LinearLayoutManager(RegisterPhoneDashboardActivity.this).getOrientation());
                registerPhone_phonesRegistered.addItemDecoration(dividerItemDecoration);

                registerPhone_progressBar.setVisibility(View.INVISIBLE);
//                showViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Cannot use application without permission.", Toast.LENGTH_LONG).show();
            onBackPressed();
            finish();
        }
    }



}