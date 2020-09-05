package com.lazybattley.phonetracker.Dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RegisterPhoneDashboardActivity;
import com.lazybattley.phonetracker.OptionsScreen;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.SplashScreen;

import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;
import static com.lazybattley.phonetracker.GlobalVariables.USER_PHONES;
import static com.lazybattley.phonetracker.SplashScreen.BUILD_ID;

public class MainDashBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);
        checkPhoneRegistration();
    }


    public void mainDashboard_options(View view) {
        String clicked = view.getTag().toString();

        switch (clicked) {
            case "register":
                registerPhone();
                break;

            case "request":

                break;

            case "map":

                break;

            case "settings":

                break;
        }
    }

    private void checkPhoneRegistration(){
        new Thread(() -> {
            FirebaseAuth auth;
            DatabaseReference db;
            auth = FirebaseAuth.getInstance();
            String userID = auth.getCurrentUser().getUid();
            db = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(userID);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        SplashScreen.ALREADY_REGISTERED = snapshot.child(USER_PHONES).child(BUILD_ID).getValue() != null;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }).start();
    }


    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, OptionsScreen.class));
        finish();
    }



    private void registerPhone() {
        Intent intent = new Intent(this, RegisterPhoneDashboardActivity.class);
//        Pair pair = new Pair(mainDashBoard_register_unregister, "transition_register_phone");
//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pair);
//        startActivity(intent, options.toBundle());
        startActivity(intent);
    }

}