package com.lazybattley.phonetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity;

import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;
import static com.lazybattley.phonetracker.GlobalVariables.USER_PHONES;

public class SplashScreen extends AppCompatActivity {
    private Animation animation;
    private ImageView splashScreen_logo;
    private TextView splashScreen_text;
    private FirebaseAuth auth;
    private DatabaseReference db;
    private String userID;
    public static String BUILD_ID;
    public static boolean ALREADY_REGISTERED;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        BUILD_ID = Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
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
            phoneRegistered(userID);
        } else {
            handlerSplashScreen(false);
        }
    }

    private void phoneRegistered(String id){
        db = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(id);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(SplashScreen.this, getText(R.string.splash_screen_logging_in) + " " + auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    handlerSplashScreen(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void handlerSplashScreen(boolean isLoggedIn) {
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