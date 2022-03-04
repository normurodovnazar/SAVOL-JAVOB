package com.normurodov_nazar.adminapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.adminapp.MFunctions.Hey;
import com.normurodov_nazar.adminapp.MFunctions.Keys;
import com.normurodov_nazar.adminapp.MFunctions.My;
import com.normurodov_nazar.adminapp.MyD.LoadingDialog;
import com.normurodov_nazar.adminapp.MyD.Question;
import com.normurodov_nazar.adminapp.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewQuestionActivity extends AppCompatActivity {
    TextView privacy;
    CheckBox checkBox;
    ConstraintLayout main;
    Button image, publish, selectTheme;
    EditText message, number;
    ImageView questionImage;
    SubsamplingScaleImageView scaleImageView;
    String filePath = "", theme = "", themeId = "";
    boolean imageSelected = false;
    ActivityResultLauncher<Intent> memoryLauncher, captureLauncher, themeR;
    final Map<String, Object> data = new HashMap<>();
    long time;
    Uri capture;
    boolean imageShowing = false;
    File file;
    CollectionReference publicQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_question);
        publicQuestions = FirebaseFirestore.getInstance().collection(Keys.publicQuestions + getString(R.string.lang));
        initVars();
    }

    private void publishQuestion() {
        String n = message.getText().toString(), x = number.getText().toString();
        int i = -1;
        if (checkBox.isChecked()){
            if (x.isEmpty()) {
                Hey.showToast(this, getString(R.string.setVisibleTime));
            } else
                try {
                    i = Integer.parseInt(x);
                } catch (NumberFormatException e) {
                    Hey.showToast(this, e.getLocalizedMessage());
                }
            if (i<=0) Hey.showToast(this, getString(R.string.setVisibleTime));
            else if (theme.isEmpty()) Hey.showToast(this, getString(R.string.themeReq));
            else if (n.isEmpty()) Hey.showToast(this, getString(R.string.empty));
            else if (!imageSelected) Hey.showToast(this, getString(R.string.you_need_upload_image));
            else {
                data.put(Keys.sender, My.id);
                data.put(Keys.time, time);
                data.put(Keys.message, n);
                data.put(Keys.theme, theme + Keys.incorrect);
                data.put(Keys.imageSize, file.length());
                data.put(Keys.visibleTime, time + 24L * 60 * 60 * 1000 * i);
                Question question = new Question(data);
                LoadingDialog dialog = Hey.showLoadingDialog(this);
                Hey.publishQuestion(this, question, i, filePath, false, doc -> {
                    dialog.closeDialog();
                    finish();
                }, errorMessage -> dialog.closeDialog());
            }
        }else Hey.showToast(this,getString(R.string.dontAgree));
    }

    private void initVars() {
        checkBox = findViewById(R.id.checkbox);
        privacy = findViewById(R.id.privacy);Hey.gotoPrivacy(this,privacy);
        main = findViewById(R.id.mainNQ);
        selectTheme = findViewById(R.id.selectTheme);
        selectTheme.setOnClickListener(v -> {
            Intent i = new Intent(this, SelectTheme.class);
            i.putExtra("s", true);
            themeR.launch(i);
        });
        memoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onMemoryResult);
        captureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onCaptureResult);
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
            Hey.setBigImage(scaleImageView, file);
        }
    }

    private void onThemeResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Intent o = result.getData();
            if (o != null) {
                theme = o.getStringExtra(Keys.theme);
                themeId = o.getStringExtra(Keys.id);
                String s = getString(R.string.theme) + theme;
                selectTheme.setText(s);
            }
        }
    }

    private void onMemoryResult(ActivityResult result) {
        Hey.print("onMemoryResult", result.toString());
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                Hey.cropImage(this, this, uri, new File(filePath), false, errorMessage -> {
                });
            } else Hey.showToast(this, getString(R.string.error));
        }
    }

    private void onCaptureResult(ActivityResult result) {
        Hey.print("onCaptureResult", result.toString());
        if (result.getResultCode() == RESULT_OK)
            Hey.cropImage(this, this, capture, new File(filePath), false, errorMessage -> {
            });
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
                    long startTime = Hey.getCurrentTime();
                    Hey.compressImage(this,file);
                    long endTime = Hey.getCurrentTime();
                    Hey.print("cropDuration", String.valueOf(endTime/1000-startTime/1000));
                    questionImage.setImageResource(R.drawable.search_glass);
                    questionImage.setImageURI(Uri.fromFile(file));
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
        Hey.chooseImage(this, image, memoryLauncher, captureLauncher, uri -> capture = uri);
    }
}