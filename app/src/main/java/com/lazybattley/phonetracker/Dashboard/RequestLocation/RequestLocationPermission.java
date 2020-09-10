package com.lazybattley.phonetracker.Dashboard.RequestLocation;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_LIST_OF_REQUESTS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_TO_ACCESS_LOCATION;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;

public class RequestLocationPermission extends AppCompatActivity {

    private TextInputLayout requestLocation_friendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_location_permission);
        requestLocation_friendEmail = findViewById(R.id.requestLocation_friendEmail);
    }

    public void locationUpdate(View view){
        requestLocationUpdate();
    }

    private void requestLocationUpdate() {
        Long currentTime = System.currentTimeMillis();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE);
        String currentUser = (FirebaseAuth.getInstance().getCurrentUser().getEmail()).replace('.', ',');
        String requestUserLocation = (requestLocation_friendEmail.getEditText().getText().toString()).replace('.',',');
        requestList(currentUser,requestUserLocation,reference, currentTime);
        reference = reference.child(requestUserLocation).child(REQUEST_PERMISSION_TO_ACCESS_LOCATION);
        reference.child(currentUser).setValue(currentTime);
    }

    private void requestList(String currentUser, String friendName, DatabaseReference reference, Long time){
        reference = reference.child(currentUser).child(REQUEST_PERMISSION_LIST_OF_REQUESTS);
        reference.child(friendName).setValue(time);
    }
}