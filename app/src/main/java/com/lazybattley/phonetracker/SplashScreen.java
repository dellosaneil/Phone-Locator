package com.lazybattley.phonetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashScreen extends AppCompatActivity implements FinishedSplashScreen {

    private FirebaseAuth auth;
    private ExecutorService executorService;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        ImageView splashScreen_logo = findViewById(R.id.splashScreen_logo);
        TextView splashScreen_text = findViewById(R.id.splashScreen_text);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_animation);
        splashScreen_logo.setAnimation(animation);
        splashScreen_text.setAnimation(animation);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        handler = HandlerCompat.createAsync(Looper.getMainLooper());
        auth = FirebaseAuth.getInstance();
//
//        SharedPreferences temp = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = temp.edit();
//        editor.putBoolean("isRegistered", false);
//        editor.apply();

        loggedIn();

    }

    private void loggedIn() {
        boolean isLoggedIn;
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            Toast.makeText(SplashScreen.this, getText(R.string.splash_screen_logging_in) + " " + auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            isLoggedIn = true;
        } else {
            isLoggedIn = false;
        }
        SplashScreenHandler splashScreen = new SplashScreenHandler(executorService, handler, isLoggedIn, this, this);
        splashScreen.executeThread();
        executorService.shutdown();
    }

    @Override
    public void removeSplashScreen() {
        this.finish();
    }

    private static class SplashScreenHandler implements Runnable {
        private Executor executor;
        private Handler handler;
        private boolean isLoggedIn;
        private Context context;
        private FinishedSplashScreen callback;

        public SplashScreenHandler(Executor executor, Handler handler, boolean isLoggedIn, Context context, FinishedSplashScreen callback) {
            this.callback = callback;
            this.executor = executor;
            this.handler = handler;
            this.isLoggedIn = isLoggedIn;
            this.context = context;
        }

        @Override
        public void run() {
            handler.postDelayed(() -> {
                if (isLoggedIn) {
                    context.startActivity(new Intent(context, MainDashBoardActivity.class));
                } else {
                    context.startActivity(new Intent(context, OptionsScreen.class));
                }
                callback.removeSplashScreen();
            }, 2500);
        }

        public void executeThread() {
            executor.execute(this);
        }
    }
}
interface FinishedSplashScreen {
    void removeSplashScreen();
}