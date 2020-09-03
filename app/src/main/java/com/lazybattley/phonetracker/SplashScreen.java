package com.lazybattley.phonetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity;
import com.lazybattley.phonetracker.LogInSignUp.LogInActivity;

import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;

public class SplashScreen extends AppCompatActivity {
    private Animation animation;
    private ImageView splashScreen_logo;
    private TextView splashScreen_text;
    private FirebaseAuth auth;
    private static final String TAG = "SplashScreen";
    private DatabaseReference db;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        splashScreen_logo = findViewById(R.id.splashScreen_logo);
        splashScreen_text = findViewById(R.id.splashScreen_text);
        animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_animation);
        splashScreen_logo.setAnimation(animation);
        splashScreen_text.setAnimation(animation);
        auth = FirebaseAuth.getInstance();
        loggedIn();
    }

    private void loggedIn() {
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            userID = auth.getCurrentUser().getUid();
            Log.i(TAG, "loggedIn: " + userID);
            db = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(userID);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        handlerSplashScreen(true);
                    }else{
                        handlerSplashScreen(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            handlerSplashScreen(false);
        }
    }

    private void handlerSplashScreen(boolean isLoggedIn){
        Handler handler = new Handler();
        Runnable run = () -> {
            if (isLoggedIn) {
                startActivity(new Intent(this, MainDashBoardActivity.class));
            } else {
                Intent intent = new Intent(SplashScreen.this, OptionsScreen.class);
                startActivity(intent);
            }
            finish();
        };
        handler.postDelayed(run, 1500);
    }

}