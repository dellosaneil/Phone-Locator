package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.GlobalVariables.LOG_IN_BUILD_EXTRA;
import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;
import static com.lazybattley.phonetracker.GlobalVariables.USER_PHONES;

public class RegisterPhoneDashboardActivity extends AppCompatActivity {

    private MaterialButton registerPhone_registerOrUnregisterButton;
    private DatabaseReference isActive;
    private FirebaseUser user;
    private boolean state;
    public String phoneUniqueID;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_dashboard);
        registerPhone_registerOrUnregisterButton = findViewById(R.id.registerPhone_registerOrUnregisterButton);
        user = FirebaseAuth.getInstance().getCurrentUser();


        phoneUniqueID = Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);


        isActive = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(user.getUid()).child(USER_PHONES).child(phoneUniqueID);
        registerPhone_registerOrUnregisterButton.setClickable(false);
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
                phoneState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void phoneState() {
        Intent serviceIntent = new Intent(this, PhoneLocationService.class);
        serviceIntent.putExtra(REGISTERED, state);
        serviceIntent.putExtra(LOG_IN_BUILD_EXTRA, phoneUniqueID);
        if (!state) {
            //Phone is currently not registered
            startService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_register));
        } else {
            //Phone is Registered
            startService(serviceIntent);
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_unregister));
        }
    }

    public void changeState(View view) {
        state = !state;
        phoneState();
    }
}