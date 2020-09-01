package com.lazybattley.phonetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lazybattley.phonetracker.LogInSignUp.LogInActivity;

public class OptionsScreen extends AppCompatActivity {

    private static final String TAG = "OptionsScreen";

    private ImageView user_log_in_image, user_search_phone_image;
    private TextView user_log_in_text_view, user_search_phone_text_view;
    private CardView logInSignUp;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options_screen);
        user_log_in_image = findViewById(R.id.user_log_in_image);
        user_search_phone_image = findViewById(R.id.user_search_phone_image);
        user_log_in_text_view = findViewById(R.id.user_log_in_text_view);
        user_search_phone_text_view = findViewById(R.id.user_search_phone_text_view);
        logInSignUp = findViewById(R.id.logInSignUp);



    }

    public void redirect(View view){
        String tag = view.getTag().toString();
        if(tag.equals("1")){
            Intent intent = new Intent(this, LogInActivity.class);
            Pair<View, String>[] pairs = new Pair[3];
            pairs[0] = new Pair<>(user_log_in_text_view, "log_in_transition_text_view");
            pairs[1] = new Pair<>(user_log_in_image,"log_in_transition_logo");
            pairs[2] = new Pair<>(logInSignUp, "log_in_transition_button");
            ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, option.toBundle());
        }else{
//            startActivity(new Intent(this, PhoneLocator.class));
        }



    }








}