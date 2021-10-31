package com.normurodov_nazar.savol_javob.MyD;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.R;

import java.util.HashMap;

public class AddItemDialog extends Dialog {
    Button ok,cancel;
    EditText text;
    final CollectionReference reference;
    final String user;
    public AddItemDialog(@NonNull Context context, CollectionReference reference, String user) {
        super(context);
        this.reference = reference;
        this.user = user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_item);
        ok = findViewById(R.id.addItemOk);
        cancel = findViewById(R.id.cancel_bu);
        text = findViewById(R.id.itemField);text.setText(user);
        cancel.setOnClickListener(v -> dismiss());
        ok.setOnClickListener(v -> add());
    }

    private void add() {
        String t = text.getText().toString();
        if (!t.isEmpty()){
            dismiss();
            Hey.addDocumentToCollection(getContext(), reference, t, new HashMap<>(), doc -> {

            }, errorMessage -> {

            });
        }
    }
}
