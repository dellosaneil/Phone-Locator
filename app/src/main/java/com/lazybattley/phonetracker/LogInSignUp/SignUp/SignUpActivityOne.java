package com.lazybattley.phonetracker.LogInSignUp.SignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.LogInSignUp.LogInActivity;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;

public class SignUpActivityOne extends AppCompatActivity {
    private ProgressBar signUp_progressBar;
    private TextView signUp_message;
    private TextInputLayout signUp_fullName, signUp_password, signUp_email;
    private MaterialButton signUp_next;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private ImageView imageView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_one);
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference(USERS_REFERENCE);
        imageView = findViewById(R.id.imageView);
        signUp_email = findViewById(R.id.signUp_email);
        auth = FirebaseAuth.getInstance();
        signUp_progressBar = findViewById(R.id.signUp_progressBar);
        signUp_message = findViewById(R.id.signUp_message);
        signUp_fullName = findViewById(R.id.signUp_fullName);
        signUp_password = findViewById(R.id.signUp_password);
        signUp_next = findViewById(R.id.signUp_next);
        verifyAccount();
    }

    private void uneditable() {
        signUp_fullName.setEnabled(false);
        signUp_password.setEnabled(false);
        signUp_email.setEnabled(false);
        signUp_next.setEnabled(false);
    }

    private void editable() {
        signUp_fullName.setEnabled(true);
        signUp_password.setEnabled(true);
        signUp_email.setEnabled(true);
        signUp_next.setEnabled(true);
    }

    private void verifyAccount() {
        signUp_next.setOnClickListener((view) -> {
            createUser();
        });
    }

    public void signUp_redirect() {
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
        finish();
    }

    private String validateFullName() {
        String userInput = signUp_fullName.getEditText().getText().toString().trim();
        if (userInput.length() == 0) {
            signUp_fullName.setError(getResources().getText(R.string.missing_field));
            return null;
        } else if (userInput.length() < 4) {
            signUp_fullName.setError(getResources().getText(R.string.minimum_length_full_name));
            return null;
        } else {
            signUp_fullName.setErrorEnabled(false);
            signUp_fullName.setError(null);
            return userInput;
        }
    }

    private void createUser() {
        signUp_progressBar.setVisibility(View.VISIBLE);
        uneditable();
        String fullName = validateFullName();
        String email = validateEmail();
        String password = validatePassword();
        if (email != null && password != null && fullName != null) {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(SignUpActivityOne.this, "New account created, Check email for verification", Toast.LENGTH_LONG).show();
                                            createTableProfile(user.getEmail(), user.getUid(), fullName);
                                            editable();
                                            signUp_redirect();
                                        } else {
                                            signUp_email.setError(task1.getException().getMessage());
                                        }
                                        signUp_progressBar.setVisibility(View.INVISIBLE);
                                    });
                        } else {
                            signUp_email.setError(task.getException().getMessage());
                            signUp_progressBar.setVisibility(View.INVISIBLE);
                            editable();
                        }
                    });
        }
    }


    private String validateEmail() {
        String userInput = signUp_email.getEditText().getText().toString().trim();
        if (userInput.length() == 0) {
            signUp_email.setError(getResources().getText(R.string.missing_field));
            return null;
        } else {
            signUp_email.setErrorEnabled(false);
            signUp_email.setError(null);
            return userInput;
        }

    }

    private String validatePassword() {
        String userInput = signUp_password.getEditText().getText().toString().trim();
        if (userInput.length() == 0) {
            signUp_password.setError(getResources().getText(R.string.missing_field));
            return null;
        } else if (userInput.length() < 6) {
            signUp_password.setError(getResources().getText(R.string.password_length));
            return null;
        } else {
            signUp_password.setErrorEnabled(false);
            signUp_password.setError(null);
            return userInput;
        }
    }

    private void createTableProfile(String username, String uid, String fullName) {
        reference.child(uid).setValue(new SignUpHelperClass(uid, username, fullName));
    }


}