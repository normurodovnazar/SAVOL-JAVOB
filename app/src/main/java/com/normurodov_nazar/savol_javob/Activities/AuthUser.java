package com.normurodov_nazar.savol_javob.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.util.concurrent.TimeUnit;

import static com.normurodov_nazar.savol_javob.MFunctions.Keys.p;
import static com.normurodov_nazar.savol_javob.MFunctions.Keys.verificationId;

public class AuthUser extends AppCompatActivity implements View.OnClickListener {

    ActivityResultLauncher<Intent> launcher;
    String n;
    TelephonyManager manager;
    TextView text;
    EditText phone;
    Button button;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_user);
        initVars();
    }

    private void initVars() {
        preferences = getPreferences(MODE_PRIVATE);
        text = findViewById(R.id.textView);
        phone = findViewById(R.id.phone);
        button = findViewById(R.id.button);

        manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        requestMyPermission();
        Hey.animateFadeOut(text,300);
        Hey.animateHorizontally(phone,400,500);
        Hey.animateVertically(button,350,800);

        button.setOnClickListener(this);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent res = result.getData();
                    if(res != null){
                        if(res.getBooleanExtra("a",false)){
                            doUser();
                        }else Hey.showUnknownError(this);
                    }else {
                        Hey.showUnknownError(this);
                        setButtonAsDefault();
                    }
                }
        );
    }

    @SuppressLint("HardwareIds")
    private void requestMyPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] a;
                a = new String[]{Manifest.permission.READ_SMS,Manifest.permission.READ_PHONE_STATE};
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                   a = new String[]{Manifest.permission.READ_SMS,Manifest.permission.READ_PHONE_NUMBERS,Manifest.permission.READ_PHONE_STATE};
                }
                requestPermissions(a,1);
                n = manager.getLine1Number();
                n = manager.getLine1Number();
                if(n==null || n.equals("")) {
                    phone.setText(preferences.getString(p, "+"));
                } else {
                    phone.setText(n);
                }
            }
        }else{
            n = manager.getLine1Number();
            if(n==null || n.equals("")) {
                phone.setText(preferences.getString(p, "+"));
            } else {
                phone.setText(n);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().putString(p,phone.getText().toString()).apply();
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks(FirebaseAuth auth) {
        return new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                My.verificationCompleted=true;
                auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult()!=null){
                            FirebaseUser user = task.getResult().getUser();
                            if(user!=null){
                                My.setFirebaseUser(user);
                                doUser();
                            }else Hey.showUnknownError(AuthUser.this);
                        }else{
                            Hey.showUnknownError(AuthUser.this);
                        }
                    }else{
                        String e="";
                        if(task.getException()!=null) {
                            if(task.getException().getMessage()!=null) e = task.getException().getMessage();
                            else{
                                Hey.showUnknownError(AuthUser.this);
                            }
                        } else {
                            Hey.showUnknownError(AuthUser.this);
                        }
                        if(!e.equals("")) {
                            Hey.showAlertDialog(AuthUser.this, getString(R.string.error_unknown) + e);
                        } else Hey.showUnknownError(AuthUser.this);
                    }
                });
                setButtonAsDefault();
                Log.e("My","Verification completed");
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e("My","Verification failed:"+e.getMessage());
                failed(e.getMessage());
                setButtonAsDefault();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Log.e("My","Auto retrieval time out");
                My.timedOut = true;
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                codeSend(s);
                Log.e("My","Code sent");
            }
        };
    }

    private void doUser() {
        Hey.amIOnline().addOnCompleteListener(task -> {
            try{
                if(task.getResult()==null || !task.isSuccessful()) throw new NullPointerException();
                if(!task.getResult().getMetadata().isFromCache()){
                    Hey.newUserOrNot(My.uId).addOnCompleteListener(task1 -> {
                        if(!task1.isSuccessful() || task1.getResult()==null) throw new NullPointerException();
                        boolean bo = task1.getResult().exists();
                            Intent i;
                            if(!bo){
                                i = new Intent(AuthUser.this, NewUser.class);
                            }else {
                                i = new Intent(AuthUser.this, Home.class);
                            }
                            startActivity(i);
                            finish();
                    }).addOnFailureListener(this::onFailure);
                }else {
                    setButtonAsDefault();
                    Hey.showAlertDialog(this,getString(R.string.error_connection));
                }
            }catch (NullPointerException e){
                Hey.showUnknownError(this);
                setButtonAsDefault();
            }
        }).addOnFailureListener(this::onFailure);
    }

    private void onFailure(Exception e){
        Hey.showAlertDialog(this,getString(R.string.error_unknown)+e.getMessage());
        setButtonAsDefault();
    }

    private void failed(@Nullable String m) {
        if(m!=null)
        switch (m){
            case Keys.errorInternetWhenNumber:
                Hey.showAlertDialog(this,getString(R.string.error_connection));
                break;
            case Keys.errorNumberInvalid:
                Hey.showAlertDialog(this,getString(R.string.error_number)).setOnDismissListener(dialog -> requestMyPermission());
                break;
            default:
                Hey.showAlertDialog(this,getString(R.string.error_unknown)+m);
                break;
        }
        else  Hey.showAlertDialog(this,getString(R.string.error_unknown).replaceAll(":",""));
    }

    private void codeSend(String id) {
        Intent i = new Intent(this, SmsCode.class);
        My.number=phone.getText().toString();
        i.putExtra(verificationId,id);
        launcher.launch(i);
    }

    @Override
    public void onClick(View view) {
       //if(!My.loading) onButtonClicked();

    }

    private void onButtonClicked() {
        String p = phone.getText().toString();
        if(p.contains("+")){
            setButtonAsLoading();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(p)
                    .setActivity(this)
                    .setTimeout(120L, TimeUnit.SECONDS)
                    .setCallbacks(callBacks(auth))
                    .build();
            My.auth = auth;
            PhoneAuthProvider.verifyPhoneNumber(options);
        }else {
            Toast.makeText(this, getString(R.string.error_insert_number), Toast.LENGTH_SHORT).show();
            requestMyPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
           if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
               requestMyPermission();
           else Toast.makeText(this, getString(R.string.permissionDenied), Toast.LENGTH_SHORT).show();
        }
    }

    private void setButtonAsDefault(){
        phone.setEnabled(true);
        Hey.setButtonAsDefault(this,button,getString(R.string.verify));
    }

    private void setButtonAsLoading(){
        phone.setEnabled(false);
        Hey.setButtonAsLoading(this,button);
    }
}