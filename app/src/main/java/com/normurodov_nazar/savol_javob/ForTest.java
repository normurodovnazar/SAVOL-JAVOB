package com.normurodov_nazar.savol_javob;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.My;

import java.lang.reflect.Method;

public class ForTest extends AppCompatActivity{
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_test);
        b = findViewById(R.id.aaaa);
    }

    @Override
    protected void onStart() {
        super.onStart();
        CountDownTimer timer = new CountDownTimer(10*1000L,1000L) {

            @Override
            public void onTick(long millisUntilFinished) {
                if(My.timedOut){
                    showDialogAndExit();
                }else Log.e("AAA", String.valueOf(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                showDialogAndExit();
            }
        };
        timer.start();
    }

    private void showDialogAndExit() {
        Hey.showAlertDialog(this,getString(R.string.timeout)).setOnDismissListener(dialog -> finish());
    }
}