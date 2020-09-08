package com.lazybattley.phonetracker;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.lazybattley.phonetracker.LogInSignUp.LogInActivity;

public class OptionsScreen extends AppCompatActivity {


    private ImageView user_log_in_image;
    private TextView user_log_in_text_view;
    private CardView logInSignUp;
    private Intent intent;
    private Pair<View, String>[] pairs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options_screen);
        user_log_in_image = findViewById(R.id.user_log_in_image);
        user_log_in_text_view = findViewById(R.id.user_log_in_text_view);
        logInSignUp = findViewById(R.id.logInSignUp);

        intent = new Intent(this, LogInActivity.class);
        pairs = new Pair[3];
        pairs[0] = new Pair<>(user_log_in_text_view, "log_in_transition_text_view");
        pairs[1] = new Pair<>(user_log_in_image, "log_in_transition_logo");
        pairs[2] = new Pair<>(logInSignUp, "log_in_transition_button");


    }


    public void redirect(View view) {
        String tag = view.getTag().toString();
        if (tag.equals("1")) {
            ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, option.toBundle());
//            startActivity(intent);
        } else {
//            startActivity(new Intent(this, PhoneLocator.class));
        }


    }


}