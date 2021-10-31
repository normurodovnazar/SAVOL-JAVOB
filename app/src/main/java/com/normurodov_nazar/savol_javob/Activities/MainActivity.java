package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.Works;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences p = Hey.getPreferences(this);
        long id = Hey.getId(p);
        boolean b = Hey.isLoggedIn(p);
        My.folder = getExternalFilesDir("images").toString()+ File.separatorChar;
        Hey.print("Main","logged:"+b);
        Intent i = new Intent(this, b ? Home.class : AuthUser.class),s = new Intent(this, Works.class);
        if(b && id==-1) Hey.showAlertDialog(this,getString(R.string.error_unknown)+getString(R.string.reinstall_app)).setOnDismissListener(d->finish()); else {
            My.id = id;
            startActivity(i);
            finish();
        }
    }
}