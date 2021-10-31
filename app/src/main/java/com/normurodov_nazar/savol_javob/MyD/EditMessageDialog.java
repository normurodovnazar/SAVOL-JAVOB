package com.normurodov_nazar.savol_javob.MyD;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.util.HashMap;
import java.util.Map;

public class EditMessageDialog extends Dialog {
    EditText text;
    Button ok,cancel;
    final Message message;
    CollectionReference chats;

    public EditMessageDialog(@NonNull Context context, Message message, CollectionReference chats) {
        super(context);
        this.message = message;
        this.chats = chats;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_edit_message_dialog);
        text = findViewById(R.id.editingField);
        ok = findViewById(R.id.ok_button);ok.setOnClickListener(v -> {
            String m = text.getText().toString();
            if(!m.isEmpty() && !m.replaceAll(" ","").isEmpty()){
                Map<String,Object> data = new HashMap<>();
                data.put(Keys.message,m);
                chats.document(message.getId()).update(data).addOnSuccessListener(unused -> Hey.print("a","succes")).addOnFailureListener(e -> Hey.print("a",e.getLocalizedMessage()));
                dismiss();
            } else Toast.makeText(getContext(), getContext().getString(R.string.emty), Toast.LENGTH_SHORT).show();
        });
        cancel = findViewById(R.id.cancel_b);cancel.setOnClickListener(v -> dismiss());
        text.setText(message.getMessage());
    }
}
