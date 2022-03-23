package com.normurodov_nazar.savol_javob.MyD;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.getCurrentTime;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.sendMessage;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.setButtonAsDefault;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.setButtonAsLoading;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.showToast;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.util.HashMap;
import java.util.Map;

public class ReplyDialog extends Dialog  {
    final Message message;
    final CollectionReference chats;
    Button send,cancel;
    EditText edittext;
    ConstraintLayout parent,nested;

    public ReplyDialog(@NonNull Context context,Message message,CollectionReference chats) {
        super(context);
        this.message = message;
        this.chats = chats;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reply_layout);
        initVars();
    }

    private void initVars() {
        cancel = findViewById(R.id.cancel);cancel.setOnClickListener(c->dismiss());
        send = findViewById(R.id.send);send.setOnClickListener(c->{
            setButtonAsLoading(getContext(),send);
            sendReply();
        });
        nested = findViewById(R.id.nested);
        parent = findViewById(R.id.parent);
        edittext = findViewById(R.id.replyText);
        LayoutInflater inflater = getLayoutInflater();

        View v;
        if (message.getType().equals(Keys.textMessage)){
            v = inflater.inflate(R.layout.message_from_other,parent,false);
            TextView textView = v.findViewById(R.id.message),time = v.findViewById(R.id.time);
            textView.setText(message.getMessage());time.setText(Hey.getTimeText(getContext(),message.getTime()));
        }else {
            v = inflater.inflate(R.layout.image_message_from_other,parent,false);
            TextView time = v.findViewById(R.id.time),imageSize = v.findViewById(R.id.imageSize);
            ImageView image = v.findViewById(R.id.image);
            time.setText(Hey.getTimeText(getContext(),message.getTime()));
            imageSize.setText(Hey.getMb(message.getImageSize()));
            Hey.workWithImageMessage(message, doc -> image.setImageURI(Uri.fromFile(Hey.getLocalFile(message))), errorMessage -> { });
        }
        nested.addView(v);
    }

    private void sendReply() {
        String text = edittext.getText().toString();
        if (text.isEmpty() || text.replaceAll(" ","").isEmpty()) {
            showToast(getContext(),R.string.empty);
            asDefault();
        } else {
            long currentTime = getCurrentTime();
            Map<String,Object> data = new HashMap<>();
            data.put(Keys.time,currentTime);
            data.put(Keys.read,false);
            data.put(Keys.type,Keys.reply);
            data.put(Keys.sender, My.id);
            data.put(Keys.to,message.getId());
            data.put(Keys.toType,message.getType());
            data.put(Keys.message,text);
            Message message = new Message(data);
            Hey.amIOnline(new StatusListener() {
                @Override
                public void online() {
                    sendMessage(getContext(),chats, message, doc -> dismiss(), errorMessage -> {
                        showToast(getContext(),R.string.error,errorMessage);
                        asDefault();
                    });
                }

                @Override
                public void offline() {
                    showToast(getContext(),R.string.error_connection);
                    asDefault();
                }
            }, errorMessage -> {},getContext());
        }
    }

    void asDefault(){
        setButtonAsDefault(getContext(),send, R.string.send);
    }
}
