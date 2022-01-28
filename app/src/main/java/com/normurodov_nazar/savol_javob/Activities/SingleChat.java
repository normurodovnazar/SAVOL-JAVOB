package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.EditMode;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.MessageAdapterInSingleChat;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SingleChat extends AppCompatActivity {

    ConstraintLayout main, image;
    RecyclerView recyclerView;
    TextView name, seen, centerText;
    EditText editText;
    ImageView send, sendImage, profileImage, menu;
    String chatId;
    CollectionReference chats;
    DocumentReference extraData;
    ProgressBar progressBar, barForImageDownload;
    User friend;
    boolean loading = false, imageIsViewing = false;
    ListenerRegistration registration;
    MessageAdapterInSingleChat adapter = null;
    ActivityResultLauncher<Intent> imagePickLauncher;
    SubsamplingScaleImageView imageItem;
    long i;
    Message message;
    Map<String, Object> d;
    ArrayList<Message> oldMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);
        imagePickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
        initVars();
        loadChatData();
    }

    private void loadChatData() {
        Intent i = getIntent();
        chatId = i.getStringExtra(Keys.chatId);
        if (chatId != null) {
            extraData = FirebaseFirestore.getInstance().collection(Keys.chats).document(chatId);
            chats = extraData.collection(chatId);
            loadMessages();
            loadFriendsData(FirebaseFirestore.getInstance().collection(Keys.users).document(Hey.getFriendsIdFromChatId(chatId)));
        } else {
            showError().setOnDismissListener(d -> finish());
        }
    }

    void onResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri uri = result.getData().getData();
            i = Timestamp.now().toDate().getTime();
            d = new HashMap<>();
            d.put(Keys.type, Keys.imageMessage);
            d.put(Keys.time, i);
            d.put(Keys.sender, My.id);
            d.put(Keys.read, false);
            message = new Message(d);
            Hey.cropImage(this, this, uri, new File(My.folder + message.getId() + ".png"), false, errorMessage -> stopLoading(sendImage));
        } else stopLoading(sendImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP)
            if (data != null && resultCode == RESULT_OK) {
                Uri res = UCrop.getOutput(data);
                if (res != null)
                    Hey.uploadImageToChat(this, res.getPath(), message.getId(), doc -> {
                        File f = new File(res.getPath());
                                d.put(Keys.imageSize,f.length());
                                message = new Message(d);
                                Hey.addDocumentToCollection(getApplicationContext(), chats, message.getId(), message.toMap(),
                                        doc1 -> {
                                            addToExtra();
                                            stopLoading(sendImage);
                                        },
                                        errorMessage -> stopLoading(sendImage));
                            },
                            (position, name) -> stopLoading(sendImage),
                            errorMessage -> {

                            });
                else stopLoading(sendImage);
            } else stopLoading(sendImage);
    }

    private void loadMessages() {
        registration = Hey.addMessagesListener(this, chats, messages -> {
            if (messages.size() == 0) showNoMessages();
            else {
                if (adapter == null) {
                    showMessages(messages);
                }
                if (oldMessages.size() < messages.size()) {
                    Hey.print("a", "item inserted");
                    adapter.addItems(Hey.getDifferenceOfArrays(messages, oldMessages), messages.size() - oldMessages.size());
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                    oldMessages = messages;
                }
                if (oldMessages.size() == messages.size()) {
                    ArrayList<Integer> positionsOfNewRead = Hey.getDifferenceOfReadUnreadMessages(oldMessages, messages), positionsOfChanged = Hey.getDifferenceBetweenMessageChanges(oldMessages, messages);
                    for (int i : positionsOfNewRead) adapter.changeItem(i, messages.get(i));
                    for (int i : positionsOfChanged) adapter.changeItem(i, messages.get(i));
                }
                if (oldMessages.size() > messages.size()) {
                    ArrayList<Message> x = Hey.getDeletedMessages(oldMessages, messages);
                    for (Message m : x) adapter.removeItem(m);

                }
                oldMessages = messages;
                changeAsNotLoading();
                Hey.getAllUnreadMessagesForMeAndRead(messages, chats);
            }
        }, errorMessage -> {

        });
    }

    private void showMessages(ArrayList<Message> messages) {
        Hey.print("A", "showMessages");
        oldMessages = messages;
        adapter = new MessageAdapterInSingleChat(messages, this, (message, itemView, i) -> {
            if (message.getSender() == My.id) if (message.getType().equals(Keys.textMessage)) {
                Hey.showPopupMenu(this, itemView, new ArrayList<>(Arrays.asList(getString(R.string.delete), getString(R.string.change), getString(R.string.copy))), (position, name) -> {
                    switch (position) {
                        case 0:
                            MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(R.string.confirm_delete_message), message, true);
                            d.setOnDismissListener(a -> {
                                if (d.getResult())
                                    Hey.deleteDocument(this, chats.document(message.getId()), doc -> {

                                    });
                            });
                            break;
                        case 1:
                            Hey.editMessage(this, message.toMap(), chats.document(message.getId()), EditMode.message, doc -> {
                                Map<String, Object> x = (Map<String, Object>) doc;
                                message.setMessage((String) x.get(Keys.message));
                                adapter.changeItem(messages.indexOf(message), message);
                            });
                            break;
                        case 2:
                            Hey.copyToClipboard(this, message.getMessage());
                            break;
                    }
                }, true).setOnDismissListener(m -> itemView.setBackgroundColor(Color.TRANSPARENT));
                itemView.setBackgroundColor(Color.BLACK);
            }
            if (message.getType().equals(Keys.imageMessage)) {
                File f = new File(Hey.getLocalFile(message));
                Hey.workWithImageMessage(message, doc -> {
                    imageItem.setImage(ImageSource.uri(Uri.fromFile(f)));
                    imageItem.setBackgroundColor(Color.BLACK);
                    imageItem.setMaxScale(15);
                    imageItem.setMinScale(0.1f);
                    main.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    imageIsViewing = true;
                }, errorMessage -> Hey.amIOnline(new StatusListener() {
                    @Override
                    public void online() {
                        Hey.showDownloadDialog(SingleChat.this, message, doc -> adapter.changeItem(messages.indexOf(message), message), errorMessage -> {

                        });
                    }

                    @Override
                    public void offline() {
                        Hey.showToast(SingleChat.this, getString(R.string.error_connection));
                    }
                }, e -> {

                }, this));
            }
        }, (message, itemView, position) -> {
            Hey.showPopupMenu(this, itemView, new ArrayList<>(Collections.singletonList(getString(R.string.delete))), (position1, name) -> {
                MyDialogWithTwoButtons dialog = Hey.showDeleteDialog(this, getString(R.string.confirmdeleteImagem), message, true);
                dialog.setOnDismissListener(dialog1 -> {
                    if (dialog.getResult()) Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            Hey.showDeleteImageDialog(SingleChat.this, message,
                                    doc -> Hey.deleteDocument(SingleChat.this, chats.document(message.getId()), doc1 -> {
                                    }), errorMessage -> {
                                    });
                        }

                        @Override
                        public void offline() {
                            Hey.showAlertDialog(SingleChat.this, getString(R.string.youAreOffline));
                        }
                    }, errorMessage -> {

                    }, this);
                });
                dialog.show();
            }, true)
                    .setOnDismissListener(s -> itemView.setBackgroundColor(Color.TRANSPARENT));
            itemView.setBackgroundColor(Color.BLACK);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(SingleChat.this));
        recyclerView.smoothScrollToPosition(messages.size() - 1);
    }

    @Override
    public void onBackPressed() {
        if (imageIsViewing) {
            image.setVisibility(View.GONE);
            main.setVisibility(View.VISIBLE);
            imageIsViewing = false;
        } else super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private void sendTextMessage() {
        String m = editText.getText().toString();
        if (!loading && chats != null && !m.replaceAll(" ", "").isEmpty()) {
            startLoading(send);
            Map<String, Object> data = new HashMap<>();
            data.put(Keys.time, Hey.getCurrentTime());
            data.put(Keys.message, m);
            data.put(Keys.sender, My.id);
            data.put(Keys.type, Keys.textMessage);
            data.put(Keys.read, false);
            Message message = new Message(data);
            Hey.sendMessage(this, chats, message, doc -> {
                editText.setText("");
                stopLoading(send);
                addToExtra();
            }, errorMessage -> stopLoading(send));
        }
    }

    private void addToExtra() {
        Hey.getDocument(this, extraData, eData -> {
            DocumentSnapshot snapshot = (DocumentSnapshot) eData;
            String fId = Keys.newMessagesTo + friend.getId();
            Map<String, Object> up = new HashMap<>();
            Hey.print("snapshot", snapshot.toString());
            if (snapshot.contains(fId)) {
                Object uO = snapshot.get(fId);
                long unreadNumber = uO == null ? 0 : (long) uO;
                up.put(fId, unreadNumber + 1);
                extraData.update(up);
                Hey.print("sendMessage", "snapshotContainsAndAdded");
            } else {
                up.put(fId, 1);
                extraData.set(up, SetOptions.merge());
                Hey.print("sendMessage", "snapshotIsNotContainsAndCreated");
            }
        }, errorMessage -> {

        });
    }

    private void loadFriendsData(DocumentReference reference) {
        Hey.getDocument(this, reference, doc -> {
            DocumentSnapshot d = (DocumentSnapshot) doc;
            friend = User.fromDoc(d);
            setAllFriendsData();
        }, errorMessage -> {

        });
    }

    private void setAllFriendsData() {
        name.setText(friend.fullName);
        seen.setText(Hey.getSeenTime(this, friend.getSeen()));
        setImage();
    }

    private void setImage() {
        File f = new File(friend.getLocalFileName());
        Hey.workWithProfileImage(friend, doc -> showImage(f), errorMessage -> {
            barForImageDownload.setVisibility(View.VISIBLE);
            Hey.downloadFile(this, Keys.users, String.valueOf(friend.getId()), f, (progress, total) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    barForImageDownload.setProgress(Hey.getPercentage(progress, total), true);
                } else barForImageDownload.setProgress(Hey.getPercentage(progress, total));
            }, doc -> {
                showImage(f);
                Hey.print("a", "completed");
            }, e -> {

            });
        });
    }

    private void showImage(File f) {
        profileImage.setImageURI(Uri.parse(f.getPath()));
        barForImageDownload.setVisibility(View.INVISIBLE);
        profileImage.setVisibility(View.VISIBLE);
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

    private void initVars() {
        main = findViewById(R.id.main);
        image = findViewById(R.id.imageSide);
        imageItem = findViewById(R.id.imageItem);
        centerText = findViewById(R.id.center_text);
        recyclerView = findViewById(R.id.chatsInSingleChat);
        send = findViewById(R.id.send);
        send.setOnClickListener(x -> {
            if (!loading) sendTextMessage();
            else Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_SHORT).show();
        });
        sendImage = findViewById(R.id.sendImageQuestion);
        sendImage.setOnClickListener(v -> {
            if (!loading) sendImage();
            else Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_SHORT).show();
        });

        PopupMenu p = Hey.showPopupMenu(this, menu = findViewById(R.id.menuInSingleChat), new ArrayList<>(Arrays.asList(getString(R.string.to_bottom), getString(R.string.to_top))), (position, name) -> {
            switch (position) {
                case 0:
                    if (adapter.getItemCount() != 0)
                        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    break;
                case 1:
                    recyclerView.smoothScrollToPosition(0);
                    break;
            }
        }, false);
        menu.setOnClickListener(view -> p.show());
        name = findViewById(R.id.nameAndSurnameInSingleChat);
        name.setOnClickListener(view -> gotoInfo());
        seen = findViewById(R.id.seenSingleChat);
        seen.setOnClickListener(view -> gotoInfo());
        profileImage = findViewById(R.id.profileImageInSingleChat);
        profileImage.setOnClickListener(view -> gotoInfo());
        editText = findViewById(R.id.textInSingle);
        progressBar = findViewById(R.id.progressBarInSingleChat);
        barForImageDownload = findViewById(R.id.barForImageDownload);
        String draft = getPreferences(MODE_PRIVATE).getString(Keys.message, "");
        if (!draft.isEmpty()) editText.setText(draft);
    }

    private void gotoInfo() {
        Intent info = new Intent(this, AccountInformation.class);
        info.putExtra(Keys.id, Hey.getFriendsIdFromChatId(chatId));
        info.putExtra(Keys.fromChat, true);
        startActivity(info);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registration.remove();
        markAsRead();
        getPreferences(MODE_PRIVATE).edit().putString(chatId, editText.getText().toString()).apply();
    }

    private void markAsRead() {
        Hey.getDocument(this, extraData, doc -> {
            DocumentSnapshot snapshot = (DocumentSnapshot) doc;
            String key = Keys.newMessagesTo + My.id;
            Map<String, Object> d = new HashMap<>();
            d.put(key, 0);
            if (snapshot.contains(key)) {
                Hey.print("markAsRead", "is contains");
                extraData.update(d);
            } else {
                extraData.set(d, SetOptions.merge());
                Hey.print("markAsRead", "is not contains");
            }
        }, errorMessage -> {

        });
    }

    private void sendImage() {
        startLoading(sendImage);
        Hey.amIOnline(new StatusListener() {
            @Override
            public void online() {
                Hey.pickImage(imagePickLauncher);
            }

            @Override
            public void offline() {
                stopLoading(sendImage);
                Hey.showToast(SingleChat.this, getString(R.string.error_connection));
            }
        }, errorMessage -> stopLoading(sendImage), this);
    }

    private MyDialog showError() {
        return Hey.showAlertDialog(this, getString(R.string.error_unknown));
    }

    private void stopLoading(ImageView icon) {
        loading = false;
        Hey.setIconButtonAsDefault(icon);
    }

    private void startLoading(ImageView icon) {
        loading = true;
        Hey.setIconButtonAsLoading(icon);
    }
}