package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.databinding.ActivityAnswerToQuestionBinding;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnswerToQuestion extends AppCompatActivity {

    ActivityResultLauncher<Intent> memoryLauncher,captureLauncher;
    Uri capture;
    long imageSentTime;
    File file;
    String chatId,questionId,theme;
    CollectionReference chats;
    boolean imageShowing = false;

    private ActivityAnswerToQuestionBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAnswerToQuestionBinding.inflate(getLayoutInflater());
        View v = b.getRoot();
        setContentView(v);
        initVars();
    }

    private void initVars() {
        Hey.gotoPrivacy(this,b.privacy);
        chatId = getIntent().getStringExtra(Keys.id);
        questionId = getIntent().getStringExtra(Keys.question);
        theme = getIntent().getStringExtra(Keys.theme);
        if (chatId==null || questionId==null || theme==null){
            Hey.showToast(this,getString(R.string.error));
            finish();
        }else {
            chats = FirebaseFirestore.getInstance().collection(Keys.chats).document(chatId).collection(Keys.chats);
        }
        b.chooseImage.setOnClickListener(c->getImage());
        b.answerQuestion.setOnClickListener(v -> {
            if (b.checkBox.isChecked()){
                publishQ();
            }else Hey.showToast(this,getString(R.string.dontAgree));
        });
        b.imageOfAnswer.setOnClickListener(v -> showImage());
        memoryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResultMemory
        );
        captureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResultCapture
        );
    }

    private void showImage() {
        if (file != null)
            if (!imageShowing) {
                Hey.setBigImage(b.bigImage,file);
                b.bigImage.setVisibility(View.VISIBLE);
                b.main.setVisibility(View.INVISIBLE);
                imageShowing = true;
            }
    }

    @Override
    public void onBackPressed() {
        if (imageShowing) {
            imageShowing = false;
            b.bigImage.setVisibility(View.GONE);
            b.main.setVisibility(View.VISIBLE);
        }
        else super.onBackPressed();
    }

    private void onResultMemory(ActivityResult result) {
        if (result.getData() != null) {
            Uri uri = result.getData().getData();
            imageSentTime = Hey.getCurrentTime();
            Hey.cropImage(this, this, uri, new File(My.folder + My.id + imageSentTime + ".png"), false, errorMessage -> { });
        }
    }

    private void onResultCapture(ActivityResult result){
        if (result.getResultCode() == RESULT_OK) {
            imageSentTime = Hey.getCurrentTime();
            Hey.cropImage(this, this, capture, new File(My.folder + My.id+imageSentTime + ".png"), false, errorMessage -> {});
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
                    Hey.compressImage(this,file);
                    b.imageOfAnswer.setImageURI(Uri.fromFile(file));
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
        Hey.chooseImage(this,b.chooseImage,memoryLauncher,captureLauncher,uri-> capture = uri);
    }

    private void publishQ() {
        String t = b.explanation.getText().toString();
        if (!t.isEmpty() && !t.replaceAll(" ","").isEmpty() && file!=null){
            LoadingDialog a = Hey.showLoadingDialog(AnswerToQuestion.this);
            Hey.amIOnline(new StatusListener() {
                @Override
                public void online() {
                    Hey.uploadImageToChat(AnswerToQuestion.this, file.getPath(), String.valueOf(My.id)+imageSentTime, doc -> {
                        Map<String, Object> answer = new HashMap<>();
                        answer.put(Keys.type, Keys.answer);
                        answer.put(Keys.sender, My.id);
                        answer.put(Keys.message, t);
                        answer.put(Keys.time, imageSentTime);
                        answer.put(Keys.read, false);
                        answer.put(Keys.imageSize,file.length());
                        Hey.sendMessage(AnswerToQuestion.this, chats, new Message(answer), doc1 -> {
                            Map<String,String> x = new HashMap<>();
                            x.put(Keys.type,Keys.needQuestions);
                            x.put(Keys.id,questionId);
                            x.put(Keys.theme,theme);
                            x.put(Keys.sender, String.valueOf(My.id));
                            Hey.sendNotification(AnswerToQuestion.this, My.fullName, getString(R.string.sentAnswer).replace("xxx", theme.replace(Keys.correct,"").replace(Keys.incorrect,"")), questionId, x, doc22 -> { }, errorMessage -> { });
                            Hey.updateDocument(AnswerToQuestion.this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), Collections.singletonMap(Keys.numberOfMyAnswers, My.numberOfMyAnswers + 1), doc2 -> {
                                if(a.isShowing()) a.closeDialog();
                                finish();
                            }, errorMessage -> {

                            });
                        }, errorMessage -> {
                            if(a.isShowing()) a.closeDialog();
                        });
                    }, (position, name) -> {
                        if(a.isShowing()) a.closeDialog();
                    }, errorMessage -> {
                        if(a.isShowing()) a.closeDialog();
                    });
                }

                @Override
                public void offline() {
                    Hey.showToast(AnswerToQuestion.this,getString(R.string.error_connection));
                    a.closeDialog();
                }
            }, errorMessage -> a.closeDialog(),this);
        } else Hey.showToast(this,getString(R.string.explanationAndImage));

    }
}