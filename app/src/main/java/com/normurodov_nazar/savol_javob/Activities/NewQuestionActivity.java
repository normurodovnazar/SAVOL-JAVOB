package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.constraintlayout.widget.ConstraintLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewQuestionActivity extends AppCompatActivity {
    ConstraintLayout main;
    Button image, publish, selectTheme;
    EditText message,number;
    ImageView questionImage;
    SubsamplingScaleImageView scaleImageView;
    String filePath = "", theme = "";
    boolean imageSelected = false;
    ActivityResultLauncher<Intent> imageR, themeR;
    Map<String, Object> data = new HashMap<>();
    long time;
    boolean imageShowing = false;
    File file;
    CollectionReference publicQuestions = FirebaseFirestore.getInstance().collection(Keys.publicQuestions),
            allQuestions = FirebaseFirestore.getInstance().collection(Keys.allQuestions), chats = FirebaseFirestore.getInstance().collection(Keys.chats),
    myAllQuestions = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.allQuestions);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_question);
        initVars();
    }

    private void publishQuestion() {
        String n = message.getText().toString(),x = number.getText().toString();
        int i = -1;
        try{
            i = Integer.parseInt(x);
        } catch (NumberFormatException e){
            Hey.showToast(this,e.getLocalizedMessage());
        }
        if (i!=-1) if (My.units >= i * My.unitsForPerDay){
            if (theme.isEmpty()) Hey.showToast(this, getString(R.string.themeReq));
            else if (n.isEmpty()) Hey.showToast(this, getString(R.string.emty));
            else if (!imageSelected) Hey.showToast(this, getString(R.string.you_need_upload_image));
            else {
                data.put(Keys.sender, My.id);
                data.put(Keys.time, time);
                data.put(Keys.message, n);
                data.put(Keys.theme, theme+Keys.incorrect);
                data.put(Keys.imageSize,file.length());
                data.put(Keys.visibleTime,time+ 24L *60*60*1000*i);
                Question question = new Question(data);
                int finalI = i;
                Hey.amIOnline(new StatusListener() {
                    @Override
                    public void online() {
                        LoadingDialog d = Hey.showLoadingDialog(NewQuestionActivity.this);
                        Hey.getCollection(NewQuestionActivity.this, publicQuestions, docs -> {
                            if (My.questionLimit > docs.size()) {
                                Hey.uploadImageToChat(NewQuestionActivity.this, filePath, question.getQuestionId(), doc ->
                                        Hey.addDocumentToCollection(NewQuestionActivity.this, allQuestions, question.getQuestionId(), question.toMap(), doc0 -> Hey.addDocumentToCollection(NewQuestionActivity.this, publicQuestions, question.getQuestionId(), question.toMap(), doc1 -> Hey.addDocumentToCollection(NewQuestionActivity.this, myAllQuestions, question.getQuestionId(), question.toMap(), new SuccessListener() {
                                            @Override
                                            public void onSuccess(Object doc) {
                                                Map<String, Object> data = new HashMap<>();
                                                data.put(Keys.time, question.getTime());
                                                data.put(Keys.type, Keys.question);
                                                data.put(Keys.sender, question.getSender());
                                                data.put(Keys.message, question.getMessage());
                                                data.put(Keys.read, false);
                                                data.put(Keys.imageSize,file.length());
                                                Map<String,Object> x = new HashMap<>();
                                                x.put(Keys.numberOfMyPublishedQuestions,My.numberOfMyPublishedQuestions+1);
                                                x.put(Keys.units,My.units-My.unitsForPerDay* finalI);
                                                Hey.addDocumentToCollection(NewQuestionActivity.this, chats.document(question.getQuestionId()).collection(Keys.chats), question.getQuestionId(), data, doc2 -> Hey.updateDocument(NewQuestionActivity.this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), x, doc3 -> {
                                                    if (d.isShowing()) d.dismiss();
                                                    finish();
                                                }, errorMessage -> {

                                                }), errorMessage -> {
                                                    if (d.isShowing()) d.dismiss();
                                                });
                                            }
                                        }, errorMessage -> {

                                        }), errorMessage -> {
                                            if (d.isShowing()) d.dismiss();
                                        }), errorMessage -> { if (d.isShowing()) d.dismiss(); }), (position, name) -> {
                                }, errorMessage -> {
                                    if (d.isShowing()) d.dismiss();
                                });
                            }
                            else {
                                if (d.isShowing()) d.dismiss();
                                Hey.showAlertDialog(NewQuestionActivity.this, getString(R.string.questionLimitError).replace("xxx", String.valueOf(My.questionLimit)));
                            }
                        }, errorMessage -> {
                            if (d.isShowing()) d.dismiss();
                        });
                    }

                    @Override
                    public void offline() {
                        Hey.showToast(NewQuestionActivity.this, "OFFLINE");
                    }
                }, errorMessage -> {
                }, this);
            }
        } else Hey.showAlertDialog(this,getString(R.string.notEnoughUnitsForDay).replaceAll("xxx", String.valueOf(i)).replaceAll("yyy", String.valueOf(My.units)).replaceAll("zzz", String.valueOf(My.unitsForPerDay)));
    }

    private void initVars() {
        main = findViewById(R.id.mainNQ);
        selectTheme = findViewById(R.id.selectTheme);
        selectTheme.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectTheme.class);
            themeR.launch(i);
        });
        imageR = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onPickImageResult);
        themeR = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onThemeResult);
        image = findViewById(R.id.selectImage);
        image.setOnClickListener(v -> chooseImage());
        publish = findViewById(R.id.publish);
        publish.setOnClickListener(v -> publishQuestion());
        message = findViewById(R.id.itemText);
        questionImage = findViewById(R.id.questionImage);
        questionImage.setOnClickListener(v -> showImage());
        scaleImageView = findViewById(R.id.bigImageNQ);
        time = Calendar.getInstance().getTimeInMillis();
        number = findViewById(R.id.numberOfDays);
        filePath = My.folder + My.id + time + ".png";
    }

    private void showImage() {
        if (!imageShowing && file != null) {
            imageShowing = true;
            main.setVisibility(View.INVISIBLE);
            scaleImageView.setVisibility(View.VISIBLE);
            scaleImageView.setMaxScale(15);
            scaleImageView.setMinScale(0.1f);
            scaleImageView.setBackgroundColor(Color.BLACK);
            scaleImageView.setImage(ImageSource.uri(Uri.fromFile(file)));
        }
    }

    private void onThemeResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Intent o = result.getData();
            if (o != null) {
                theme = o.getStringExtra(Keys.theme);
                selectTheme.setText(getString(R.string.theme)+theme);
            }
        }
    }

    private void onPickImageResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri uri = result.getData().getData();
            Hey.cropImage(this, this, uri, new File(filePath), false, errorMessage -> Hey.print("a", errorMessage));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP)
            if (data != null && resultCode == RESULT_OK) {
                Uri res = UCrop.getOutput(data);
                if (res != null) {
                    imageSelected = true;
                    filePath = res.getPath();
                    file = new File(filePath);
                    questionImage.setImageURI(Uri.fromFile(file));
                    Log.e("onActivityResult", "Result is not null:" + res.getPath());
                } else {
                    Log.e("onActivityResult", "Result is null");
                    Toast.makeText(this, "xxx", Toast.LENGTH_SHORT).show();
                }
            }
    }

    @Override
    public void onBackPressed() {
        if (imageShowing) {
            imageShowing = false;
            scaleImageView.setVisibility(View.GONE);
            main.setVisibility(View.VISIBLE);
        } else super.onBackPressed();
    }

    private void chooseImage() {
        Hey.pickImage(imageR);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}