package com.lazybattley.phonetracker.LogInSignUp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.lazybattley.phonetracker.LogInSignUp.SignUp.SignUpActivityOne;
import com.lazybattley.phonetracker.R;

public class LogInActivity extends AppCompatActivity {
    private Button logInUser;
    private TextView welcome_back_TV;
    private TextInputLayout log_in_username, log_in_password;
    private ImageView logIn_logo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        logInUser = findViewById(R.id.logInUser);
        welcome_back_TV = findViewById(R.id.welcome_back_TV);
        log_in_username = findViewById(R.id.log_in_username);
        log_in_password = findViewById(R.id.log_in_password);
        logIn_logo = findViewById(R.id.logIn_logo);
    }

    public void logInUser(View view){

    }

    public void redirectSignUp(View view){
        Intent intent = new Intent(this, SignUpActivityOne.class);
        Pair<View,String>[] pairs = new Pair[3];
        pairs[0] = new Pair<>(logIn_logo, "log_in_transition_logo");
        pairs[1] = new Pair<>(logInUser, "log_in_transition_button");
        pairs[2] = new Pair<>(welcome_back_TV, "log_in_transition_text_view");
        ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
        startActivity(intent, option.toBundle());

    }


}