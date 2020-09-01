package com.lazybattley.phonetracker.LogInSignUp.SignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivityOne extends AppCompatActivity {

    private static final String TAG = "SignUpActivityOne";

    public static final String USERS_REFERENCE = "users";
    public static final String CHILD_USERNAME = "username";

    private ProgressBar signUp_progressBar;
    private TextView signUp_message;
    private TextInputLayout signUp_fullName, signUp_username, signUp_password;
    private MaterialButton signUp_verifyAccount;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_one);
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference(USERS_REFERENCE);
        signUp_progressBar = findViewById(R.id.signUp_progressBar);
        signUp_message = findViewById(R.id.signUp_message);
        signUp_fullName = findViewById(R.id.signUp_fullName);
        signUp_username = findViewById(R.id.signUp_username);
        signUp_password = findViewById(R.id.signUp_password);
        signUp_verifyAccount = findViewById(R.id.signUp_next);
    }

    public void signUp_redirect(View view) {
        if (!validateFullName() | !validatePassword() | !validateUsername()) {

        }else{
            Toast.makeText(this, "CHECK", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean validateFullName() {
        String userInput = signUp_fullName.getEditText().getText().toString().trim();
        if (userInput.length() == 0) {
            signUp_fullName.setError(getResources().getText(R.string.missing_field));
            return false;
        } else if (userInput.length() < 4) {
            signUp_fullName.setError(getResources().getText(R.string.minimum_length_full_name));
            return false;
        } else {
            signUp_fullName.setErrorEnabled(false);
            signUp_fullName.setError(null);
            return true;
        }
    }

    private boolean validateUsername() {
        String userInput = signUp_username.getEditText().getText().toString().trim();
        if (userInput.length() == 0) {
            signUp_username.setError(getResources().getText(R.string.missing_field));
            return false;
        } else if (userInput.length() < 4) {
            signUp_username.setError(getResources().getText(R.string.username_length));
            return false;
        } else if(isAvailable(userInput)){
            signUp_username.setError(getResources().getText(R.string.username_taken));
            return false;
        }
        else {
            signUp_username.setErrorEnabled(false);
            signUp_username.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String userInput = signUp_password.getEditText().getText().toString().trim();
        if (userInput.length() == 0) {
            signUp_password.setError(getResources().getText(R.string.missing_field));
            return false;
        } else if (userInput.length() < 6) {
            signUp_password.setError(getResources().getText(R.string.password_length));
            return false;
        } else {
            signUp_password.setErrorEnabled(false);
            signUp_password.setError(null);
            return true;
        }
    }

    private boolean isAvailable(String username){
        final boolean[] available = new boolean[1];
        Query query = reference.orderByChild(CHILD_USERNAME).equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                available[0] = !snapshot.exists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return available[0];
    }




}