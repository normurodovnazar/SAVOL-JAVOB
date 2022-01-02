package com.normurodov_nazar.savol_javob.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.ImageUploadingDialog;
import com.normurodov_nazar.savol_javob.MyD.ItemClickListener;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

public class NewUser extends AppCompatActivity implements View.OnClickListener {
    ImageView i;
    EditText name,surname;
    Button next,imageB;
    String mName="",mSurname="",mImage = "",mFilePath = "";
    ActivityResultLauncher<Intent> imagePickLauncher;
    File originalFile;
    boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        initVars();
        originalFile = new File(My.folder+"me");
    }

    private void initVars() {
        i = findViewById(R.id.profileImageNewUser);
        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        next = findViewById(R.id.next);
        imageB = findViewById(R.id.addImage);
        next.setOnClickListener(this);
        imageB.setOnClickListener(this);

        Hey.animateVertically(findViewById(R.id.card),400,300);
        Hey.animateVertically(i,400,600);
        Hey.animateHorizontally(imageB,400,900);
        Hey.animateVertically(name,400,1200);
        Hey.animateVertically(surname,400,1500);

        Hey.animateFadeOut(next,1800);
        imagePickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onPickImageResult
        );
    }

    private void onPickImageResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri uri = result.getData().getData();
            cropImage(uri);
        }else orElse();
    }

    void orElse(){
        changeImageButtonAsDefault();
        mImage="";
        i.setImageResource(R.drawable.tab1_icon);
    }

    private void cropImage(Uri uri){
        Hey.cropImage(this, this, uri, originalFile, true, errorMessage -> {
            Toast.makeText(getApplicationContext(), getString(R.string.file_not_exists), Toast.LENGTH_SHORT).show();
            changeImageButtonAsDefault();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP){
            if (data != null && resultCode == RESULT_OK) {
                Log.e("onActivityResult", "Result came");
                changeImageButtonAsDefault();
                Uri res = UCrop.getOutput(data);
                if (res != null) {
                    i.setImageResource(R.drawable.tab1_icon);
                    Log.e("onActivityResult", "Result is not null:"+res.getPath());
                    i.setImageURI(res);
                    mFilePath = res.getPath();
                    ImageUploadingDialog d = Hey.uploadImageForProfile(this, mFilePath, String.valueOf(My.id), doc -> {

                    }, (position, name) -> {

                    });
                    d.setOnDismissListener(x-> mImage = d.getDownloadUrl());
                } else {
                    Log.e("onActivityResult", "Result is null");
                    Toast.makeText(this, "xxx", Toast.LENGTH_SHORT).show();
                }
            } else if (data != null) {
                Log.e("onActivityResult", "Result came with error");
                changeImageButtonAsDefault();
                Throwable throwable = UCrop.getError(data);
                String mes;
                if (throwable != null) mes = throwable.getMessage();
                else mes = getString(R.string.unknown);
                Hey.showAlertDialog(this, getString(R.string.error_unknown) + mes);
            }
            changeImageButtonAsDefault();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if(!loading)
            switch (v.getId()){
            case R.id.next:
                mName = name.getText().toString();
                mSurname = surname.getText().toString();
                if(mName.equals("") || mSurname.equals("") || mImage.equals(""))
                    Toast.makeText(this, getText(R.string.name_surname_url_required), Toast.LENGTH_SHORT).show(); else {
                        changeNextButtonAsLoading();
                        nextPressed();
                }
                break;
            case R.id.addImage:
                    changeImageButtonAsLoading();
                    doWorksWithFile();
                    Intent i = new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_GET_CONTENT);
                    imagePickLauncher.launch(Intent.createChooser(i, getString(R.string.choose_image)));
                break;
        }
    }

    private void nextPressed() {
            Hey.amIOnline(new StatusListener() {
                @Override
                public void online() {
                    createUserById();
                }

                @Override
                public void offline() {
                    Hey.showAlertDialog(NewUser.this,getString(R.string.error_connection));
                    changeNextButtonAsDefault();
                }
            }, errorMessage -> changeNextButtonAsDefault(),this);
    }

    private void createUserById() {
        User me = new User(mName,mSurname,mImage,Timestamp.now().toDate().getTime(),My.number,My.id,0,0,0,0, 0);
        FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).set(me.toMap()).addOnCompleteListener(task1 -> {
            Intent intent = new Intent(NewUser.this,Home.class);
            if(task1.isSuccessful()){
                My.setDataFromUser(me);
                startActivity(intent);
                finish();
            }else {
                Hey.showUnknownError(NewUser.this);
                changeNextButtonAsDefault();
            }
        });
    }

    private void doWorksWithFile() {
        if(originalFile.exists()){
            Log.e("doWorksWithFile","File is exists.Now we will delete and create new one after delete");
            deleteAndThenCreateOriginalFile();
        }else {
            Log.e("doWorksWithFile","File is not exists.We will just create a new file");
            createOriginalFile();
        }
    }

    private void deleteAndThenCreateOriginalFile() {
        boolean deleted = originalFile.delete();
        if (deleted) {
            createOriginalFile();
        }else {
            Toast.makeText(this, getString(R.string.cannot_delete_existing_file), Toast.LENGTH_SHORT).show();
        }
    }

    private void createOriginalFile() {
        try {
            boolean created = originalFile.createNewFile();
            if(!created){
                Toast.makeText(this, getString(R.string.error_on_creating_image_file), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeImageButtonAsDefault(){
        Hey.setButtonAsDefault(this,imageB,getString(R.string.choose_image),loading);
    }
    private void changeImageButtonAsLoading(){
        Hey.setButtonAsLoading(this,imageB,loading);
    }
    private void changeNextButtonAsDefault(){
        Hey.setButtonAsDefault(this,next,getString(R.string.verify),loading);
    }
    private void changeNextButtonAsLoading(){
        Hey.setButtonAsLoading(this,next,loading);
    }
}