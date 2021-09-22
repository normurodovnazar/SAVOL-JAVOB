package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.Works;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(this, Hey.isLoggedIn(getPreferences(MODE_PRIVATE)) ? Home.class : AuthUser.class),s = new Intent(this, Works.class);
        startActivity(i);finish();
    }
}