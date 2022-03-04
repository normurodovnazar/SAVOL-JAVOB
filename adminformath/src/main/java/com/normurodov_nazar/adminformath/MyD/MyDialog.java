package com.normurodov_nazar.adminformath.MyD;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.normurodov_nazar.savol_javob.R;

public class MyDialog extends Dialog implements View.OnClickListener{
    private final String message;

    public MyDialog(@NonNull Context context, String message) {
        super(context);
        this.message=message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog);
        Button closer = findViewById(R.id.buttonErrorDialog);
        TextView textView = findViewById(R.id.textErrorDialog);
        closer.setOnClickListener(this);
        textView.setText(message);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
