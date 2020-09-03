package com.lazybattley.phonetracker;

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
import com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity;
import com.lazybattley.phonetracker.LogInSignUp.LogInActivity;

public class SplashScreen extends AppCompatActivity {
    private Animation animation;
    private ImageView splashScreen_logo;
    private TextView splashScreen_text;
    private FirebaseAuth auth;

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

        Handler handler = new Handler();
        Runnable run = () -> {
            if(auth.getCurrentUser() != null  && auth.getCurrentUser().isEmailVerified()){
                startActivity(new Intent(this, MainDashBoardActivity.class));
            }else{
                Intent intent = new Intent(SplashScreen.this, OptionsScreen.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(run, 1500);
    }
}