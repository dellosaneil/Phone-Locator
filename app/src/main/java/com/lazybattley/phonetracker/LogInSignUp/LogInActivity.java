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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity;
import com.lazybattley.phonetracker.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogInActivity extends AppCompatActivity {
    private Button logInUser;
    private TextView welcome_back_TV;
    private TextInputLayout log_in_email, log_in_password;
    private ImageView logIn_logo;
    private FirebaseAuth auth;
    private Intent intent;
    private Pair<View, String>[] pairs;
    private ProgressBar logIn_progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        logInUser = findViewById(R.id.logInUser);
        logIn_logo = findViewById(R.id.logIn_logo);
        welcome_back_TV = findViewById(R.id.welcome_back_TV);

        new Thread(() -> {
            intent = new Intent(this, SignUpActivityOne.class);
            pairs = new Pair[3];
            pairs[0] = new Pair<>(logIn_logo, "log_in_transition_logo");
            pairs[1] = new Pair<>(logInUser, "log_in_transition_button");
            pairs[2] = new Pair<>(welcome_back_TV, "log_in_transition_text_view");
        }).start();

        log_in_email = findViewById(R.id.log_in_email);
        log_in_password = findViewById(R.id.log_in_password);
        logIn_progressBar = findViewById(R.id.logIn_progressBar);
        auth = FirebaseAuth.getInstance();

    }

    public void logInUser(View view) {
        logIn_progressBar.setVisibility(View.VISIBLE);
        clearError();
        String email = validateEmail();
        String password = validatePassword();
        if (email != null && password != null) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            exceptionError(e.getMessage());
                            logIn_progressBar.setVisibility(View.INVISIBLE);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if (auth.getCurrentUser().isEmailVerified()) {
                                clearError();
                                startActivity(new Intent(LogInActivity.this, MainDashBoardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LogInActivity.this, R.string.log_in_verify, Toast.LENGTH_SHORT).show();
                            }
                            logIn_progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    private void exceptionError(String errors) {
        switch (errors) {
            //User is not registered
            case "There is no user record corresponding to this identifier. The user may have been deleted.":
                log_in_email.setError(getText(R.string.log_in_user_not_found));
                break;
            //Incorrect password
            case "The password is invalid or the user does not have a password.":
                log_in_password.setError(getText(R.string.log_in_incorrect_pass));
                break;
            // unexpected exception,
            default:
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearError() {
        log_in_email.setError(null);
        log_in_password.setError(null);
        log_in_email.setErrorEnabled(false);
        log_in_password.setErrorEnabled(false);
    }


    private String validateEmail() {
        String email = log_in_email.getEditText().getText().toString().trim();
        Pattern validateEmail = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = validateEmail.matcher(email);
        if (email.length() == 0) {
            log_in_email.setError(getText(R.string.missing_field));
            logIn_progressBar.setVisibility(View.INVISIBLE);
            return null;
        } else if (!matcher.find()) {
            log_in_email.setError(getText(R.string.log_in_invalid_format));
            logIn_progressBar.setVisibility(View.INVISIBLE);
            return null;
        } else {
            log_in_email.setError(null);
            log_in_email.setErrorEnabled(false);
            return email;
        }
    }

    private String validatePassword() {
        String password = log_in_password.getEditText().getText().toString().trim();
        if (password.length() == 0) {
            log_in_password.setError(getText(R.string.missing_field));
            logIn_progressBar.setVisibility(View.INVISIBLE);
            return null;
        } else {
            log_in_password.setError(null);
            log_in_password.setErrorEnabled(false);
            return password;
        }
    }


    public void redirectSignUp(View view) {
            ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
            startActivity(intent, option.toBundle());
//            startActivity(intent);

    }
}