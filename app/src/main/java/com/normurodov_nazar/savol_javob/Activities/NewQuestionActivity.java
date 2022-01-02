package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewQuestionActivity extends AppCompatActivity {
    Button subject,theme,image,publish;
    EditText number;
    ImageView subjectList,themeList,questionImage;
    ArrayList<String> subjects = new ArrayList<>(),themes = new ArrayList<>();
    String subjectS = "",themeS = "",filePath = "";
    ActivityResultLauncher<Intent> subjectR,themeR,imageR;
    Map<String,Object> data = new HashMap<>();
    long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_question);
        initVars();
        opportunityTest();
    }

    private void opportunityTest() {
        My.questionOpportunity=100;
        if (My.questionOpportunity==0) Hey.showAlertDialog(this,getString(R.string.no_opportunity)).setOnDismissListener(dialog -> {
            startActivity(new Intent(this,ShowAd.class));
            this.finish();
        });
        else publishQuestion();
    }

    private void publishQuestion() {
        String n = number.getText().toString();
        if(!n.isEmpty()) Hey.showToast(this,getString(R.string.enter_question_number)); else {
            Question question = new Question(data);
            Hey.amIOnline(new StatusListener() {
                @Override
                public void online() {
                    Hey.addDocumentToCollection(getApplicationContext(), FirebaseFirestore.getInstance().collection(Keys.publicQuestions), question.getQuestionId(), question.toMap(), doc -> {
                        Hey.showToast(getApplicationContext(),"ADDED");
                    }, errorMessage -> {
                        Hey.showToast(getApplicationContext(),"ERROR");
                    });
                }

                @Override
                public void offline() {
                    Hey.showToast(getApplicationContext(),"OFFLINE");
                }
            }, errorMessage -> {
                Hey.showToast(getApplicationContext(),"ERROR");
            },this);

        }
    }

    private void initVars() {
        subjectR = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> onSubjectR(true)
        );
        themeR = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> onSubjectR(false)
        );
        imageR = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onPickImageResult
        );
        subjectList = findViewById(R.id.listSubject);subjectList.setOnClickListener(v -> showSubjects());
        themeList = findViewById(R.id.listTheme);themeList.setOnClickListener(v -> showThemes());
        subject = findViewById(R.id.selectSubject);subject.setOnClickListener(view -> {
            Intent i = new Intent(this,SelectItem.class);
            i.putExtra("a", Keys.subject);
            My.result = subjects;
            subjectR.launch(i);
        });
        theme = findViewById(R.id.selectTheme);theme.setOnClickListener(v -> {
            Intent i = new Intent(this,SelectItem.class);
            i.putExtra("a", Keys.theme);
            My.result = themes;
            themeR.launch(i);
        });
        image = findViewById(R.id.selectImage);image.setOnClickListener(v -> chooseImage());
        publish = findViewById(R.id.publish);publish.setOnClickListener(v -> publishQuestion());
        number = findViewById(R.id.itemText);
        time = Timestamp.now().toDate().getTime();
        filePath = My.folder+My.id+time+".png";
        data.put(Keys.time, time);
        data.put(Keys.sender,My.id);
        questionImage = findViewById(R.id.questionImage);
    }

    private void onPickImageResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri uri = result.getData().getData();
            Hey.cropImage(this, this, uri, new File(filePath), false, errorMessage -> {Hey.print("a","AAA"); });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP) if (data != null && resultCode == RESULT_OK) {
            Uri res = UCrop.getOutput(data);
            if (res != null) {
                questionImage.setImageDrawable(new ColorDrawable(Color.WHITE));
                Log.e("onActivityResult", "Result is not null:"+res.getPath());
                filePath = res.getPath();
                ImageUploadingDialog d = Hey.uploadImageForProfile(this, filePath, String.valueOf(My.id), doc -> {
                    questionImage.setImageURI(res);
                }, (position, name) -> {

                });
            } else {
                Log.e("onActivityResult", "Result is null");
                Toast.makeText(this, "xxx", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chooseImage() {
        Hey.pickImage(imageR);
    }

    private void onSubjectR(boolean isSubject) {
        if(My.isSuccess && !My.result.isEmpty()) {
            if(isSubject){
                subjects = new ArrayList<>();
                for(String s:My.result) if (!subjects.contains(s)) subjects.add(s);
                subjectS = "";
                for (String i:subjects) if(subjects.get(0).equals(i)) subjectS+=i; else subjectS+=","+i;
                Hey.print("A","s:"+subjectS);
                data.remove(Keys.subject);
                data.put(Keys.subject,subjectS);
            } else {
                themes = new ArrayList<>();
                for(String a:My.result) if(!themes.contains(a)) themes.add(a);
                themeS = "";
                for (String i:themes) if(themes.get(0).equals(i)) themeS+=i; else themeS+=","+i;
                data.remove(Keys.theme);
                data.put(Keys.theme,themeS);
            }
            My.result.clear();
            My.isSuccess = false;
        }
    }

    private void showThemes() {
        if(themes.size()==0) Hey.showToast(this,getString(R.string.you_not_selected)); else {
            Hey.showAlertDialog(this, themeS);
        }
    }

    private void showSubjects() {
        if(subjects.size()==0) Hey.showToast(this,getString(R.string.you_not_selected)); else {
            Hey.showAlertDialog(this, subjectS);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            Hey.print("A",subjects.toString());
            return true;
        }else
        return super.onKeyDown(keyCode, event);
    }
}