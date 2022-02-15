package com.normurodov_nazar.savol_javob.Activities;

import static com.normurodov_nazar.savol_javob.MFunctions.Keys.p;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.Exists;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.R;

import java.util.concurrent.TimeUnit;

public class AuthUser extends AppCompatActivity implements View.OnClickListener {

    ActivityResultLauncher<Intent> launcher;
    TextView text;
    EditText phone;
    Button button;
    SharedPreferences preferences;
    boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_user);
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent res = result.getData();
                    if(res != null){
                        if(res.getBooleanExtra("a",false)) doUser(); else Hey.showUnknownError(this);
                    }else {
                        Hey.showUnknownError(this);
                        setButtonAsDefault();
                    }
                }
        );
        checkHasPermission();
        initVars();
    }

    private void checkHasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED) initVars(); else {
                Toast.makeText(getApplicationContext(), getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initVars() {
        preferences = getPreferences(MODE_PRIVATE);
        text = findViewById(R.id.textView);
        phone = findViewById(R.id.phone);
        button = findViewById(R.id.button);

        Hey.animateFadeOut(text,300);
        Hey.animateHorizontally(phone,400,500);
        Hey.animateVertically(button,350,800);

        button.setOnClickListener(this);
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
        Hey.amIOnline(new StatusListener() {
            @Override
            public void online() {
                Hey.myNumberExists(new Exists() {
                    @Override
                    public void exists(DocumentSnapshot doc) {
                        My.setDataFromDoc(doc);
                        startActivity(new Intent(AuthUser.this, Home.class));
                        finish();
                    }
                    @Override
                    public void notExists() {
                        generateUniqueId();
                    }
                });
            }

            @Override
            public void offline() {
                Hey.showAlertDialog(AuthUser.this,getString(R.string.error_connection));
            }
        }, errorMessage -> {}, this);
    }

    void generateUniqueId(){
        int id = Hey.generateID();
        Hey.isDocumentExists(this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(id)),
                new Exists() {
                    @Override
                    public void exists(DocumentSnapshot doc) {
                        generateUniqueId();
                    }

                    @Override
                    public void notExists() {
                        My.id = id;
                        startActivity(new Intent(AuthUser.this, NewUser.class));
                        finish();
                    }
                }, errorMessage -> finish());
    }

    private void failed(@Nullable String m) {
        if(m!=null)
        switch (m){
            case Keys.errorInternetWhenNumber:
                Hey.showAlertDialog(this,getString(R.string.error_connection));
                break;
            case Keys.errorNumberInvalid:
                Hey.showAlertDialog(this,getString(R.string.error_number));
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
        i.putExtra(Keys.verificationId,id);
        launcher.launch(i);
    }

    @Override
    public void onClick(View view) {
       if(!loading) onButtonClicked();
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
        }
    }

    private void setButtonAsDefault(){
        phone.setEnabled(true);
        Hey.setButtonAsDefault(this,button,getString(R.string.verify));
        loading = false;
    }

    private void setButtonAsLoading(){
        phone.setEnabled(false);
        Hey.setButtonAsLoading(this,button);
        loading = true;
    }
}