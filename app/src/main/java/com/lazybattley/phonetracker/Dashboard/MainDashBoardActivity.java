package com.lazybattley.phonetracker.Dashboard;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.Dashboard.Notifications.NotificationActivity;
import com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RegisterPhoneDashboardActivity;
import com.lazybattley.phonetracker.Dashboard.RequestLocation.RequestLocationPermission;
import com.lazybattley.phonetracker.OptionsScreen;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.GlobalVariables.NOTIFICATIONS;
import static com.lazybattley.phonetracker.GlobalVariables.PENDING_REQUESTS;
import static com.lazybattley.phonetracker.GlobalVariables.USERS;

public class MainDashBoardActivity extends AppCompatActivity {

    private DatabaseReference rootNode;
    public static String ENCODED_EMAIL;
    private CardView mainDashboard_bellCardView;
    private TextView mainDashBoard_notificationCounter;
    private ImageView mainDashBoard_notificationBell;
    private MaterialButton mainDashBoard_requestLocation;
    private MaterialButton mainDashBoard_registerOrUnregister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);
        mainDashBoard_registerOrUnregister = findViewById(R.id.mainDashBoard_registerOrUnregister);
        mainDashBoard_requestLocation = findViewById(R.id.mainDashBoard_requestLocation);
        mainDashBoard_notificationBell = findViewById(R.id.mainDashBoard_notificationBell);
        mainDashboard_bellCardView = findViewById(R.id.mainDashboard_bellCardView);
        mainDashBoard_notificationCounter = findViewById(R.id.mainDashBoard_notificationCounter);
        ENCODED_EMAIL = encodeEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        rootNode = FirebaseDatabase.getInstance().getReference(USERS);
        rootNode.child(ENCODED_EMAIL).child(NOTIFICATIONS).child(PENDING_REQUESTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                updateNotificationCounter(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateNotificationCounter(long count) {
        String number = String.valueOf(count);
        if (count == 0) {
            mainDashboard_bellCardView.setVisibility(View.INVISIBLE);
        } else {
            mainDashboard_bellCardView.setVisibility(View.VISIBLE);
            mainDashBoard_notificationCounter.setText(number);
        }

    }


    private String encodeEmail(String email) {
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

    public void viewAllNotification(View view) {
        Intent intent = new Intent(this, NotificationActivity.class);
        Pair<View, String> pair = new Pair(mainDashBoard_notificationBell, "mainDashboard_toNotification");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pair);
        startActivity(intent, options.toBundle());
    }

    private void registerPhone() {
        Intent intent = new Intent(this, RegisterPhoneDashboardActivity.class);
        Pair<View, String> pair = new Pair(mainDashBoard_registerOrUnregister, "mainDashboard_toRegisterPhone");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pair);
        startActivity(intent, options.toBundle());
    }

    private void requestFriendLocation() {
        Intent intent = new Intent(this, RequestLocationPermission.class);
        Pair<View, String> pair = new Pair(mainDashBoard_requestLocation, "mainDashboard_toRequest");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pair);
        startActivity(intent, options.toBundle());
    }


    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, OptionsScreen.class));
        finish();
    }


}