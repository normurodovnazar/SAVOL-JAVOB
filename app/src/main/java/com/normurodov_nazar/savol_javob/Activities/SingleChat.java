package com.normurodov_nazar.savol_javob.Activities;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.addToLastChat;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.animateHide;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.animateShow;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.chooseImage;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.getFriendsIdFromChatId;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.getUserReference;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.print;
import static com.normurodov_nazar.savol_javob.MFunctions.Keys.lastChat;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.EditMode;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.MessageAdapterInSingleChat;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.databinding.ActivitySingleChatBinding;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SingleChat extends AppCompatActivity {

    String chatId;
    CollectionReference chats;
    DocumentReference extraData;
    User friend;
    boolean loading = false, imageIsViewing = false, fromPrivateChat, friendBlocked = false, imBlocked = false;
    ListenerRegistration registration;
    MessageAdapterInSingleChat adapter = null;
    ActivityResultLauncher<Intent> memoryImageLauncher, captureLauncher;
    Uri capture;
    long i, limit = 0;
    Message message;
    Map<String, Object> d;
    ArrayList<Message> oldMessages = new ArrayList<>();
    private ActivitySingleChatBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySingleChatBinding.inflate(getLayoutInflater());
        View v = b.getRoot();
        setContentView(v);
        memoryImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResultMemory
        );
        captureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResultCapture
        );
        initVars();
        downloadChatData();
    }

    private void downloadChatData() {
        Intent i = getIntent();
        chatId = i.getStringExtra(Keys.chatId);
        fromPrivateChat = i.getBooleanExtra(Keys.privateChat, true);
        prepareChat();
    }

    private void prepareChat() {
        if (chatId != null) {
            My.activeId = chatId;
            b.textField.setText(Hey.getPreferences(this).getString(chatId, ""));
            extraData = FirebaseFirestore.getInstance().collection(Keys.chats).document(chatId);
            Hey.addDocumentListener(this, extraData, doc -> {
                Boolean bo = doc.getBoolean(Keys.blockTime + My.id), ib = doc.getBoolean(Keys.blockTime + Hey.getFriendsIdFromChatId(chatId));
                friendBlocked = bo != null && bo;
                imBlocked = ib != null && ib;
                if (imBlocked) {
                    b.youBlockedBy.setVisibility(View.VISIBLE);
                    b.sendImage.setVisibility(View.INVISIBLE);
                    b.textField.setVisibility(View.INVISIBLE);
                    b.send.setVisibility(View.INVISIBLE);
                } else {
                    b.youBlockedBy.setVisibility(View.INVISIBLE);
                    b.sendImage.setVisibility(View.VISIBLE);
                    b.textField.setVisibility(View.VISIBLE);
                    b.send.setVisibility(View.VISIBLE);
                }
            }, errorMessage -> {
            });
            chats = extraData.collection(chatId);
            loadMessages();
            loadFriendsData(getUserReference(getFriendsIdFromChatId(chatId)));
        } else {
            showError().setOnDismissListener(d -> finish());
        }
    }

    private void loadMessages() {
        chats.orderBy(Keys.time, Query.Direction.ASCENDING).limitToLast(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                Message m = Message.fromDoc(queryDocumentSnapshots.getDocuments().get(0));
                limit = m.getTime();
            }
            registration = Hey.addMessagesListener(this, chats, limit, this::changeView, errorMessage -> {
            });
        }).addOnFailureListener(e -> Hey.showAlertDialog(this, getString(R.string.error) + ":" + e.getLocalizedMessage()));
    }

    private void changeView(ArrayList<Message> messages) {
        if (messages.size() == 0) showNoMessages();
        else {
            if (adapter == null) {
                showMessages(messages);
                changeAsNotLoading();
                return;
            }
            if (Hey.isMessageAddedToTop(oldMessages, messages)) {
                adapter.addItemsToTop(Hey.getMessagesOnTop(oldMessages, messages));
            }
            if (Hey.isMessagesAddedToBottom(oldMessages, messages)) {
                adapter.addItems(Hey.getDifferenceOfMessages(messages, oldMessages), messages.size() - oldMessages.size());
                b.chatsRecycler.smoothScrollToPosition(messages.size());
                Hey.getAllUnreadMessagesForMeAndRead(messages, chats);
            }
            if (oldMessages.size() == messages.size()) {
                ArrayList<Integer> positionsOfNewRead = Hey.getDifferenceOfReadUnreadMessages(oldMessages, messages), positionsOfChanged = Hey.getDifferenceBetweenMessageChanges(oldMessages, messages);
                for (int i : positionsOfNewRead) adapter.changeItem(i, messages.get(i));
                for (int i : positionsOfChanged) adapter.changeItem(i, messages.get(i));
            }
            if (oldMessages.size() > messages.size()) {
                ArrayList<Message> x = Hey.getDeletedMessages(oldMessages, messages);
                for (Message message : x) adapter.removeItem(message);
            }
            oldMessages = messages;
            changeAsNotLoading();
        }
    }

    private void loadFriendsData(DocumentReference reference) {
        Hey.getDocument(this, reference, doc -> {
            DocumentSnapshot d = (DocumentSnapshot) doc;
            try {
                friend = User.fromDoc(d);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Hey.showUnknownError(this).setOnDismissListener(x -> finish());
            }
            setAllFriendsData();
        }, errorMessage -> {

        });
    }

    void onResultMemory(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK)
            if (result.getData() != null) {
                startLoading(b.sendImage);
                Uri uri = result.getData().getData();
                i = Timestamp.now().toDate().getTime();
                d = new HashMap<>();
                d.put(Keys.type, Keys.imageMessage);
                d.put(Keys.time, i);
                d.put(Keys.sender, My.id);
                d.put(Keys.read, false);
                message = new Message(d);
                Hey.cropImage(this, this, uri, new File(My.folder + message.getId() + ".png"), false, errorMessage -> stopLoading(b.sendImage));
            } else stopLoading(b.sendImage);
    }

    void onResultCapture(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            startLoading(b.sendImage);
            i = Timestamp.now().toDate().getTime();
            d = new HashMap<>();
            d.put(Keys.type, Keys.imageMessage);
            d.put(Keys.time, i);
            d.put(Keys.sender, My.id);
            d.put(Keys.read, false);
            message = new Message(d);
            Hey.cropImage(this, this, capture, new File(My.folder + message.getId() + ".png"), false, errorMessage -> stopLoading(b.sendImage));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_VOLUME_UP) addToLastChat(this, friend);
        if (keyCode==KeyEvent.KEYCODE_VOLUME_DOWN) Hey.getPreferences(this).edit().putString(lastChat,"sdf").apply();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP)
            if (data != null && resultCode == RESULT_OK) {
                Uri res = UCrop.getOutput(data);
                if (res != null) {
                    File f = new File(res.getPath());
                    Hey.compressImage(this, f);
                    d.put(Keys.imageSize, f.length());
                    message = new Message(d);
                    Hey.uploadImageToChat(this, f.getPath(), message.getId(), doc -> Hey.addDocumentToCollection(getApplicationContext(), chats, message.getId(), message.toMap(),
                            doc1 -> {
                                addToLastChat(this, friend);
                                Map<String, String> d = new HashMap<>();
                                d.put(Keys.type, Keys.privateChat);
                                d.put(Keys.id, String.valueOf(My.id));
                                Hey.sendNotification(this, My.fullName, getString(R.string.imageMessage), friend.getToken(), d, doc2 -> {

                                }, errorMessage -> {

                                });
                                addToExtra();
                                stopLoading(b.sendImage);
                            },
                            errorMessage -> stopLoading(b.sendImage)), (position, name) -> stopLoading(b.sendImage), errorMessage -> {
                    });
                } else stopLoading(b.sendImage);
            } else stopLoading(b.sendImage);
    }

    private void showMessages(ArrayList<Message> messages) {
        oldMessages = messages;
        adapter = new MessageAdapterInSingleChat(messages, this, chats, (message, itemView, i) -> {
            print("a",message.toMap().toString());
            if (message.getSender() == My.id) {
                if (message.getType().equals(Keys.textMessage) || message.getType().equals(Keys.reply)) {
                    itemView.setBackgroundColor(getResources().getColor(R.color.black));
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
                                });
                                break;
                            case 2:
                                Hey.copyToClipboard(this, message.getMessage());
                                break;
                        }
                    }, true).setOnDismissListener(m -> itemView.setBackgroundColor(Color.TRANSPARENT));
                }

            } else {
                if (message.getType().equals(Keys.textMessage)) {
                    itemView.setBackgroundColor(getResources().getColor(R.color.black));
                    Hey.showPopupMenu(this, itemView, new ArrayList<>(Collections.singletonList(getString(R.string.copy))), (position, name) -> Hey.copyToClipboard(this, message.getMessage()), true)
                            .setOnDismissListener(x -> itemView.setBackgroundColor(Color.TRANSPARENT));
                }
            }
            if (message.getType().equals(Keys.imageMessage)) {
                File f = Hey.getLocalFile(message);
                Hey.workWithImageMessage(message, doc -> {
                    Hey.setBigImage(b.bigImage, f);
                    animateHide(b.main,s->{
                        b.main.setVisibility(View.GONE);
                        b.bigImage.setVisibility(View.VISIBLE);
                        imageIsViewing = true;
                    });
                }, errorMessage -> Hey.amIOnline(new StatusListener() {
                    @Override
                    public void online() {
                        Hey.showDownloadDialog(SingleChat.this, message, doc -> adapter.changeItem(Hey.getIndexInArray(message, messages), message), errorMessage -> {
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
                MyDialogWithTwoButtons dialog = Hey.showDeleteDialog(this, getString(R.string.confirmDeleteImageM), message, true);
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
        }, (position, name, button) -> chats.orderBy(Keys.time, Query.Direction.DESCENDING).startAfter(limit).limit(50).get().addOnSuccessListener(queryDocumentSnapshots -> {
            Hey.setButtonAsDefault(this, button, getString(R.string.loadMore));
            if (queryDocumentSnapshots.getDocuments().size() != 0) {
                DocumentSnapshot ds = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                limit = Message.fromDoc(ds).getTime();
                registration.remove();
                registration = Hey.addMessagesListener(this, chats, limit, this::changeView, errorMessage -> {
                    Hey.showAlertDialog(SingleChat.this, errorMessage);
                    Hey.setButtonAsDefault(SingleChat.this, button, getString(R.string.loadMore));
                });
            } else {
                Hey.showToast(this, getString(R.string.noMoreM));
                button.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            Hey.showAlertDialog(SingleChat.this, getString(R.string.error) + ":" + e.getLocalizedMessage());
            Hey.setButtonAsDefault(this, button, getString(R.string.loadMore));
        }), (message, itemView, position) -> {
            //REPLY LISTENER
            Hey.showReplyDialog(this, message, chats);
        }, doc -> {
            int position = Hey.getIndexInArray(new Message((String) doc), oldMessages);
            print("m", oldMessages.get(position).toMap().toString());
            b.chatsRecycler.smoothScrollToPosition(position);
            //print("s",recyclerView.findViewHolderForLayoutPosition(position).toString());
        });
        b.chatsRecycler.setAdapter(adapter);
        LinearLayoutManager x = new LinearLayoutManager(this);
        x.setStackFromEnd(true);
        b.chatsRecycler.setLayoutManager(x);
        b.chatsRecycler.setHasFixedSize(true);
        b.chatsRecycler.setItemViewCacheSize(25);
        b.chatsRecycler.setDrawingCacheEnabled(true);
        b.chatsRecycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Hey.getAllUnreadMessagesForMeAndRead(messages, chats);
    }

    @Override
    public void onBackPressed() {
        if (imageIsViewing) {
            animateHide(b.bigImage, doc -> {
                b.bigImage.setVisibility(View.GONE);
                b.main.setVisibility(View.VISIBLE);
                imageIsViewing = false;
            });
        } else if (friend != null) {
            if (!fromPrivateChat) {
                MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(R.string.confirmAddToChats).replaceAll("xxx", friend.getName()), null, false);
                d.setOnDismissListener(x -> {
                    if (d.getResult()) {
                        Hey.addToChats(this, My.id, friend.getId());
                    }
                    super.onBackPressed();
                });
            } else super.onBackPressed();
        } else super.onBackPressed();
    }

    private void sendTextMessage() {
        String m = b.textField.getText().toString();
        if (!loading && chats != null && !m.replaceAll(" ", "").isEmpty()) {
            startLoading(b.send);
            Map<String, Object> data = new HashMap<>();
            data.put(Keys.time, Hey.getCurrentTime());
            data.put(Keys.message, m);
            data.put(Keys.sender, My.id);
            data.put(Keys.type, Keys.textMessage);
            data.put(Keys.read, false);
            Message message = new Message(data);
            Hey.sendMessage(this, chats, message, doc -> {
                addToLastChat(this, friend);
                b.textField.setText("");
                stopLoading(b.send);
                addToExtra();
                Map<String, String> d = new HashMap<>();
                d.put(Keys.type, Keys.privateChat);
                d.put(Keys.id, String.valueOf(My.id));
                Hey.sendNotification(this, My.fullName, message.getMessage(), friend.getToken(), d, doc1 -> {

                }, errorMessage -> {

                });
            }, errorMessage -> stopLoading(b.send));
        }
    }

    private void addToExtra() {
        Hey.getDocument(this, extraData, eData -> {
            DocumentSnapshot snapshot = (DocumentSnapshot) eData;
            String fId = Keys.newMessagesTo + friend.getId();
            Map<String, Object> up = new HashMap<>();
            if (snapshot.contains(fId)) {
                Object uO = snapshot.get(fId);
                long unreadNumber = uO == null ? 0 : (long) uO;
                up.put(fId, unreadNumber + 1);
                extraData.update(up);
            } else {
                up.put(fId, 1);
                extraData.set(up, SetOptions.merge());
            }
        }, errorMessage -> {

        });
    }

    private void setAllFriendsData() {
        b.topItem.setOnClickListener(v->gotoInfo());
        b.name.setText(friend.fullName);
        b.seen.setText(Hey.getTimeText(this, friend.getSeen()));
        if (friend.hasProfileImage()) {
            print(friend.getFullName(), "Has profile image");
            setImage();
        } else print(friend.getFullName(), "Hasn't profile image");
    }

    private void setImage() {
        File f = friend.getLocalFile();
        Hey.workWithProfileImage(friend, doc -> showImage(f), errorMessage -> {
            b.profileImage.setVisibility(View.VISIBLE);
            Hey.downloadFile(this, Keys.users, String.valueOf(friend.getId()), f, (progress, total) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    b.imageProgress.setProgress(Hey.getPercentage(progress, total), true);
                } else b.imageProgress.setProgress(Hey.getPercentage(progress, total));
            }, doc -> showImage(f), e -> {

            });
        });
    }

    private void showImage(File f) {
        b.profileImage.setImageURI(Uri.parse(f.getPath()));
        b.imageProgress.setVisibility(View.INVISIBLE);
        b.profileImage.setVisibility(View.VISIBLE);
    }

    private void showNoMessages() {
        b.chatsRecycler.setVisibility(View.INVISIBLE);
        b.centerProgress.setVisibility(View.INVISIBLE);
        b.centerText.setVisibility(View.VISIBLE);
    }

    void changeAsNotLoading() {
        b.centerProgress.setVisibility(View.INVISIBLE);
        b.chatsRecycler.setVisibility(View.VISIBLE);
        b.centerText.setVisibility(View.INVISIBLE);
    }

    private void initVars() {
        b.send.setOnClickListener(x -> {
            if (!loading) sendTextMessage();
            else Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_SHORT).show();
        });
        b.sendImage.setOnClickListener(v -> {
            if (!loading) sendImage();
            else Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_SHORT).show();
        });
        b.menu.setOnClickListener(view -> Hey.showPopupMenu(this, b.menu, new ArrayList<>(Arrays.asList(getString(R.string.to_top), getString(R.string.to_bottom), getString(friendBlocked ? R.string.unblock : R.string.block))), (position, name) -> {
            switch (position) {
                case 0:
                    if (adapter != null)
                        if (adapter.getItemCount() != 0) b.chatsRecycler.smoothScrollToPosition(0);
                    break;
                case 1:
                    if (adapter != null) if (adapter.getItemCount() != 0)
                        b.chatsRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                    break;
                case 2:
                    extraData.set(Collections.singletonMap(Keys.blockTime + My.id, !friendBlocked), SetOptions.merge())
                            .addOnFailureListener(v -> Hey.showErrorMessage(this, v.getLocalizedMessage(), false));
                    break;
            }
        }, true));
        String draft = getPreferences(MODE_PRIVATE).getString(Keys.message, "");
        if (!draft.isEmpty()) b.textField.setText(draft);
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
        My.activeId = Keys.id;
        My.shortcutCompleted = false;
        Hey.getPreferences(this).edit().putString(chatId, b.textField.getText().toString()).apply();
        markAsRead();
        if (registration != null) registration.remove();
    }

    private void markAsRead() {
        Hey.getDocument(this, extraData, doc -> {
            DocumentSnapshot snapshot = (DocumentSnapshot) doc;
            String key = Keys.newMessagesTo + My.id;
            Map<String, Object> d = new HashMap<>();
            d.put(key, 0);
            if (snapshot.contains(key)) {
                extraData.update(d);
            } else {
                extraData.set(d, SetOptions.merge());
            }
        }, errorMessage -> {

        });
    }

    private void sendImage() {
        startLoading(b.sendImage);
        Hey.amIOnline(new StatusListener() {
            @Override
            public void online() {
                chooseImage(SingleChat.this, b.sendImage, memoryImageLauncher, captureLauncher, uri -> capture = uri).setOnDismissListener(x -> stopLoading(b.sendImage));
            }

            @Override
            public void offline() {
                stopLoading(b.sendImage);
                Hey.showToast(SingleChat.this, getString(R.string.error_connection));
            }
        }, errorMessage -> stopLoading(b.sendImage), this);
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