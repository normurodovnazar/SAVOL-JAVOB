package com.normurodov_nazar.savol_javob.MyD;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.showToast;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.R;

import java.util.HashMap;
import java.util.Map;

public class EditMessageDialog extends Dialog {
    EditText text;
    Button ok, cancel;
    final Map<String, Object> data;
    final SuccessListener successListener;
    final EditMode editMode;
    final DocumentReference document;


    public EditMessageDialog(@NonNull Context context, Map<String, Object> data, DocumentReference chat, EditMode editMode, SuccessListener successListener) {
        super(context);
        this.data = data;
        this.document = chat;
        this.successListener = successListener;
        this.editMode = editMode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_edit_message_dialog);
        text = findViewById(R.id.editingField);
        ok = findViewById(R.id.ok_button);
        ok.setOnClickListener(v -> {
            String m = text.getText().toString();
            if (!m.isEmpty() && !m.replaceAll(" ", "").isEmpty()) {
                Map<String, Object> x = new HashMap<>();
                switch (editMode) {
                    case name:
                        x.put(Keys.name, m);
                        break;
                    case surname:
                        x.put(Keys.surname, m);
                        break;
                    case message:
                        x.put(Keys.message, m);
                        break;
                }
                Hey.updateDocument(getContext(), document, x, doc -> successListener.onSuccess(x), errorMessage -> {
                });
                dismiss();
            } else showToast(getContext(), R.string.empty);
        });
        cancel = findViewById(R.id.cancel_b);
        cancel.setOnClickListener(v -> dismiss());
        switch (editMode) {
            case name:
                text.setText((String) data.get(Keys.name));
                break;
            case surname:
                text.setText((String) data.get(Keys.surname));
                break;
            case message:
                text.setText((String) data.get(Keys.message));
                break;
        }
    }
}
