package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

public class SmsCode extends AppCompatActivity {

    TextView t;
    EditText e;
    Button b;
    String number,id;
    FirebaseAuth auth;
    CountDownTimer timer,time;
    boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_code);
        Intent i = getIntent();
        id = i.getStringExtra(Keys.verificationId);
        number = My.number;
        t = findViewById(R.id.smsSent);e = findViewById(R.id.smsCode);
        b = findViewById(R.id.checkButton);
        t.setText(number);
        auth = My.auth;
        time = new CountDownTimer(120*1000L,1L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(My.verificationCompleted){
                    My.verificationCompleted=false;
                    finish();
                }
            }

            @Override
            public void onFinish() {

            }
        };
        timer = new CountDownTimer(4*30*1000L,1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(My.timedOut){
                    My.timedOut=false;
                    showDialogAndExit(getString(R.string.timeout));
                }else Log.e("AAA", String.valueOf(millisUntilFinished));
            }
            @Override
            public void onFinish() {
            }
        };
        time.start();
        timer.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        b.setOnClickListener(v -> {
            if(!e.getText().toString().equals("")){
                if(!loading) check();
            }else Toast.makeText(this, getString(R.string.write_code), Toast.LENGTH_SHORT).show();
        });
    }

    private void showDialogAndExit(String message) {
        Hey.showAlertDialog(this,message).setOnDismissListener(dialog -> finish());
    }

    private void check() {
        Hey.setButtonAsLoading(this,b);loading = true;
        PhoneAuthCredential authCredential = PhoneAuthProvider.getCredential(id,e.getText().toString().trim());
        auth.signInWithCredential(authCredential).addOnCompleteListener(this,
                task -> {
                    if(task.isSuccessful()){
                        if(task.getResult()!=null){
                            FirebaseUser user = task.getResult().getUser();
                            if(user!=null){
                                setResult(1,new Intent().putExtra("a",true));
                                finish();
                            }else showUnknownErrorAndExit();
                        }else{
                            showUnknownErrorAndExit();
                        }
                    }else{
                        String e="";
                        if(task.getException()!=null) {
                            if(task.getException().getMessage()!=null) e = task.getException().getMessage();
                            else{
                                showUnknownErrorAndExit();
                            }
                        } else {
                            showUnknownErrorAndExit();
                        }
                        if(!e.equals(""))
                            switch (e){
                                case Keys.errorInternetWhenCode:
                                    Hey.showAlertDialog(this,getString(R.string.error_connection));
                                    break;
                                case Keys.errorVerificationCode:
                                    Hey.showAlertDialog(this,getString(R.string.error_code)).setOnDismissListener(dialog -> this.e.setText(""));
                                    break;
                                default:
                                    Hey.showAlertDialog(this,getString(R.string.error_unknown)+e);
                                break;
                        } else showUnknownErrorAndExit();
                    }
                    Hey.setButtonAsDefault(this,b,getString(R.string.check));loading = false;
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        time.cancel();
        timer.cancel();
    }

    private void showUnknownErrorAndExit(){
        showDialogAndExit(getString(R.string.error_unknown)+getString(R.string.unknown));
    }
}