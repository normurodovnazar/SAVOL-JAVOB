package com.normurodov_nazar.savol_javob.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

public class Info extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        textView = findViewById(R.id.rules);
        String s = getString(R.string.rulesFull)
                .replaceAll("lll","\n")
                .replaceAll("xxx",getString(R.string.need))
                .replaceAll("yyy",getString(R.string.my_questions))
                .replaceAll("zzz", String.valueOf(My.unitsForAd))
                .replaceAll("ttt",getString(R.string.showAd));
        textView.setText(s);
    }
}