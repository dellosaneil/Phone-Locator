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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity;

public class SplashScreen extends AppCompatActivity {
    private FirebaseAuth auth;
    public String BUILD_ID;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        BUILD_ID = Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        ImageView splashScreen_logo = findViewById(R.id.splashScreen_logo);
        TextView splashScreen_text = findViewById(R.id.splashScreen_text);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_animation);
        splashScreen_logo.setAnimation(animation);
        splashScreen_text.setAnimation(animation);
        auth = FirebaseAuth.getInstance();
        loggedIn();
    }


    private void loggedIn() {
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            Toast.makeText(SplashScreen.this, getText(R.string.splash_screen_logging_in) + " " + auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            handlerSplashScreen(true);
        } else {
            handlerSplashScreen(false);
        }
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