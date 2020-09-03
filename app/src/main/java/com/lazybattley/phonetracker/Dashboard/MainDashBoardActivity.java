package com.lazybattley.phonetracker.Dashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lazybattley.phonetracker.R;

public class MainDashBoardActivity extends AppCompatActivity {

    private static final String TAG = "MainDashBoardActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);
    }


    public void mainDashboard_options(View view){
        String clicked = view.getTag().toString();

        switch(clicked){
            case "register":
                Log.i(TAG, "mainDashboard_options: register");
                break;

            case "request":
                Log.i(TAG, "mainDashboard_options: request");
                break;

            case "map":
                Log.i(TAG, "mainDashboard_options: map");
                break;

            case "settings":
                Log.i(TAG, "mainDashboard_options: settings");
                break;




        }







    }






}