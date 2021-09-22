package com.normurodov_nazar.savol_javob.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.MessageAdapterInSingleChat;
import com.normurodov_nazar.savol_javob.MyD.TextMessage;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SingleChat extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView name;
    EditText editText;
    ImageButton send, sendImage, profileImage, menu;
    String chatId;
    CollectionReference chats;
    ProgressBar progressBar, barForImageDownload;
    List<Map<String, Object>> messages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);
        initTempVars();
        initVars();
        loadProfileImage();
        loadMessages();
    }

    private void loadProfileImage() {
        FirebaseFirestore.getInstance().collection(Keys.users).document(Hey.getOtherUIdFromChatId(chatId)).addSnapshotListener((value, error) -> {
            if (error != null) {
                if (value != null) {
                    String imageUrl = Objects.requireNonNull(value.get(Keys.imageUrl)).toString();
                    setImage(imageUrl);
                }
            }
        });
    }

    private void setImage(String imageUrl) {
        File file = new File(getExternalFilesDir("images").toString() + File.separatorChar + imageUrl);
        if (!file.exists()) {
            FirebaseStorage.getInstance().getReference().child(Keys.users).child(imageUrl).getFile(file)
                    .addOnFailureListener(e -> Hey.showAlertDialog(this, getString(R.string.error_unknown) + e.getMessage()))
                    .addOnProgressListener(snapshot -> barForImageDownload.setProgress((int) (snapshot.getBytesTransferred() / snapshot.getTotalByteCount())))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) setImage(imageUrl);
                    });
        } else {
            setReadyImage(file);
        }
    }

    void setReadyImage(File file) {
        barForImageDownload.setVisibility(View.INVISIBLE);
        profileImage.setVisibility(View.VISIBLE);
        profileImage.setImageURI(Uri.parse(file.getPath()));
    }

    private void initTempVars() {
        chatId = "aaaa";
        My.uId = "AA";
    }

    private void loadMessages() {
        chats.addSnapshotListener((value, error) -> {
            if (error == null) {
                if (value != null) {
                    List<DocumentSnapshot> docs = value.getDocuments();
                    for (DocumentSnapshot d : docs) {
                        messages.add(d.getData());
                    }
                    if (messages.size() != 0) {
                        List<TextMessage> t = new ArrayList<>();
                        for (Map<String, Object> m : messages) {
                            t.add(new TextMessage(Objects.requireNonNull(m.get(Keys.message)).toString(), Objects.requireNonNull(m.get(Keys.time)).toString(), m.get(Keys.sender).toString()));
                        }
                        MessageAdapterInSingleChat adapter = new MessageAdapterInSingleChat(t, this);
                        recyclerView.setAdapter(adapter);
                    }
                } else Hey.showAlertDialog(this, getString(R.string.error_unknown));
                ;
            } else {
                Hey.showAlertDialog(this, getString(R.string.error) + ":" + error.getMessage());
            }
        });
    }

    private void initVars() {
        recyclerView = findViewById(R.id.chatsInSingleChat);
        //String ch = getIntent().getStringExtra(Keys.chatId);if(ch!=null) chatId = ch; else Hey.showUnknownError(this).setOnDismissListener(d -> finish());
        send = findViewById(R.id.send);
        sendImage = findViewById(R.id.sendImage);
        profileImage = findViewById(R.id.profileImageInSingleChat);
        menu = findViewById(R.id.menuInSingleChat);
        name = findViewById(R.id.nameAndSurnameInSingleChat);
        editText = findViewById(R.id.editTextInSingleChat);
        progressBar = findViewById(R.id.progressBarInSingleChat);
        barForImageDownload = findViewById(R.id.barForImageDownload);
        send.setOnClickListener(v -> onClickSentMessage());
        chats = FirebaseFirestore.getInstance().collection(chatId);
    }

    private void onClickSentMessage() {
        if (notLoading()) {
            String m = editText.getText().toString();
            if (!m.isEmpty()) {
                chats.add((new TextMessage(m, "time", My.uId)).toMap()).
                        addOnFailureListener(e -> Hey.showToast(SingleChat.this, getString(R.string.error) + ":" + e.getMessage()))
                        .addOnCompleteListener(task -> {
                            Hey.print("a", "AAA");
                        });

            }
        }
    }

    private boolean notLoading() {
        return My.loading;
    }

    void changeAsNotLoading() {
        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private ArrayList<TextMessage> getMessages() {
        ArrayList<TextMessage> list = new ArrayList<>();
        TextMessage t1 = new TextMessage("Hello how are you?Hello how are you?Hello how are you?Hello how are you?Hello how are you?Hello how are you?", "19:15,25-may,2020-yil", My.uId), t2 = new TextMessage("Im fine thank you.And you?", "19:16,25-may,2020-yil", My.uId + "a");
        for (int i = 1; i <= 30; i++) {
            if (i % 2 == 0) list.add(t1);
            else list.add(t2);
        }
        return list;
    }
}