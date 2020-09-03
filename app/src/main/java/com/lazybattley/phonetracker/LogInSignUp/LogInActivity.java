package com.lazybattley.phonetracker.LogInSignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity;
import com.lazybattley.phonetracker.LogInSignUp.SignUp.SignUpActivityOne;
import com.lazybattley.phonetracker.R;

public class LogInActivity extends AppCompatActivity {
    private Button logInUser;
    private TextView welcome_back_TV;
    private TextInputLayout log_in_email, log_in_password;
    private ImageView logIn_logo;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        logInUser = findViewById(R.id.logInUser);
        welcome_back_TV = findViewById(R.id.welcome_back_TV);
        log_in_email = findViewById(R.id.log_in_email);
        log_in_password = findViewById(R.id.log_in_password);
        logIn_logo = findViewById(R.id.logIn_logo);
        auth = FirebaseAuth.getInstance();
    }

    public void logInUser(View view) {
        String email = log_in_email.getEditText().getText().toString();
        String password = log_in_password.getEditText().getText().toString();
        auth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (auth.getCurrentUser().isEmailVerified()) {
                            startActivity(new Intent(LogInActivity.this, MainDashBoardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LogInActivity.this, R.string.log_in_verify, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void redirectSignUp(View view) {
        Intent intent = new Intent(this, SignUpActivityOne.class);
        Pair<View, String>[] pairs = new Pair[3];
        pairs[0] = new Pair<>(logIn_logo, "log_in_transition_logo");
        pairs[1] = new Pair<>(logInUser, "log_in_transition_button");
        pairs[2] = new Pair<>(welcome_back_TV, "log_in_transition_text_view");
        ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
        startActivity(intent, option.toBundle());

    }


}