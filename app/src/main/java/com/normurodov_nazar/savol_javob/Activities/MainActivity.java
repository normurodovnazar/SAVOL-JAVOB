package com.normurodov_nazar.savol_javob.Activities;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.print;
import static com.normurodov_nazar.savol_javob.MFunctions.Keys.lastChat;
import static com.normurodov_nazar.savol_javob.MFunctions.Keys.newQuestion;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Intent i;
    long id;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Hey.applyTheme(this);
        super.onCreate(savedInstanceState);
        print("onCreate","a");
        setContentView(R.layout.activity_main);
        preferences = Hey.getPreferences(this);
        id = Hey.getId(preferences);
        boolean b = Hey.isLoggedIn(preferences);
        My.folder = getExternalFilesDir("images").toString() + File.separatorChar;
        i = new Intent(this, b ? Home.class : AuthUser.class);
        if (b && id == -1)
            Hey.showAlertDialog(this, getString(R.string.error_unknown) + getString(R.string.reinstall_app)).setOnDismissListener(d -> finish());
        else {
           checkHasPermission();
        }
    }

    private void checkHasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
            else start();
        } else Hey.showAlertDialog(this,getString(R.string.storagePermission)).setOnDismissListener(x-> start());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED) start(); else {
                Toast.makeText(getApplicationContext(), getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void start() {
        My.id = id;
        String type = getIntent().getStringExtra(Keys.type) == null ? "" : getIntent().getStringExtra(Keys.type),action = getIntent().getAction();
        if (action.equals(lastChat)){
            i.putExtra(Keys.type, Keys.privateChat);
            i.putExtra(Keys.id, preferences.getString(lastChat,null));
            i.setAction(lastChat);
        }
        if (action.equals(newQuestion)){
            i.setAction(newQuestion);
        } else {
            i.putExtra(Keys.type, type);
            switch (type) {
                case Keys.privateChat:
                    i.putExtra(Keys.id, getIntent().getStringExtra(Keys.id));
                    break;
                case Keys.publicQuestions:
                case Keys.needQuestions:
                    print("id and theme",getIntent().getStringExtra(Keys.id)+" "+getIntent().getStringExtra(Keys.theme));
                    i.putExtra(Keys.id,getIntent().getStringExtra(Keys.id))
                            .putExtra(Keys.theme,getIntent().getStringExtra(Keys.theme));
                    break;
            }
        }
        if (!My.applied) {
            My.applied = true;
            startActivity(i);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        print("onDestroy","a");
    }
}