package com.normurodov_nazar.savol_javob;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class ForTest extends AppCompatActivity{
    int a = 0;
    ProgressBar bar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_test);
        bar = findViewById(R.id.ppp);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            if(a<100) a+=10; else a=0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    bar.setProgress(a,true);
                }else bar.setProgress(a);
            return true;
        }else if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){

            return true;
        }else if(keyCode==KeyEvent.KEYCODE_BACK) {
            return true;
        } return super.onKeyDown(keyCode, event);
    }
}