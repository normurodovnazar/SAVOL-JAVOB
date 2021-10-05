package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.RecyclerViewItemClickListener;
import com.normurodov_nazar.savol_javob.MyD.TextMessage;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SingleChat extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView name,seen,centerText;
    EditText editText;
    ImageView send, sendImage, profileImage, menu;
    String chatId;
    CollectionReference chats;
    ProgressBar progressBar, barForImageDownload;
    List<Map<String, Object>> messages = new ArrayList<>();
    User friend;
    boolean loading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);
        initVars();
        loadChatData();
    }

    private void loadChatData() {
        Intent i = getIntent();
        chatId = i.getStringExtra(Keys.chatId);
        if(chatId!=null) {
            chats = FirebaseFirestore.getInstance().collection(chatId);
            loadMessages();
            FirebaseFirestore.getInstance().collection(Keys.chats).document(chatId).addSnapshotListener((value, error) -> {
                if(value!=null){
                    loadFriendsData(FirebaseFirestore.getInstance().collection(Keys.users).document(Hey.getFriendsIdFromChatId(chatId)));
                }else showError();
            });
        } else {showError().setOnDismissListener(d->finish());}
    }

    private void loadFriendsData(DocumentReference reference) {
        reference.addSnapshotListener((d, error) -> {
            if(d!=null){
                friend = new User(d.get(Keys.name),d.get(Keys.surname),d.get(Keys.imageUrl),d.get(Keys.seen),d.get(Keys.number),d.get(Keys.id),
                        d.get(Keys.numberOfMyPublishedQuestions),d.get(Keys.numberOfMyAnswers),d.get(Keys.numberOfCorrectAnswers),
                        d.get(Keys.numberOfIncorrectAnswers),d.get(Keys.chats),d.get(Keys.myQuestionOpportunity));
                setAllFriendsData();
            }else if (error != null) Hey.showAlertDialog(getApplicationContext(),getString(R.string.error)+":"+ error.getLocalizedMessage());else showError();

        });
    }

    private void setAllFriendsData() {
        name.setText(friend.fullName);
        seen.setText(Hey.getSeenTime(this,friend.getSeen()));
        setImage();
    }

    private void setImage() {
        File f = new File(getExternalFilesDir("images").toString()+File.separatorChar+friend.getId()+friend.getImageUrl().substring(friend.getImageUrl().length()-5));
        if(f.exists()){
            Hey.print("a","exists");
            profileImage.setImageURI(Uri.parse(f.getPath()));
            barForImageDownload.setVisibility(View.INVISIBLE);
            profileImage.setVisibility(View.VISIBLE);
        }else {
            FirebaseStorage.getInstance().getReference().child(Keys.users).child(String.valueOf(friend.getId())).getFile(f)
                    .addOnFailureListener(e -> Hey.showAlertDialog(getApplicationContext(),getString(R.string.error_download_file)+":"+e.getLocalizedMessage()))
                    .addOnSuccessListener(taskSnapshot -> {
                        setImage();
                        Hey.print("a","completed");
                    })
                    .addOnProgressListener(snapshot -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            barForImageDownload.setProgress(Hey.getPercentage(snapshot),true);
                        }else barForImageDownload.setProgress(Hey.getPercentage(snapshot));
                    });
        }
    }

    private void loadMessages() {
        chats.addSnapshotListener((value, error) -> {
                if (value != null) {
                    List<DocumentSnapshot> docs = value.getDocuments();
                    for (DocumentSnapshot d : docs) {
                        messages.add(d.getData());
                    }
                    if (messages.size() != 0) {
                        List<TextMessage> t = new ArrayList<>();
                        for (Map<String, Object> m : messages) {
                            t.add(new TextMessage(Objects.requireNonNull(m.get(Keys.message)).toString(), Long.parseLong(m.get(Keys.time).toString()), Long.parseLong(m.get(Keys.sender).toString())));
                        }
                        MessageAdapterInSingleChat adapter = new MessageAdapterInSingleChat(t, this, (message, itemView) -> {
                            PopupMenu menu = new PopupMenu(getApplicationContext(),itemView);
                            menu.inflate(R.menu.message_popup);
                            menu.setOnMenuItemClickListener(item ->{
                                switch (item.getItemId()){
                                    case R.id.deleteMessage:
                                        Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.editMessage:
                                        Toast.makeText(this, "edit", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.copyMessage:
                                        Toast.makeText(this, "copy", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                return true;
                            });
                            menu.show();
                        });
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        changeAsNotLoading();
                    } else showNoMessages();
                } else {
                    if (error != null) Hey.showAlertDialog(this, getString(R.string.error) + ":" + error.getMessage());showError();
                }
        });
    }

    private void initVars() {
        centerText = findViewById(R.id.center_text);
        recyclerView = findViewById(R.id.chatsInSingleChat);
        send = findViewById(R.id.send);send.setOnClickListener(x->sendTextMessage());
        seen = findViewById(R.id.seenSingleChat);
        sendImage = findViewById(R.id.sendImage);
        profileImage = findViewById(R.id.profileImageInSingleChat);
        menu = findViewById(R.id.menuInSingleChat);
        name = findViewById(R.id.nameAndSurnameInSingleChat);
        editText = findViewById(R.id.editTextInSingleChat);
        progressBar = findViewById(R.id.progressBarInSingleChat);
        barForImageDownload = findViewById(R.id.barForImageDownload);

    }

    private void sendTextMessage() {
        Hey.setIconButtonAsLoading(this,send,loading);
        if(chats!=null && !editText.getText().toString().replaceAll(" ","").isEmpty()){
            String m = editText.getText().toString();
            TextMessage text = new TextMessage(m, Timestamp.now().toDate().getTime(),My.id);
            chats.add(text.toMap()).addOnFailureListener(e -> Hey.showAlertDialog(getApplicationContext(),getString(R.string.error_sending_message)+e.getLocalizedMessage())).addOnSuccessListener(reference -> {
                Hey.setIconButtonAsDefault(getApplicationContext(),send,loading);
                Hey.print("a","sent");
            });
        }
    }

    private void showNoMessages() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        centerText.setVisibility(View.VISIBLE);
    }

    void changeAsNotLoading() {
        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        centerText.setVisibility(View.INVISIBLE);
    }



    private MyDialog showError(){
        return Hey.showAlertDialog(this,getString(R.string.error_unknown));
    }
}