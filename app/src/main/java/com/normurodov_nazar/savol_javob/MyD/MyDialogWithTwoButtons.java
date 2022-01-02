package com.normurodov_nazar.savol_javob.MyD;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.R;

public class MyDialogWithTwoButtons extends Dialog {
    Button yes, no;
    TextView info, m, t;
    Message message;
    String i;
    boolean result = false;

    public MyDialogWithTwoButtons(@NonNull Context context, String i, Message message) {
        super(context);
        this.i = i;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_mydialogwithtwobuttons);
        yes = findViewById(R.id.yes);
        yes.setOnClickListener(v -> {
            result = true;
            dismiss();
        });
        no = findViewById(R.id.no);
        no.setOnClickListener(v -> {
            result = false;
            dismiss();
        });
        info = findViewById(R.id.textErrorDialog);
        info.setText(i);
        m = findViewById(R.id.messageFromMeSelector);
        t = findViewById(R.id.timeMessageFromMeSelector);
        if (message.getType().equals(Keys.textMessage)) {
            m.setText(message.getMessage());
            t.setText(Hey.getSeenTime(getContext(), message.getTime()));
        } else {
            m.setVisibility(View.INVISIBLE);
            t.setVisibility(View.INVISIBLE);
        }
    }

    public boolean getResult() {
        return result;
    }
}
