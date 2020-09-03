package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

import java.util.UUID;

import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED;

public class RegisterPhoneDashboardActivity extends AppCompatActivity {

    private MaterialButton registerPhone_registerOrUnregisterButton;
    private PhoneLocationTracker track;
    private DatabaseReference isActive;
    private FirebaseUser user;
    private boolean state;

    private PhoneLocationTemp temp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_dashboard);
        registerPhone_registerOrUnregisterButton = findViewById(R.id.registerPhone_registerOrUnregisterButton);
        track = new PhoneLocationTracker(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        isActive = FirebaseDatabase.getInstance().getReference(user.getUid()).child("Phone 1");
        temp = new PhoneLocationTemp(this);
        isActive.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        phoneState();
    }

    private void phoneState() {
        if (state) {
            //Phone is currently not registered
//            track.stopTrack();
            temp.stopUpdate();

            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_register));
        } else {
            //Phone is Registered
//            track.startTrack();

            temp.startUpdate();
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_unregister));
        }
    }

    public void changeState(View view) {
        state = !state;
        phoneState();
    }
}