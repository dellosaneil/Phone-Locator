package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.lazybattley.phonetracker.R;

import java.util.UUID;

import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED;

public class RegisterPhoneDashboardActivity extends AppCompatActivity {

    private MaterialButton registerPhone_registerOrUnregisterButton;
    private SharedPreferences preferences;
    private PhoneLocationTracker track;
    private boolean state;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_dashboard);
        registerPhone_registerOrUnregisterButton = findViewById(R.id.registerPhone_registerOrUnregisterButton);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        track = new PhoneLocationTracker(this);
        phoneState();
    }

    private void phoneState(){
        if(preferences.getBoolean(REGISTERED, false)){
            //Phone is currently not registered
            track.stopTrack();
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_register));
        }else{
            //Phone is Registered
            track.startTrack();
            registerPhone_registerOrUnregisterButton.setText(getString(R.string.register_or_unregister_unregister));
        }
    }

    public void changeState(View view){
        SharedPreferences.Editor editor = preferences.edit();
        state = preferences.getBoolean(REGISTERED, false);
        state = !state;
        editor.putBoolean(REGISTERED, state);
        editor.apply();
        phoneState();
    }
}