package com.lazybattley.phonetracker.LogInSignUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.GlobalVariables.USERS;
import static com.lazybattley.phonetracker.GlobalVariables.USER_DETAILS;

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
        reference = rootNode.getReference(USERS);
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
                                            Toast.makeText(SignUpActivityOne.this, "Account Successfully Created, Verify Email", Toast.LENGTH_LONG).show();
                                            addNewUser(user.getEmail(), encodeEmail(user.getEmail()), fullName);
                                            editable();
                                            signUp_redirect();
                                            finish();
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
            signUp_progressBar.setVisibility(View.INVISIBLE);
            editable();
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
            signUp_progressBar.setVisibility(View.INVISIBLE);
            editable();
            return null;
        } else if (userInput.length() < 6) {
            signUp_password.setError(getResources().getText(R.string.password_length));
            signUp_progressBar.setVisibility(View.INVISIBLE);
            editable();
            return null;
        } else {
            signUp_password.setErrorEnabled(false);
            signUp_password.setError(null);
            return userInput;
        }
    }


    //Adds new user in the database
    private void addNewUser(String email, String childReference, String fullName) {
        reference.child(childReference).child(USER_DETAILS).setValue(new SignUpHelperClass(childReference, email, fullName, "No Phone"));
    }


    private String encodeEmail(String email) {
        return email.replace(".", ",");
    }


}