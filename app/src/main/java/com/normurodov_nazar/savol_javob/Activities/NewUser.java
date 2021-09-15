package com.normurodov_nazar.savol_javob.Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewUser extends AppCompatActivity implements View.OnClickListener {
    ImageView i;
    EditText name,surname;
    Button next,imageB;
    String mName="",mSurname="",mImage = "",mFilePath = "";
    ActivityResultLauncher<Intent> imagePickLauncher;
    File originalFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        initVars();
        originalFile = new File(getExternalFilesDir("images").toString()+File.separatorChar+"Me.png");
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
        UCrop.Options options = new UCrop.Options();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int blackColor = getColor(R.color.black);
            int whiteColor = getColor(R.color.white);
            options.setRootViewBackgroundColor(blackColor);
            options.setStatusBarColor(blackColor);
            options.setLogoColor(blackColor);
            options.setActiveControlsWidgetColor(whiteColor);
            options.setToolbarWidgetColor(blackColor);
            options.setCropFrameColor(blackColor);
            options.setCropGridColor(blackColor);
        }
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        if(originalFile.exists()){
            Uri target = Uri.fromFile(originalFile);
            UCrop.of(uri,target).withAspectRatio(1,1)
                    .withOptions(options)
                    .start(this);
        }else{
            Toast.makeText(this, getString(R.string.file_not_exists), Toast.LENGTH_SHORT).show();
            changeImageButtonAsDefault();
        }
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
        if(!My.loading)
            switch (v.getId()){
            case R.id.next:
                mName = name.getText().toString();
                mSurname = surname.getText().toString();
                Toast.makeText(this, mName+" "+mSurname+" "+mImage, Toast.LENGTH_SHORT).show();

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
        try {
            Hey.amIOnline().addOnCompleteListener(task -> {
                if(!task.isSuccessful() || task.getResult() == null) throw new NullPointerException();
                if(!task.getResult().getMetadata().isFromCache()){
                    Map<String,Object> data = new HashMap<>();
                    data.put(Keys.name,mName);
                    data.put(Keys.surname,mSurname);
                    data.put(Keys.imageUrl,mImage);
                    data.put(Keys.myQuestionOpportunity,3);
                    data.put(Keys.numberOfMyAnswers,0);
                    data.put(Keys.numberOfMyPublishedQuestions,0);
                    String[] chats = {};
                    data.put(Keys.myChats,chats);
                    data.put(Keys.numberOfCorrectAnswers,0);data.put(Keys.numberOfIncorrectAnswers,0);
                    FirebaseFirestore.getInstance().collection(Keys.users).document(My.uId).set(data).addOnCompleteListener(task1 -> {
                        Intent intent = new Intent(NewUser.this,Home.class);
                        if(task1.isSuccessful()){
                            startActivity(intent);
                            finish();
                        }else {
                            Hey.showUnknownError(NewUser.this);
                            changeNextButtonAsDefault();
                        }
                    });
                }else {
                    Hey.showAlertDialog(NewUser.this,getString(R.string.error_connection));
                    changeNextButtonAsDefault();
                }
            })
            .addOnFailureListener(e -> {
                changeNextButtonAsDefault();
                Hey.showAlertDialog(this,getString(R.string.error_unknown)+e.getMessage());
            });
        }catch (NullPointerException e){
            Hey.showUnknownError(this);
            changeNextButtonAsDefault();
        }
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
        Hey.setButtonAsDefault(this,imageB,getString(R.string.choose_image));
    }
    private void changeImageButtonAsLoading(){
        Hey.setButtonAsLoading(this,imageB);
    }
    private void changeNextButtonAsDefault(){
        Hey.setButtonAsDefault(this,next,getString(R.string.verify));
    }
    private void changeNextButtonAsLoading(){
        Hey.setButtonAsLoading(this,next);
    }
}