package com.normurodov_nazar.savol_javob.MyD;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;

import com.normurodov_nazar.savol_javob.R;

public class LoadingDialog extends Dialog {
    boolean fromUser = true;
    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_dialog);
    }

    public void closeDialog(){
        fromUser = false;
        dismiss();
    }

    public boolean isFromUser() {
        return fromUser;
    }
}
