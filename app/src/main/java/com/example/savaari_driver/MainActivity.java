package com.example.savaari_driver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import com.example.savaari_driver.auth.login.LoginActivity;
import com.example.savaari_driver.ride.RideActivity;

// TODO Create a Login Check from SharedPreferences

public class MainActivity extends Util
{

    // Main onCreate Function
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Setting Themes
        switch (ThemeVar.getData())
        {
            case(0):
                setTheme(R.style.BlackTheme);
                break;
            case(1):
                setTheme(R.style.RedTheme);
                break;
            default:
                setTheme(R.style.BlueTheme);
                break;
        }

        // Setting Layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Getting Stored USER_ID
        SharedPreferences sh
                = getSharedPreferences("AuthSharedPref",
                MODE_PRIVATE);

        final int USER_ID = sh.getInt("USER_ID", -1);
        if (USER_ID == -1) {
            new Handler().postDelayed(() -> {

                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }, 1200);
        } else {


            // Delayed Handler for Starting Ride Activity
            new Handler().postDelayed(() -> {

                Intent i = new Intent(MainActivity.this, RideActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra("USER_ID", USER_ID);
                startActivity(i);
                finish();
            }, 1200);
        }

        //TODO: Firebase theme logic implementation in MainActivity
    }// End of: onCreate()
}