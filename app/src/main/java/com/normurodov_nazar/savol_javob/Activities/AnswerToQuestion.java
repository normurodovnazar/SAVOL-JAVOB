package com.normurodov_nazar.savol_javob.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnswerToQuestion extends AppCompatActivity {
    EditText explanation;
    Button image, publish;
    ImageView imageView;
    ConstraintLayout main;
    SubsamplingScaleImageView scaleImageView;

    ActivityResultLauncher<Intent> launcher;
    long imageSentTime;
    File file;
    String chatId;
    CollectionReference chats;
    boolean imageShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_to_question);
        initVars();
    }

    private void initVars() {
        chatId = getIntent().getStringExtra(Keys.id);
        if (chatId==null){
            Hey.showToast(this,getString(R.string.error_unknown));
            finish();
        }else chats = FirebaseFirestore.getInstance().collection(Keys.chats).document(chatId).collection(Keys.chats);
        main = findViewById(R.id.mainPart);
        scaleImageView = findViewById(R.id.bigImageQ);
        explanation = findViewById(R.id.explanation);
        image = findViewById(R.id.imageAnswer);
        image.setOnClickListener(v -> getImage());
        publish = findViewById(R.id.answerQuestion);
        publish.setOnClickListener(v -> publishQ());
        imageView = findViewById(R.id.imageOfAnswer);
        imageView.setOnClickListener(v -> showImage());
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
    }

    private void showImage() {
        if (file != null)
            if (!imageShowing) {
                scaleImageView.setImage(ImageSource.uri(Uri.fromFile(file)));
                scaleImageView.setBackgroundColor(Color.BLACK);
                scaleImageView.setMaxScale(15);
                scaleImageView.setMinScale(0.1f);
                scaleImageView.setVisibility(View.VISIBLE);
                main.setVisibility(View.INVISIBLE);
                imageShowing = true;
            }
    }

    @Override
    public void onBackPressed() {
        if (imageShowing) {
            imageShowing = false;
            scaleImageView.setVisibility(View.GONE);
            main.setVisibility(View.VISIBLE);
        }
        else super.onBackPressed();
    }

    private void onResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri uri = result.getData().getData();
            imageSentTime = Hey.getCurrentTime();
            Hey.cropImage(this, this, uri, new File(My.folder + My.id + imageSentTime + ".png"), false, errorMessage -> { });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP)
            if (data != null && resultCode == RESULT_OK) {
                Uri res = UCrop.getOutput(data);
                if (res != null) {
                    file = new File(res.getPath());
                    imageView.setImageURI(Uri.fromFile(file));
                }
            }
        else if (data != null) {
                Log.e("onActivityResult", "Result came with error");
                Throwable throwable = UCrop.getError(data);
                String mes;
                if (throwable != null) mes = throwable.getMessage();
                else mes = getString(R.string.unknown);
                Hey.showAlertDialog(this, getString(R.string.error_unknown) + mes);
            }
    }

    private void getImage() {
        Hey.pickImage(launcher);
    }

    private void publishQ() {
        String t = explanation.getText().toString();
        if (!t.isEmpty() && !t.replaceAll(" ","").isEmpty() && file!=null){
            LoadingDialog a = Hey.showLoadingDialog(AnswerToQuestion.this);
            Hey.amIOnline(new StatusListener() {
                @Override
                public void online() {
                    Hey.uploadImageToChat(AnswerToQuestion.this, file.getPath(), String.valueOf(My.id)+imageSentTime, doc -> {
                        Hey.print("a", "uploaded");
                        Map<String, Object> answer = new HashMap<>();
                        answer.put(Keys.type, Keys.answer);
                        answer.put(Keys.sender, My.id);
                        answer.put(Keys.message, t);
                        answer.put(Keys.time, imageSentTime);
                        answer.put(Keys.read, false);
                        answer.put(Keys.imageSize,file.length());
                        Hey.sendMessage(AnswerToQuestion.this, chats, new Message(answer), doc1 -> {
                            Hey.updateDocument(AnswerToQuestion.this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), Collections.singletonMap(Keys.numberOfMyAnswers, My.numberOfMyAnswers + 1), doc2 -> {
                                if(a.isShowing()) a.dismiss();
                                finish();
                            }, errorMessage -> {

                            });
                        }, errorMessage -> {
                            if(a.isShowing()) a.dismiss();
                        });
                    }, (position, name) -> {
                        if(a.isShowing()) a.dismiss();
                    }, errorMessage -> {
                        if(a.isShowing()) a.dismiss();
                    });
                }

                @Override
                public void offline() {
                    Hey.showToast(AnswerToQuestion.this,getString(R.string.error_connection));
                    a.dismiss();
                }
            }, errorMessage -> {
                a.dismiss();
            },this);
        } else Hey.showToast(this,getString(R.string.explanationAndImage));

    }
}