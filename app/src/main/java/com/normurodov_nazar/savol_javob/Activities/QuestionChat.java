package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.EditMode;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.QuestionChatAdapter;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QuestionChat extends AppCompatActivity {
    ImageButton back, menu, sendText, sendImage;
    Button answer;
    ProgressBar bar;
    RecyclerView recyclerView;
    ConstraintLayout main;
    SubsamplingScaleImageView bigImage;

    EditText text;

    CollectionReference chats;
    ListenerRegistration registration;
    QuestionChatAdapter adapter;

    String questionId,theme;
    ArrayList<Message> oldMessages = new ArrayList<>(), messages = new ArrayList<>();
    ActivityResultLauncher<Intent> launcher;

    boolean loading = false,imageShowing = false;
    long imageSentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_chat);
        initVars();
        downloadChatData();
    }

    private void downloadChatData() {
        registration = Hey.addMessagesListener(this, chats, messages -> {
            this.messages = messages;
            Hey.print("X","new");
            if (adapter == null) showMessages(messages); else {
                if (oldMessages.size()<messages.size()) {
                    ArrayList<Message> newMessages = Hey.getDifferenceOfArrays(messages,oldMessages);
                    adapter.addItems(newMessages, messages.size()- oldMessages.size());
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
                if (messages.size()==oldMessages.size()){
                    ArrayList<Integer> readChanges = Hey.getDifferenceOfReadUnreadMessages(oldMessages,messages),textChanges = Hey.getDifferenceBetweenMessageChanges(oldMessages,messages);
                    for (int i : readChanges) adapter.changeItem(messages.get(i),i);
                    for (int i : textChanges) adapter.changeItem(messages.get(i),i);
                }
                if (oldMessages.size()>messages.size()){
                    ArrayList<Message> x = Hey.getDeletedMessages(oldMessages,messages);
                    for (Message m : x) adapter.removeItem(m);
                }
                oldMessages = messages;
                Hey.getAllUnreadMessagesForMeAndRead(messages,chats);
            }
        }, errorMessage -> {

        });
    }

    private void showMessages(ArrayList<Message> messages) {
        oldMessages = messages;
        adapter = new QuestionChatAdapter(this, messages, questionId, (message, itemView, position) -> {
            //TEXT CLICK
            Hey.showPopupMenu(this, itemView, new ArrayList<>(Arrays.asList(getString(R.string.delete), getString(R.string.change), getString(R.string.copy))), (i, name) -> {
                switch (i) {
                    case 0:
                        if (message.getType().equals(Keys.question))
                            Hey.showToast(this, getString(R.string.deleteQuestion));
                        else {
                            MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(R.string.confirm_delete_message), message, true);
                            d.setOnDismissListener(dialog -> {
                                if (d.getResult()) {
                                    Hey.deleteDocument(this, chats.document(message.getId()), doc -> { });
                                }
                            });
                            d.show();
                        }
                        break;
                    case 1:
                        Hey.editMessage(this, message.toMap(), chats.document(message.getId()), EditMode.message, doc -> {

                        });
                        break;
                    case 2:
                        Hey.copyToClipboard(this, message.getMessage());
                        break;
                }
            }, true);
        }, (message, itemView, position) -> {
            //IMAGE CLICK
            Hey.workWithImageMessage(message, doc -> {
                if (!imageShowing){
                    imageShowing = true;
                    main.setVisibility(View.INVISIBLE);
                    bigImage.setVisibility(View.VISIBLE);
                    bigImage.setImage(ImageSource.uri(Uri.fromFile(new File(Hey.getLocalFile(message)))));
                    bigImage.setBackgroundColor(Color.BLACK);
                    bigImage.setMaxScale(15);
                    bigImage.setMinScale(0.1f);
                }
            }, NeedDownloadImage -> Hey.amIOnline(new StatusListener() {
                @Override
                public void online() {
                    Hey.showDownloadDialog(QuestionChat.this, message, doc -> adapter.changeItem(message,position), errorMessage1 -> { });
                }
                @Override
                public void offline() {
                    Hey.showToast(getApplicationContext(),getString(R.string.error_connection));
                }
            }, errorMessage -> {

            },this));
        }, (message, itemView, position) -> {
            //PROFILE IMAGE CLICK
            Intent s = new Intent(this,SingleChat.class);
            s.putExtra(Keys.chatId,Hey.getChatIdFromIds(My.id,message.getSender()));
            startActivity(s);
            Hey.addToChats(this,My.id,message.getSender());
        }, (message, itemView, position) -> {
            //LONG CLICK IMAGE
            Hey.showPopupMenu(this, itemView, new ArrayList<>(Collections.singletonList(getString(R.string.delete))), (i, name) -> {
                MyDialogWithTwoButtons d = Hey.showDeleteDialog(this,getString(R.string.confirmdeleteImagem),message,false);
                d.setOnDismissListener(dialog -> {
                    if (d.getResult()){
                        Hey.amIOnline(new StatusListener() {
                            @Override
                            public void online() {
                                Hey.showDeleteImageDialog(QuestionChat.this, message, doc -> Hey.deleteDocument(QuestionChat.this, chats.document(message.getId()), doc1 -> {

                                }), errorMessage -> {

                                });
                            }
                            @Override
                            public void offline() {
                                Hey.showAlertDialog(QuestionChat.this, getString(R.string.youAreOffline));
                            }
                        }, errorMessage -> {

                        },this);
                    }
                });
            },true);
            Hey.showToast(this, "LONG CLICK IMAGE");
        }, this.theme);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.smoothScrollToPosition(messages.size()-1);
        bar.setVisibility(View.GONE);
    }

    private void initVars() {
        main = findViewById(R.id.mainQ);
        bigImage = findViewById(R.id.bigImageQ);
        text = findViewById(R.id.textInQuestion);
        back = findViewById(R.id.backQuestionChat);
        back.setOnClickListener(v -> finish());
        menu = findViewById(R.id.menuInQuestionChat);
        menu.setOnClickListener(v -> showMenu());
        sendText = findViewById(R.id.sendQuestion);
        sendText.setOnClickListener(v -> sendTextMessage());
        sendImage = findViewById(R.id.imageQuestion);
        sendImage.setOnClickListener(v -> sendImageMessage());
        answer = findViewById(R.id.answerToQuestion);
        answer.setOnClickListener(v -> sendAnswer());
        bar = findViewById(R.id.progressBarQuestion);
        recyclerView = findViewById(R.id.recyclerQuestion);
        questionId = getIntent().getStringExtra(Keys.id);
        theme = getIntent().getStringExtra(Keys.theme);
        if (questionId != null && theme!=null)
            chats = FirebaseFirestore.getInstance().collection(Keys.chats).document(questionId).collection(Keys.chats);
        else {
            Hey.showToast(this, getString(R.string.error_unknown));
            finish();
        }
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
    }

    private void sendAnswer() {
        Intent i = new Intent(this, AnswerToQuestion.class);
        i.putExtra(Keys.id, questionId);
        startActivity(i);
    }

    private void sendTextMessage() {
        String t = text.getText().toString();
        if (!loading) {
            if (!t.isEmpty() && !t.replaceAll(" ", "").isEmpty()) {
                startLoading(sendText);
                Hey.amIOnline(new StatusListener() {
                    @Override
                    public void online() {
                        Map<String, Object> data = new HashMap<>();
                        data.put(Keys.message, t);
                        data.put(Keys.time, Hey.getCurrentTime());
                        data.put(Keys.type, Keys.textMessage);
                        data.put(Keys.sender, My.id);
                        data.put(Keys.read, false);
                        Message message = new Message(data);
                        Hey.sendMessage(getApplicationContext(), chats, message, doc -> {
                            stopLoading(sendText);
                            text.setText("");
                        }, errorMessage -> stopLoading(sendText));
                    }

                    @Override
                    public void offline() {
                        stopLoading(sendText);
                        Hey.showToast(getApplicationContext(), getString(R.string.error_connection));
                    }
                }, errorMessage -> {

                }, this);
            }
        } else Hey.showToast(this, getString(R.string.wait));
    }

    private void sendImageMessage() {
        if (!loading) {
            startLoading(sendImage);
            Hey.amIOnline(new StatusListener() {
                @Override
                public void online() {
                    Hey.pickImage(launcher);
                }

                @Override
                public void offline() {
                    stopLoading(sendImage);
                    Hey.showToast(getApplicationContext(), getString(R.string.error_connection));
                }
            }, errorMessage -> stopLoading(sendImage), this);
        } else Hey.showToast(this, getString(R.string.wait));
    }

    private void onResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri uri = result.getData().getData();
            imageSentTime = Hey.getCurrentTime();
            Hey.cropImage(this, this, uri, new File(My.folder + My.id + imageSentTime + ".png"), false, errorMessage -> { });
        }else stopLoading(sendImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP)
            if (data != null && resultCode == RESULT_OK) {
                Uri res = UCrop.getOutput(data);
                if (res != null) {
                    LoadingDialog a = Hey.showLoadingDialog(this);
                    File file = new File(res.getPath());
                    Hey.uploadImageToChat(this, file.getPath(), String.valueOf(My.id)+imageSentTime, doc -> {
                        Hey.print("a", "uploaded");
                        Map<String, Object> image = new HashMap<>();
                        image.put(Keys.type, Keys.imageMessage);
                        image.put(Keys.sender, My.id);
                        image.put(Keys.time, imageSentTime);
                        image.put(Keys.read, false);
                        image.put(Keys.imageSize,file.length());
                        Hey.sendMessage(this, chats, new Message(image), doc1 -> {
                            if (a.isShowing()) a.dismiss();
                            stopLoading(sendImage);
                        }, errorMessage -> {
                            if (a.isShowing()) a.dismiss();
                        });
                    }, (position, name) -> {
                        if (a.isShowing()) a.dismiss();
                        stopLoading(sendImage);
                    }, errorMessage -> {
                        if (a.isShowing()) a.dismiss();
                        stopLoading(sendImage);
                    });
                }else stopLoading(sendImage);
            } else if (data != null) {
                stopLoading(sendImage);
                Log.e("onActivityResult", "Result came with error");
                Throwable throwable = UCrop.getError(data);
                String mes;
                if (throwable != null) mes = throwable.getMessage();
                else mes = getString(R.string.unknown);
                Hey.showAlertDialog(this, getString(R.string.error_unknown) + mes);
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    void startLoading(View v) {
        Hey.setIconButtonAsLoading(v);
        loading = true;
    }

    void stopLoading(View v) {
        Hey.setIconButtonAsDefault(v);
        loading = false;
    }

    private void showMenu() {
        Hey.showPopupMenu(this, menu, new ArrayList<>(Arrays.asList(getString(R.string.to_top), getString(R.string.to_bottom))), (position, name) -> {
            switch (position) {
                case 0:
                    recyclerView.smoothScrollToPosition(0);
                    break;
                case 1:
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                    break;
            }
        }, true);
    }

    @Override
    public void onBackPressed() {
        if (imageShowing){
            imageShowing = false;
            bigImage.setVisibility(View.GONE);
            main.setVisibility(View.VISIBLE);
        }else super.onBackPressed();
    }
}