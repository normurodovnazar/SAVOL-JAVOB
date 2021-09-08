package com.normurodov_nazar.savol_javob;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Keys;

public class Home extends AppCompatActivity {
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initVars();
    }

    private void initVars() {
        preferences = getPreferences(MODE_PRIVATE);
        if(!preferences.getBoolean(Keys.logged,false)){
            preferences.edit().putBoolean(Keys.logged,true).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}