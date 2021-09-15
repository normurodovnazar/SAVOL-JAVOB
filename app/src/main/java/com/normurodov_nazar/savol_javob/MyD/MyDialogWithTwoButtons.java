package com.normurodov_nazar.savol_javob.MyD;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.normurodov_nazar.savol_javob.R;

public class MyDialogWithTwoButtons extends Dialog {
    Button yes,no;
    TextView info;
    String y,n,i;
    boolean result = false;

    public MyDialogWithTwoButtons(@NonNull Context context,String y,String n,String i) {
        super(context);
        this.y = y;
        this.n = n;
        this.i = i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_mydialogwithtwobuttons);
        yes = findViewById(R.id.yes);yes.setText(y);yes.setOnClickListener(v -> {result = true;dismiss();});
        no = findViewById(R.id.no);no.setText(n);no.setOnClickListener(v -> {result = false;dismiss();});
        info = findViewById(R.id.textErrorDialog);info.setText(i);
    }

    public boolean getResult(){
        return result;
    }
}
