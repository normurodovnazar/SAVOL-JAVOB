package com.normurodov_nazar.savol_javob.Activities;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.gotoPrivateChat;

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
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
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
import java.util.List;
import java.util.Map;

public class QuestionChat extends AppCompatActivity {
    ImageButton back, menu, sendText, sendImage;
    TextView youBlocked;
    Button answer;
    ProgressBar bar;
    RecyclerView recyclerView;
    ConstraintLayout main;
    SubsamplingScaleImageView bigImage;

    EditText text;

    CollectionReference chats;
    ListenerRegistration registration;
    QuestionChatAdapter adapter;

    String questionId, theme;
    ArrayList<Message> oldMessages = new ArrayList<>(), messages = new ArrayList<>();
    ActivityResultLauncher<Intent> memoryLauncher, captureLauncher;
    Uri capture;

    Map<String, Object> d;
    Message message;

    boolean loading = false, imageShowing = false;
    long imageSentTime = 0, limit = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_chat);
        initVars();
        showLoading();
        downloadChatData();
    }

    private void downloadChatData() {
        chats.orderBy(Keys.time, Query.Direction.ASCENDING).limitToLast(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
            if (!list.isEmpty()) {
                limit = Message.fromDoc(list.get(0)).getTime();
                registration = Hey.addMessagesListener(QuestionChat.this, chats, limit, messages -> {
                    showView();
                    changeView(messages);
                }, errorMessage -> {
                });
            }
        }).addOnFailureListener(this::showErrorAndFinish);
    }

    private void changeView(ArrayList<Message> messages) {
        this.messages = messages;
        if (adapter == null) {
            showMessages(messages);
            oldMessages = messages;
            return;
        }
        if (Hey.isMessageAddedToTop(oldMessages, messages)) {
            adapter.addItemsToTop(Hey.getMessagesOnTop(oldMessages, messages));
        }
        if (Hey.isMessagesAddedToBottom(oldMessages, messages)) {
            adapter.addItems(Hey.getDifferenceOfMessages(messages, oldMessages), messages.size() - oldMessages.size());
            recyclerView.smoothScrollToPosition(messages.size());
            Hey.getAllUnreadMessagesForMeAndRead(messages, chats);
        }
        if (messages.size() == oldMessages.size()) {
            ArrayList<Integer> readChanges = Hey.getDifferenceOfReadUnreadMessages(oldMessages, messages), textChanges = Hey.getDifferenceBetweenMessageChanges(oldMessages, messages);
            for (int i : readChanges) adapter.changeItem(messages.get(i), i);
            for (int i : textChanges) adapter.changeItem(messages.get(i), i);
        }
        if (oldMessages.size() > messages.size()) {
            ArrayList<Message> x = Hey.getDeletedMessages(oldMessages, messages);
            for (Message m : x) adapter.removeItem(m);
        }
        oldMessages = messages;
    }

    private void showMessages(ArrayList<Message> messages) {
        oldMessages = messages;
        adapter = new QuestionChatAdapter(this, messages, questionId,
                (message, itemView, position) -> {
                    //TEXT CLICK
                    itemView.setBackgroundColor(getResources().getColor(R.color.black));
                    if (message.getSender() == My.id)
                        Hey.showPopupMenu(this, itemView, new ArrayList<>(Arrays.asList(getString(R.string.delete), getString(R.string.change), getString(R.string.copy))), (i, name) -> {
                            switch (i) {
                                case 0:
                                    if (message.getType().equals(Keys.question))
                                        Hey.showToast(this, getString(R.string.deleteQuestion));
                                    else {
                                        if (message.getType().equals(Keys.answer)) {
                                            MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(R.string.confirm_delete_message), message, true);
                                            d.setOnDismissListener(dialog -> {
                                                if (d.getResult()) {
                                                    Hey.amIOnline(new StatusListener() {
                                                        @Override
                                                        public void online() {
                                                            LoadingDialog loadingDialog = Hey.showLoadingDialog(QuestionChat.this);
                                                            Hey.showDeleteImageDialog(QuestionChat.this, message, doc -> Hey.deleteDocument(QuestionChat.this, chats.document(message.getId()), doc12 -> loadingDialog.closeDialog()), errorMessage -> loadingDialog.closeDialog());
                                                        }

                                                        @Override
                                                        public void offline() {
                                                            Hey.showToast(QuestionChat.this, getString(R.string.youAreOffline));
                                                        }
                                                    }, errorMessage -> {

                                                    }, this);
                                                }
                                            });
                                        } else {
                                            MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(R.string.confirm_delete_message), message, true);
                                            d.setOnDismissListener(dialog -> {
                                                if (d.getResult()) {
                                                    Hey.deleteDocument(this, chats.document(message.getId()), doc -> {
                                                    });
                                                }
                                            });
                                            d.show();
                                        }
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
                        }, true).setOnDismissListener(popupMenu -> itemView.setBackgroundColor(Color.TRANSPARENT));
                    else
                        Hey.showPopupMenu(this, itemView, new ArrayList<>(Arrays.asList(getString(R.string.copy), getString(R.string.id))), (position1, name) -> {
                            Hey.copyToClipboard(this, position1 == 0 ? message.getMessage() : message.getId());
                        }, true).setOnDismissListener(x -> itemView.setBackgroundColor(Color.TRANSPARENT));
                },
                (message, itemView, position) -> {
                    //IMAGE CLICK
                    Hey.workWithImageMessage(message, doc -> {
                        if (!imageShowing) {
                            imageShowing = true;
                            main.setVisibility(View.INVISIBLE);
                            bigImage.setVisibility(View.VISIBLE);
                            Hey.setBigImage(bigImage, Hey.getLocalFile(message));
                        }
                    }, NeedDownloadImage -> Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            Hey.showDownloadDialog(QuestionChat.this, message, doc -> adapter.changeItem(message, Hey.getIndexInArray(message, messages)), errorMessage1 -> {
                            });
                        }

                        @Override
                        public void offline() {
                            Hey.showToast(getApplicationContext(), getString(R.string.error_connection));
                        }
                    }, errorMessage -> {
                    }, this));
                },
                user -> {
                    //PROFILE IMAGE CLICK
                    if (Hey.amIBlocked()) Hey.showYouBlockedDialog(this);
                    else {
                        if (!user.isHiddenFromQuestionChat()) {
                            gotoPrivateChat(this,message.getSender());
                        } else Hey.showToast(this, getString(R.string.hiddenUser));
                    }
                },
                user -> {
                    //PROFILE IMAGE LONG CLICK
                    Intent info = new Intent(this, AccountInformation.class);
                    info.putExtra(Keys.id, String.valueOf(user.getId()));
                    info.putExtra(Keys.fromChat, false);
                    startActivity(info);
                },
                (message, itemView, position) -> {
                    //LONG CLICK IMAGE
                    if (message.getSender() == My.id) {
                        if (!message.getType().equals(Keys.answer))
                            Hey.showPopupMenu(this, itemView, new ArrayList<>(Collections.singletonList(getString(R.string.delete))), (i, name) -> {
                                MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(R.string.confirmDeleteImageM), message, false);
                                d.setOnDismissListener(dialog -> {
                                    if (d.getResult()) {
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

                                        }, this);
                                    }
                                });
                            }, true);
                    } else
                        Hey.showPopupMenu(this, itemView, new ArrayList<>(Collections.singletonList(getString(R.string.id))), (position12, name) -> Hey.copyToClipboard(this, message.getId()), true);
                },
                (position, name, button) -> {
                    //Load more CLICK
                    chats.orderBy(Keys.time, Query.Direction.DESCENDING).startAfter(limit).limit(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        Hey.setButtonAsDefault(this, button, getString(R.string.loadMore));
                        if (queryDocumentSnapshots.getDocuments().size() != 0) {
                            DocumentSnapshot ds = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            limit = Message.fromDoc(ds).getTime();
                            registration.remove();
                            registration = Hey.addMessagesListener(this, chats, limit, this::changeView, errorMessage -> {
                                Hey.showAlertDialog(QuestionChat.this, errorMessage);
                                Hey.setButtonAsDefault(QuestionChat.this, button, getString(R.string.loadMore));
                            });
                        } else {
                            Hey.showToast(this, getString(R.string.noMoreM));
                            button.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(this::showErrorAndFinish);
                }, this.theme);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager x = new LinearLayoutManager(this);
        x.setStackFromEnd(true);
        recyclerView.setLayoutManager(x);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(25);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        bar.setVisibility(View.GONE);
        Hey.getAllUnreadMessagesForMeAndRead(messages, chats);
    }

    private void initVars() {
        youBlocked = findViewById(R.id.youBlocked);
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
        if (questionId != null && theme != null) {
            My.activeId = questionId;
            text.setText(Hey.getPreferences(this).getString(questionId, ""));
            chats = FirebaseFirestore.getInstance().collection(Keys.chats).document(questionId).collection(Keys.chats);
        } else {
            Hey.showToast(this, getString(R.string.error_unknown));
            finish();
        }
        memoryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResultMemory
        );
        captureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResultCapture
        );
        if (Hey.amIBlocked()) {
            youBlocked.setVisibility(View.VISIBLE);
            youBlocked.setText(getString(R.string.youBlocked).replaceAll("xxx", Hey.getTimeText(this, My.blockTime)));
            sendImage.setVisibility(View.INVISIBLE);
            sendText.setVisibility(View.INVISIBLE);
            text.setVisibility(View.INVISIBLE);
        }
    }

    private void sendAnswer() {
        if (Hey.amIBlocked()) Hey.showYouBlockedDialog(this);
        else {
            Intent i = new Intent(this, AnswerToQuestion.class);
            i.putExtra(Keys.id, questionId);
            i.putExtra(Keys.question, questionId);
            i.putExtra(Keys.theme, theme);
            startActivity(i);
        }
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
                            Map<String, String> x = new HashMap<>();
                            x.put(Keys.type, Keys.needQuestions);
                            x.put(Keys.id, questionId);
                            x.put(Keys.theme, theme);
                            x.put(Keys.sender, String.valueOf(My.id));
                            Hey.sendNotification(QuestionChat.this, My.fullName, getString(R.string.newMInQ).replace("xxx", theme.replace(Keys.correct, "").replace(Keys.incorrect, "")) + t, questionId, x, doc1 -> {
                            }, errorMessage -> {
                            });
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
                    Hey.chooseImage(QuestionChat.this, sendImage, memoryLauncher, captureLauncher, uri -> capture = uri).setOnDismissListener(x -> stopLoading(sendImage));
                }

                @Override
                public void offline() {
                    stopLoading(sendImage);
                    Hey.showToast(getApplicationContext(), getString(R.string.error_connection));
                }
            }, errorMessage -> stopLoading(sendImage), this);
        } else Hey.showToast(this, getString(R.string.wait));
    }

    private void onResultMemory(ActivityResult result) {
        if (result.getData() != null) {
            startLoading(sendImage);
            Uri uri = result.getData().getData();
            imageSentTime = Hey.getCurrentTime();
            Hey.cropImage(this, this, uri, new File(My.folder + My.id + imageSentTime + ".png"), false, errorMessage -> {
            });
        } else stopLoading(sendImage);
    }

    private void onResultCapture(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            startLoading(sendImage);
            imageSentTime = Timestamp.now().toDate().getTime();
            d = new HashMap<>();
            d.put(Keys.type, Keys.imageMessage);
            d.put(Keys.time, imageSentTime);
            d.put(Keys.sender, My.id);
            d.put(Keys.read, false);
            message = new Message(d);
            Hey.cropImage(this, this, capture, new File(My.folder + message.getId() + ".png"), false, errorMessage -> stopLoading(sendImage));
        }
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
                    Hey.compressImage(this, file);
                    Hey.uploadImageToChat(this, file.getPath(), String.valueOf(My.id) + imageSentTime, doc -> {
                        Map<String, Object> image = new HashMap<>();
                        image.put(Keys.type, Keys.imageMessage);
                        image.put(Keys.sender, My.id);
                        image.put(Keys.time, imageSentTime);
                        image.put(Keys.read, false);
                        image.put(Keys.imageSize, file.length());
                        Hey.sendMessage(this, chats, new Message(image), doc1 -> {
                            if (a.isShowing()) a.closeDialog();
                            stopLoading(sendImage);
                            Map<String, String> x = new HashMap<>();
                            x.put(Keys.type, Keys.needQuestions);
                            x.put(Keys.id, questionId);
                            x.put(Keys.theme, theme);
                            x.put(Keys.sender, String.valueOf(My.id));
                            Hey.sendNotification(this, My.fullName, getString(R.string.newIInQ).replace("xxx", theme.replace(Keys.correct, "").replace(Keys.incorrect, "")), questionId, x, doc2 -> {
                            }, errorMessage -> {
                            });
                        }, errorMessage -> {
                            if (a.isShowing()) a.closeDialog();
                        });
                    }, (position, name) -> {
                        if (a.isShowing()) a.closeDialog();
                        stopLoading(sendImage);
                    }, errorMessage -> {
                        if (a.isShowing()) a.closeDialog();
                        stopLoading(sendImage);
                    });
                } else stopLoading(sendImage);
            } else {
                stopLoading(sendImage);
                if (data != null) {
                    Log.e("onActivityResult", "Result came with error");
                    Throwable throwable = UCrop.getError(data);
                    String mes;
                    if (throwable != null) mes = throwable.getMessage();
                    else mes = getString(R.string.unknown);
                    Hey.showAlertDialog(this, getString(R.string.error_unknown) + mes);
                }
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        My.activeId = Keys.id;
        Hey.getPreferences(this).edit().putString(questionId, text.getText().toString()).apply();
        if (registration != null) registration.remove();
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
        Hey.showPopupMenu(this, menu, new ArrayList<>(Arrays.asList(getString(R.string.to_top), getString(R.string.to_bottom), getString(R.string.copyQuestionId), getString(R.string.showQuestion), getString(R.string.enableQCH), getString(R.string.disableQCH))), (position, name) -> {
            switch (position) {
                case 0:
                    if (adapter != null)
                        if (adapter.getItemCount() != 0) recyclerView.smoothScrollToPosition(0);
                    break;
                case 1:
                    if (adapter != null) if (adapter.getItemCount() != 0)
                        recyclerView.smoothScrollToPosition(messages.size() - 1);
                    break;
                case 2:
                    if (questionId != null) Hey.copyToClipboard(this, questionId);
                    break;
                case 3:
                    startActivity(new Intent(this, ShowQuestion.class).putExtra(Keys.id, questionId));
                    break;
                case 4:
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            LoadingDialog d = Hey.showLoadingDialog(QuestionChat.this);
                            FirebaseMessaging.getInstance().subscribeToTopic(Keys.topics + questionId).addOnSuccessListener(unused -> d.closeDialog()).addOnFailureListener(e -> {
                                d.closeDialog();
                                Hey.showErrorMessage(QuestionChat.this, e.getLocalizedMessage(), false);
                            });
                        }

                        @Override
                        public void offline() {
                            Hey.showToast(QuestionChat.this, getString(R.string.error_connection));
                        }
                    }, errorMessage -> {
                    }, this);
                    break;
                case 5:
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            LoadingDialog x = Hey.showLoadingDialog(QuestionChat.this);
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(Keys.topics + questionId).addOnSuccessListener(unused -> x.closeDialog()).addOnFailureListener(e -> {
                                x.closeDialog();
                                Hey.showErrorMessage(QuestionChat.this, e.getLocalizedMessage(), false);
                            });
                        }

                        @Override
                        public void offline() {
                            Hey.showToast(QuestionChat.this, getString(R.string.error_connection));
                        }
                    }, errorMessage -> {
                    }, this);
                    break;
            }
        }, true);
    }

    @Override
    public void onBackPressed() {
        if (imageShowing) {
            imageShowing = false;
            bigImage.setVisibility(View.GONE);
            main.setVisibility(View.VISIBLE);
        } else super.onBackPressed();
    }

    private void showLoading() {
        bar.setVisibility(View.VISIBLE);
    }

    private void showView() {
        bar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    void showErrorAndFinish(Exception e) {
        Hey.showErrorMessage(this, e.getLocalizedMessage(), true);
    }
}