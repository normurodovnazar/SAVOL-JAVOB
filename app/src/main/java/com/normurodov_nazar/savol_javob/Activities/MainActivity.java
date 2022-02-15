package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences p = Hey.getPreferences(this);
        long id = Hey.getId(p);
        boolean b = Hey.isLoggedIn(p);
        My.folder = getExternalFilesDir("images").toString() + File.separatorChar;
        Intent i = new Intent(this, b ? Home.class : AuthUser.class);
        if (b && id == -1)
            Hey.showAlertDialog(this, getString(R.string.error_unknown) + getString(R.string.reinstall_app)).setOnDismissListener(d -> finish());
        else {
            My.id = id;
            String type = getIntent().getStringExtra(Keys.type) == null ? "" : getIntent().getStringExtra(Keys.type);
            i.putExtra(Keys.type, type);
            switch (type) {
                case Keys.privateChat:
                    i.putExtra(Keys.id, getIntent().getStringExtra(Keys.id));
                    break;
                case Keys.publicQuestions:
                case Keys.needQuestions:
                    Hey.print("id and theme",getIntent().getStringExtra(Keys.id)+" "+getIntent().getStringExtra(Keys.theme));
                    i.putExtra(Keys.id,getIntent().getStringExtra(Keys.id))
                            .putExtra(Keys.theme,getIntent().getStringExtra(Keys.theme));
                    break;
            }

            startActivity(i);
            finish();
        }
    }
}