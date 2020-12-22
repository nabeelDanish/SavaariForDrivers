package com.example.savaari_driver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.savaari_driver.auth.login.LoginActivity;
import com.example.savaari_driver.ride.RideActivity;


public class MainActivity extends Util {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ThemeVar.setData(preferences.getInt(getString(R.string.preference_theme_var), ThemeVar.getData()));

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Expand logo animation
//        ImageView logo = findViewById(R.drawable.ic_savaari_logo);
//        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.zoom);
//        logo.startAnimation(animation);

        SharedPreferences sh
                = getSharedPreferences("AuthSharedPref",
                MODE_PRIVATE);

        final int USER_ID = sh.getInt("USER_ID", -1);
        if (USER_ID == -1) {
            launchLoginActivity();
        }
        else {
            ((SavaariApplication) getApplication()).getRepository().persistConnection(object -> {
                if (object == null || !((Boolean) object)) {
                    Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
                }

                launchRideActivity(USER_ID);
            }, USER_ID);
        }
    }

    public void launchLoginActivity() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    public void launchRideActivity(int userID) {
        Intent i = new Intent(MainActivity.this, RideActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra("USER_ID", userID);
        startActivity(i);
        finish();
    }
}

//