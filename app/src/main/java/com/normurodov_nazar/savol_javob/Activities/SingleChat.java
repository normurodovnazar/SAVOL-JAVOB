package com.normurodov_nazar.savol_javob.Activities;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
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
import com.google.firebase.storage.FirebaseStorage;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.EditMessageDialog;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.ImageDownloadingDialog;
import com.normurodov_nazar.savol_javob.MyD.ItemClickListener;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.MessageAdapterInSingleChat;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.RecyclerViewItemLongClickListener;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SingleChat extends AppCompatActivity {

    ConstraintLayout main,image;
    RecyclerView recyclerView;
    TextView name, seen, centerText;
    EditText editText;
    ImageView send, sendImage, profileImage, menu;
    String chatId;
    CollectionReference chats;
    ProgressBar progressBar, barForImageDownload;
    User friend;
    boolean loading = false,imageIsViewing = false;
    ListenerRegistration registration;
    MessageAdapterInSingleChat adapter = null;
    ActivityResultLauncher<Intent> imagePickLauncher;
    SubsamplingScaleImageView imageItem;
    long i;Message message;
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

    void onResult(ActivityResult result){
        if (result.getData()!=null){
            Uri uri = result.getData().getData();
            cropImage(uri);
        }else stopLoading(sendImage);

    }

    private void cropImage(Uri uri){
        i = Timestamp.now().toDate().getTime();
        Map<String,Object> d = new HashMap<>();
        d.put(Keys.type,Keys.imageMessage);
        d.put(Keys.time,i);
        d.put(Keys.sender,My.id);
        message = new Message(d);
        Hey.cropImage(this, this, uri, new File(My.folder + message.getId() + ".png"), false, errorMessage -> {

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP) if (data != null && resultCode == RESULT_OK){
                Uri res = UCrop.getOutput(data);
                if(res!=null)
                    Hey.uploadImageToChat(this, res.getPath(), message.getId(), doc -> Hey.addDocumentToCollection(getApplicationContext(), chats, message.getId(), message.toMap(), doc1 -> stopLoading(sendImage), errorMessage -> stopLoading(sendImage)),
                            (position, name) -> stopLoading(sendImage));
                else stopLoading(sendImage);
        }else stopLoading(sendImage);
    }

    private void stopLoading(ImageView icon) {
        loading=false;
        Hey.setIconButtonAsDefault(icon);
    }
    private void startLoading(ImageView icon) {
        loading=true;
        Hey.setIconButtonAsLoading(icon);
    }

    private void loadMessages() {
        registration = Hey.addMessagesListener(this, chats, messages -> {
            if(messages.size()==0) showNoMessages(); else{
                if (adapter==null){
                    showMessages(messages);
                }else {
                    if (Hey.isSimilar(messages,oldMessages)){
                        Hey.print("oldMessages=", String.valueOf(oldMessages.size()));
                        Hey.print("messages=", String.valueOf(messages.size()));
                        if (oldMessages.size()<messages.size()){
                            Hey.print("a","item inserted");
                            if (oldMessages.size()+1==messages.size()) adapter.addItem(Hey.biggerListsLastElement(oldMessages,messages));
                            else showMessages(messages);
                            recyclerView.smoothScrollToPosition(messages.size()-1);
                            oldMessages=messages;
                        }else if (oldMessages.size()>messages.size()){
                            Hey.print("a","item removed");
                        }
                    }
                }
                editText.setText("");
                changeAsNotLoading();
            }
        }, errorMessage -> {

        });
    }



    private void showMessages(ArrayList<Message> messages) {
        Hey.print("A","showMessages");
        oldMessages=messages;
        adapter = new MessageAdapterInSingleChat(messages, this, (message, itemView) -> {
            PopupMenu menu = new PopupMenu(getApplicationContext(), itemView);
            if (message.getSender()==My.id) if (message.getType().equals(Keys.textMessage)){
                menu.inflate(R.menu.message_popup);
                menu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.deleteMessage:
                            MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(R.string.confirm_delete_message), message);
                            d.setOnDismissListener(a -> {
                                if (d.getResult())
                                    Hey.deleteDocument(this, chats.document(message.getId()), doc -> {
                                        oldMessages = messages;
                                        adapter.removeItem(message);
                                        Hey.print("a", "remove called");
                                    });
                            });
                            break;
                        case R.id.editMessage:
                            Hey.editMessage(this, message, chats, newMessage -> adapter.changeItem((Message) newMessage));
                            break;
                        case R.id.copyMessage:
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clipData = new ClipData(new ClipDescription("a", new String[0]), new ClipData.Item(message.getMessage()));
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(this, getText(R.string.copied), Toast.LENGTH_SHORT).show();
                            break;
                    }
                    return true;
                });
                menu.setOnDismissListener(menu1 -> itemView.setBackgroundColor(Color.TRANSPARENT));
                itemView.setBackgroundColor(Color.BLACK);
                menu.show();
            }
            if (message.getType().equals(Keys.imageMessage)){
                File f = new File(Hey.getLocalFile(message));
                if (f.exists()){
                    imageItem.setImage(ImageSource.uri(Uri.fromFile(f)));
                    imageItem.setBackgroundColor(Color.BLACK);
                    imageItem.setMaxScale(15);
                    imageItem.setMinScale(0.1f);
                    main.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    imageIsViewing=true;
                }else {
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            Hey.showDownloadDialog(SingleChat.this, message, doc -> {
                                adapter.changeItem(message);
                            }, errorMessage -> {

                            });
                        }

                        @Override
                        public void offline() {
                            Hey.showToast(SingleChat.this,getString(R.string.error_connection));
                        }
                    }, errorMessage -> {

                    },this);
                }
            }
        }, (message,itemView, position) -> {
            PopupMenu menu = new PopupMenu(getApplicationContext(), itemView);
            menu.inflate(R.menu.delete_item);
            menu.setOnMenuItemClickListener(item -> {
                MyDialogWithTwoButtons dialog = Hey.showDeleteDialog(this,getString(R.string.confirmdeleteImagem),message);
                dialog.setOnDismissListener(dialog1 -> {
                    if (dialog.getResult()){
                        Hey.amIOnline(new StatusListener() {
                            @Override
                            public void online() {
                                Hey.showDeleteImageDialog(SingleChat.this, message, errorMessage -> {
                                        },
                                        doc -> Hey.deleteDocument(SingleChat.this, chats.document(message.getId()), doc1 -> {
                                            oldMessages = messages;
                                            adapter.removeItem(message);
                                            Hey.print("a", "remove called");
                                        }));
                            }

                            @Override
                            public void offline() {
                                Hey.showAlertDialog(SingleChat.this,getString(R.string.youAreOffline));
                            }
                        }, errorMessage -> {

                        },this);
                    }
                });
                dialog.show();
                return true;
            });
            menu.setOnDismissListener(menu12 -> itemView.setBackgroundColor(Color.TRANSPARENT));
            menu.show();
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
        }else super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP) {
            Hey.showLoadingDialog(this);
            return true;
        }else
        return super.onKeyDown(keyCode, event);
    }

    private void sendTextMessage() {
        if (!loading && chats != null && !editText.getText().toString().replaceAll(" ", "").isEmpty()) {
            startLoading(send);
            String m = editText.getText().toString();
            long l = Timestamp.now().toDate().getTime();
            Map<String, Object> data = new HashMap<>();
            data.put(Keys.time, l);
            data.put(Keys.message, m);
            data.put(Keys.sender, My.id);
            data.put(Keys.type,Keys.textMessage);
            Message text = new Message(data);
            Hey.sendMessage(this, chats, text, doc -> stopLoading(send), errorMessage -> stopLoading(send));
        }
    }

    private void loadFriendsData(DocumentReference reference) {
        reference.addSnapshotListener((d, error) -> {
            if (d != null) {
                friend = new User(d.get(Keys.name), d.get(Keys.surname), d.get(Keys.imageUrl), d.get(Keys.seen), d.get(Keys.number), d.get(Keys.id),
                        d.get(Keys.numberOfMyPublishedQuestions), d.get(Keys.numberOfMyAnswers), d.get(Keys.numberOfCorrectAnswers),
                        d.get(Keys.numberOfIncorrectAnswers), d.get(Keys.myQuestionOpportunity));
                setAllFriendsData();
            } else if (error != null)
                Hey.showAlertDialog(getApplicationContext(), getString(R.string.error) + ":" + error.getLocalizedMessage());
            else showError();

        });
    }

    private void setAllFriendsData() {
        name.setText(friend.fullName);
        seen.setText(Hey.getSeenTime(this, friend.getSeen()));
        setImage();
    }

    private void setImage() {
        File f = new File(My.folder + friend.getLocalFileName());
        if (f.exists()) {
            Hey.print("a", "exists");
            profileImage.setImageURI(Uri.parse(f.getPath()));
            barForImageDownload.setVisibility(View.INVISIBLE);
            profileImage.setVisibility(View.VISIBLE);
        } else {
            barForImageDownload.setVisibility(View.VISIBLE);
            Hey.downloadFile(this, Keys.users, String.valueOf(friend.getId()), f, (progress, total) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    barForImageDownload.setProgress(Hey.getPercentage(progress, total), true);
                } else barForImageDownload.setProgress(Hey.getPercentage(progress, total));
            }, doc -> {
                setImage();
                Hey.print("a", "completed");
            }, errorMessage -> {

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

    private void initVars() {
        main = findViewById(R.id.main);
        image = findViewById(R.id.imageSide);
        imageItem = findViewById(R.id.imageItem);
        centerText = findViewById(R.id.center_text);
        recyclerView = findViewById(R.id.chatsInSingleChat);
        send = findViewById(R.id.send);
        send.setOnClickListener(x -> {
            if (!loading) sendTextMessage(); else Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_SHORT).show();
        });
        seen = findViewById(R.id.seenSingleChat);
        sendImage = findViewById(R.id.sendImageQuestion);sendImage.setOnClickListener(v -> {
            if (!loading) sendImage(); else Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_SHORT).show();
        });
        profileImage = findViewById(R.id.profileImageInSingleChat);
        PopupMenu p = new PopupMenu(this, menu = findViewById(R.id.menuInSingleChat));
        menu.setOnClickListener(view -> p.show());
        p.inflate(R.menu.single_chat_menu);
        p.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.toBottom:
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    break;
                case R.id.toTop:
                    recyclerView.smoothScrollToPosition(0);
                    break;
                default:
                    return false;
            }
            return true;
        });
        name = findViewById(R.id.nameAndSurnameInSingleChat);
        editText = findViewById(R.id.editTextInSingleChat);
        progressBar = findViewById(R.id.progressBarInSingleChat);
        barForImageDownload = findViewById(R.id.barForImageDownload);
    }

    private void sendImage() {
        startLoading(sendImage);
        pickImage();
    }

    private void pickImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        imagePickLauncher.launch(Intent.createChooser(i, getString(R.string.choose_image)));
    }

    private void loadChatData() {
        Intent i = getIntent();
        chatId = i.getStringExtra(Keys.chatId);
        if (chatId != null) {
            chats = FirebaseFirestore.getInstance().collection(Keys.chats).document(chatId).collection(chatId);
            loadMessages();
            FirebaseFirestore.getInstance().collection(Keys.chats).document(chatId).addSnapshotListener((value, error) -> {
                if (value != null) {
                    loadFriendsData(FirebaseFirestore.getInstance().collection(Keys.users).document(Hey.getFriendsIdFromChatId(chatId)));
                } else showError();
            });
        } else {
            showError().setOnDismissListener(d -> finish());
        }
    }

    private MyDialog showError() {
        return Hey.showAlertDialog(this, getString(R.string.error_unknown));
    }
}