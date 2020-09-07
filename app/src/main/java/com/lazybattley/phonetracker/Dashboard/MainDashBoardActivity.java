package com.lazybattley.phonetracker.Dashboard;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RegisterPhoneDashboardActivity;
import com.lazybattley.phonetracker.OptionsScreen;
import com.lazybattley.phonetracker.R;

public class MainDashBoardActivity extends AppCompatActivity {

    private MaterialButton mainDashBoard_register_unregister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);
        mainDashBoard_register_unregister = findViewById(R.id.mainDashBoard_register_unregister);
    }

    public void mainDashboard_options(View view) {
        String clicked = view.getTag().toString();

        switch (clicked) {
            case "register":
                registerPhone();
                break;

            case "request":

                break;

            case "map":

                break;

            case "settings":

                break;
        }
    }


    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, OptionsScreen.class));
        finish();
    }



    private void registerPhone() {
        Intent intent = new Intent(this, RegisterPhoneDashboardActivity.class);
        Pair pair = new Pair(mainDashBoard_register_unregister, "transition_register_phone");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, pair);
        startActivity(intent, options.toBundle());
        startActivity(intent);
    }

}