package com.normurodov_nazar.savol_javob.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class QuestionFilter extends AppCompatActivity implements View.OnClickListener {

    Button theme,direction,apply,selectDate,divider,statusQuestionB;
    TextView directionT,themeT,dateT,statusQuestionT;


    boolean descending = true,before = true, correct = true,hide = false;
    String themeS = "";
    long time = Calendar.getInstance().getTimeInMillis();


    ActivityResultLauncher<Intent> themeR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_filter);
        initVars();
        if (hide) {
            statusQuestionB.setVisibility(View.INVISIBLE);
            statusQuestionT.setVisibility(View.INVISIBLE);
        }
        showChanges();
    }

    private void initVars() {
        themeR = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onResult);
        directionT = findViewById(R.id.directionT);
        themeT = findViewById(R.id.themeT);
        dateT = findViewById(R.id.dateT);
        statusQuestionT = findViewById(R.id.statusT);

        Intent data = getIntent();
        themeS = data.getStringExtra(Keys.theme);
        correct = data.getBooleanExtra(Keys.correct,true);
        descending = data.getBooleanExtra(Keys.order,true);
        before = data.getBooleanExtra(Keys.divider,true);
        time = data.getLongExtra(Keys.time,Hey.getCurrentTime());
        hide = data.getBooleanExtra(Keys.hidden,false);
        theme = findViewById(R.id.themeF);theme.setOnClickListener(this);
        direction = findViewById(R.id.direction);direction.setOnClickListener(this);
        selectDate = findViewById(R.id.datePickerF);selectDate.setOnClickListener(this);
        divider = findViewById(R.id.divider);divider.setOnClickListener(this);
        statusQuestionB = findViewById(R.id.statusAnswerB);statusQuestionB.setOnClickListener(this);
        apply = findViewById(R.id.apply);apply.setOnClickListener(this);
    }



    void onResult(ActivityResult result){
        if (result.getResultCode()==RESULT_OK) {
            assert result.getData() != null;
            themeS = result.getData().getStringExtra(Keys.theme);
            showChanges();
        }
    }

    @Override
    public void onClick(View v) {
        if (apply.equals(v)) {
            if (!themeS.isEmpty()) {
                Intent i = new Intent();
                i.putExtra(Keys.theme,themeS).putExtra(Keys.order,descending).
                        putExtra(Keys.time,time).putExtra(Keys.divider,before)
                        .putExtra(Keys.status, correct);
                setResult(RESULT_OK,i);
                finish();
            } else Hey.showToast(this, getString(R.string.selectThemeAtLeast));
        } else {
            if (theme.equals(v)) {
                themeR.launch(new Intent(this, SelectTheme.class).putExtra("s",true));
            }
            if (direction.equals(v)) {
                Hey.showPopupMenu(this, direction, new ArrayList<>(Arrays.asList(getString(R.string.ascending), getString(R.string.descending))), (position, name) -> {
                    descending = position == 1;
                    showChanges();
                }, true);
            }
            if (selectDate.equals(v)) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(time);
                DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    time = c.getTimeInMillis();
                    showChanges();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
            if (divider.equals(v)) {
                Hey.showPopupMenu(this, divider, new ArrayList<>(Arrays.asList(getString(R.string.after), getString(R.string.before))), (position, name) -> {
                    before = position==1;
                    showChanges();
                }, true);
            }
            if (statusQuestionB.equals(v)) {
                Hey.showPopupMenu(this, statusQuestionB, new ArrayList<>(Arrays.asList(getString(R.string.answered), getString(R.string.unanswered))), (position, name) -> {
                    correct = position==0;
                    showChanges();
                },true);
            }
        }
    }

    void showChanges(){
        if (!themeS.isEmpty()) themeT.setText(getString(R.string.theme)+themeS);
        directionT.setText(getString(R.string.order)+getString(descending ? R.string.descending : R.string.ascending));
        dateT.setText(getString(R.string.date)+Hey.getSeenTime(this,time));
        statusQuestionT.setText(getString(R.string.statusQuestion)+":"+getString(correct ? R.string.answered : R.string.unanswered));
        divider.setText(before ? R.string.before : R.string.after);
    }
}