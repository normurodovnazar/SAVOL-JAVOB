package com.normurodov_nazar.savol_javob.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.databinding.ActivityQuestionFilterBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class QuestionFilter extends AppCompatActivity implements View.OnClickListener {

    boolean descending = true,before = true, correct = true,hide = false;
    String themeS = "";
    long time = Calendar.getInstance().getTimeInMillis();
    private ActivityQuestionFilterBinding b;

    ActivityResultLauncher<Intent> themeR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityQuestionFilterBinding.inflate(getLayoutInflater());
        View v = b.getRoot();
        setContentView(v);
        initVars();
        if (hide) {
            b.statusOfQuestion.setVisibility(View.INVISIBLE);
            b.status.setVisibility(View.INVISIBLE);
        }
        showChanges();
    }

    private void initVars() {
        themeR = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onResult);
        Intent data = getIntent();
        themeS = data.getStringExtra(Keys.theme);
        correct = data.getBooleanExtra(Keys.correct,true);
        descending = data.getBooleanExtra(Keys.order,true);
        before = data.getBooleanExtra(Keys.divider,true);
        time = data.getLongExtra(Keys.time,Hey.getCurrentTime());
        hide = data.getBooleanExtra(Keys.hidden,false);
        b.selectTheme.setOnClickListener(this);
        b.direction.setOnClickListener(this);
        b.selectDate.setOnClickListener(this);
        b.divider.setOnClickListener(this);
        b.statusOfQuestion.setOnClickListener(this);
        b.apply.setOnClickListener(this);
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
        if (b.apply.equals(v)) {
            if (!themeS.isEmpty()) {
                Intent i = new Intent();
                i.putExtra(Keys.theme,themeS).putExtra(Keys.order,descending).
                        putExtra(Keys.time,time).putExtra(Keys.divider,before)
                        .putExtra(Keys.status, correct);
                setResult(RESULT_OK,i);
                finish();
            } else Hey.showToast(this, getString(R.string.selectThemeAtLeast));
        } else {
            if (b.selectTheme.equals(v)) {
                themeR.launch(new Intent(this, SelectTheme.class).putExtra("s",true));
            }
            if (b.direction.equals(v)) {
                Hey.showPopupMenu(this, b.direction, new ArrayList<>(Arrays.asList(getString(R.string.ascending), getString(R.string.descending))), (position, name) -> {
                    descending = position == 1;
                    showChanges();
                }, true);
            }
            if (b.selectDate.equals(v)) {
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
            if (b.divider.equals(v)) {
                Hey.showPopupMenu(this, b.divider, new ArrayList<>(Arrays.asList(getString(R.string.after), getString(R.string.before))), (position, name) -> {
                    before = position==1;
                    showChanges();
                }, true);
            }
            if (b.statusOfQuestion.equals(v)) {
                Hey.showPopupMenu(this, b.statusOfQuestion, new ArrayList<>(Arrays.asList(getString(R.string.answered), getString(R.string.unanswered))), (position, name) -> {
                    correct = position==0;
                    showChanges();
                },true);
            }
        }
    }

    void showChanges(){
        if (!themeS.isEmpty()) b.theme.setText(getString(R.string.theme)+themeS);
        b.direction.setText(getString(R.string.order)+getString(descending ? R.string.descending : R.string.ascending));
        b.date.setText(getString(R.string.date)+Hey.getTimeText(this,time));
        b.status.setText(getString(R.string.statusQuestion)+":"+getString(correct ? R.string.answered : R.string.unanswered));
        b.divider.setText(before ? R.string.before : R.string.after);
    }
}