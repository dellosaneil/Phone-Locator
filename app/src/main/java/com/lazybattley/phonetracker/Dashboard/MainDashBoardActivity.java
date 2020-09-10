package com.lazybattley.phonetracker.Dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RegisterPhoneDashboardActivity;
import com.lazybattley.phonetracker.Dashboard.RequestLocation.RequestLocationPermission;
import com.lazybattley.phonetracker.OptionsScreen;
import com.lazybattley.phonetracker.R;

public class MainDashBoardActivity extends AppCompatActivity {

    private DatabaseReference rootNode;
    private static final String TAG = "MainDashBoardActivity";
    public static String ENCODED_EMAIL;
    private FloatingActionButton mainDashboard_notificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);
        ENCODED_EMAIL = encodeEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        rootNode = FirebaseDatabase.getInstance().getReference("users");
        rootNode.child(ENCODED_EMAIL).child("people_requesting_location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "onDataChange: " + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mainDashboard_notificationButton = findViewById(R.id.mainDashboard_notificationButton);


        mainDashboard_notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.i(TAG, "onClick: " + );
            }
        });
        
    }

    private String encodeEmail(String email){
        return email.replace(".", ",");
    }

    public void mainDashboard_options(View view) {
        String clicked = view.getTag().toString();

        switch (clicked) {
            case "register":
                registerPhone();
                break;

            case "request":
                requestFriendLocation();
                break;

            case "map":

                break;

            case "settings":

                break;
        }
    }

    private void requestFriendLocation() {
        Intent intent = new Intent(this, RequestLocationPermission.class);
        startActivity(intent);

    }


    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, OptionsScreen.class));
        finish();
    }

    private void registerPhone() {
        Intent intent = new Intent(this, RegisterPhoneDashboardActivity.class);
        startActivity(intent);
    }

}