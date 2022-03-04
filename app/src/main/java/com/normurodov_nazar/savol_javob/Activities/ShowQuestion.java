package com.normurodov_nazar.savol_javob.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.R;

import java.util.List;

public class ShowQuestion extends AppCompatActivity {
    String questionId;
    Message message;
    boolean imageIsShowing = false;

    ImageView image;
    TextView messageText;
    SubsamplingScaleImageView bigImage;
    ConstraintLayout main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_question);
        initVars();
    }

    private void initVars() {
        main = findViewById(R.id.main);
        image = findViewById(R.id.questionImageInShowQuestion);image.setOnClickListener(view -> onCLickImage());
        messageText = findViewById(R.id.textInShowQuestion);
        bigImage = findViewById(R.id.bigImageInShowQuestion);
        questionId = getIntent().getStringExtra(Keys.id);
        if (questionId==null) Hey.showErrorMessage(this,getString(R.string.error_unknown),true);
        else {
            LoadingDialog dialog = Hey.showLoadingDialog(this, (position, name) -> finish());
            FirebaseFirestore.getInstance().collection(Keys.chats).document(questionId).collection(Keys.chats).orderBy(Keys.time, Query.Direction.ASCENDING).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
                dialog.closeDialog();
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                if (!list.isEmpty()){
                    message = Message.fromDoc(list.get(0));
                    messageText.setText(message.getMessage());
                    Hey.workWithImageMessage(message, doc1 -> image.setImageURI(Uri.fromFile(Hey.getLocalFile(message))), errorMessage -> Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            Hey.showDownloadDialog(ShowQuestion.this, message, doc12 -> image.setImageURI(Uri.fromFile(Hey.getLocalFile(message))), errorMessage1 -> { });
                        }

                        @Override
                        public void offline() {
                            Hey.showErrorMessage(ShowQuestion.this,getString(R.string.error_connection),true);
                        }
                    }, errorMessage12 -> { },this));
                }
            }).addOnFailureListener(e -> Hey.showErrorMessage(this,e.getLocalizedMessage(),true));
        }
    }

    private void onCLickImage() {
        showBigImage();
        imageIsShowing = true;
        Hey.setBigImage(bigImage,Hey.getLocalFile(message));
    }

    @Override
    public void onBackPressed() {
        if (imageIsShowing){
            hideBigImage();
            imageIsShowing = false;
        }else super.onBackPressed();
    }

    void showBigImage(){
        main.setVisibility(View.INVISIBLE);
        bigImage.setVisibility(View.VISIBLE);
    }

    void hideBigImage(){
        main.setVisibility(View.VISIBLE);
        bigImage.setVisibility(View.INVISIBLE);
    }
}