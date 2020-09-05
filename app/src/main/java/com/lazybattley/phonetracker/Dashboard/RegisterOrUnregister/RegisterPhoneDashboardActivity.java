package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RecyclerView.RegisteredPhoneAdapter;
import com.lazybattley.phonetracker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;
import static com.lazybattley.phonetracker.GlobalVariables.USER_PHONES;
import static com.lazybattley.phonetracker.SplashScreen.ALREADY_REGISTERED;
import static com.lazybattley.phonetracker.SplashScreen.BUILD_ID;

public class RegisterPhoneDashboardActivity extends AppCompatActivity {

    private MaterialButton registerPhone_registerOrUnregisterButton;
    private DatabaseReference isActive;
    private FirebaseUser user;
    private boolean state;
    private TextView registerPhone_isRegistered;
    private RecyclerView registerPhone_phonesRegistered;
    private List<Map<String, LatLng>> registeredPhoneDetails;
    private DatabaseReference phoneDetails;
    private static final String TAG = "RegisterPhoneDashboardA";


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_dashboard);
        registerPhone_phonesRegistered = findViewById(R.id.registerPhone_phonesRegistered);
        registerPhone_registerOrUnregisterButton = findViewById(R.id.registerPhone_registerOrUnregisterButton);
        registerPhone_isRegistered = findViewById(R.id.registerPhone_isRegistered);
        registeredPhoneDetails = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        isActive = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(user.getUid()).child(USER_PHONES).child(BUILD_ID);
        registerPhone_registerOrUnregisterButton.setClickable(false);
        if (ALREADY_REGISTERED) {
            registerPhone_isRegistered.setText(getString(R.string.register_or_unregister_currently_registered));
            registerPhone_isRegistered.setTextColor(Color.BLACK);
        } else {
            registerPhone_isRegistered.setText(getString(R.string.register_or_unregister_currently_unregistered));
            registerPhone_isRegistered.setTextColor(Color.RED);
        }
        initRecyclerView();
        getCurrentStatus();
    }

    private void getCurrentStatus() {
        isActive.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        state = data.getValue(Boolean.class);
                        break;
                    }
                }
                registerPhone_registerOrUnregisterButton.setClickable(true);
                startLocationTrackingService();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void startLocationTrackingService() {
        Intent serviceIntent = new Intent(this, PhoneLocationService.class);
        serviceIntent.putExtra(REGISTERED, state);
        if (!state) {
            //Phone is currently not registered
            startService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_track_phone));
        } else {
            //Phone is Registered
            startService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_untrack_phone));
        }
    }
    public void changeState(View view) {
        state = !state;
        startLocationTrackingService();
    }

    private void initRecyclerView(){
        phoneDetails = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(user.getUid()).child(USER_PHONES);
        phoneDetails.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot details: snapshot.getChildren()){
                        Map<String, LatLng> tempMap = new HashMap<>();
                        PhoneTrackHelperClass s = details.getValue(PhoneTrackHelperClass.class);
                        LatLng temp = new LatLng(s.getLatitude(), s.getLongitude());
                        String phoneModel  = s.getPhoneModel();
                        tempMap.put(phoneModel, temp);
                        registeredPhoneDetails.add(tempMap);
                    }
                }else{
                    Toast.makeText(RegisterPhoneDashboardActivity.this, "No device registered", Toast.LENGTH_SHORT).show();
                }
                RegisteredPhoneAdapter adapter = new RegisteredPhoneAdapter(RegisterPhoneDashboardActivity.this, registeredPhoneDetails);
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


}