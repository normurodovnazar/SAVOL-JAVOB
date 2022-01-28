package com.normurodov_nazar.savol_javob.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;
import java.util.ArrayList;

public class AccountInformation extends AppCompatActivity {
    ListView listView;
    ImageView profileImage;
    TextView fullName, seen;
    Intent i;
    String id;
    User user;
    ConstraintLayout main, imageSide;
    File f;
    SubsamplingScaleImageView bigImage;
    boolean imageViewing = false, fromChat,canScale = false;
    Button gotoChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accaunt_information);
        initVars();
    }

    private void initVars() {
        i = getIntent();
        id = i.getStringExtra(Keys.id);
        fromChat = i.getBooleanExtra(Keys.fromChat, false);
        gotoChat = findViewById(R.id.gotoChatButton);
        gotoChat.setOnClickListener(v -> {
            if (fromChat) finish();
            else {
                Intent singleChat = new Intent(this, SingleChat.class);
                singleChat.putExtra(Keys.chatId, Hey.getChatIdFromIds(My.id, user.getId()));
                startActivity(singleChat);
            }
        });
        bigImage = findViewById(R.id.bigImage);
        main = findViewById(R.id.mainInfo);
        imageSide = findViewById(R.id.imageSideInfo);

        listView = findViewById(R.id.infoList);
        profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(view -> onTapImage());
        fullName = findViewById(R.id.fullName);
        seen = findViewById(R.id.seenTime);

        if (id == null)
            Hey.showAlertDialog(this, getString(R.string.error) + ":" + getString(R.string.unknown)).setOnDismissListener(dialogInterface -> finish());
        else downloadData();
    }

    private void onTapImage() {
        Hey.print("a", "A");
        if (canScale) {
            if (!imageViewing) {
                main.setVisibility(View.INVISIBLE);
                imageSide.setVisibility(View.VISIBLE);
                bigImage.setImage(ImageSource.uri(Uri.fromFile(f)));
                bigImage.setBackgroundColor(Color.BLACK);
                bigImage.setMaxScale(15);
                bigImage.setMinScale(0.1f);
            }
            imageViewing = !imageViewing;
        }
    }

    @Override
    public void onBackPressed() {
        if (imageViewing) {
            main.setVisibility(View.VISIBLE);
            imageSide.setVisibility(View.GONE);
            imageViewing = false;
        } else super.onBackPressed();
    }

    private void downloadData() {
        Hey.getDocument(this, FirebaseFirestore.getInstance().collection(Keys.users).document(id),
                doc -> {
                    Hey.print("user", doc.toString());
                    user = User.fromDoc((DocumentSnapshot) doc);
                    fullName.setText(user.getFullName());
                    seen.setText(Hey.getSeenTime(this, user.getSeen()));
                    f = new File(user.getLocalFileName());
                    Hey.workWithProfileImage(user, doc1 -> {
                        profileImage.setImageURI(Uri.fromFile(f));
                        canScale = true;
                    }, errorMessage -> { });
                    int i = user.getNumberOfMyAnswers() == 0 ? 0 : (int) (user.getNumberOfCorrectAnswers() / user.getNumberOfMyAnswers() * 10000);
                    float s = i / 100f;
                    ArrayList<String> a = new ArrayList<>();
                    a.add(getString(R.string.phone_number) + " " + user.getNumber());
                    a.add(getString(R.string.units) + " " + user.getUnits());
                    a.add(getString(R.string.numberOfPublishedQ) + " " + user.getNumberOfMyPublishedQuestions());
                    a.add(getString(R.string.numberOfAnswers) + " " + user.getNumberOfMyAnswers());
                    a.add(getString(R.string.correctAnswers) + " " + user.getNumberOfCorrectAnswers());
                    a.add(getString(R.string.incorrectAnswers) + " " + user.getNumberOfIncorrectAnswers());
                    a.add(getString(R.string.status) + " " + s + " %");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, a);
                    listView.setAdapter(adapter);
                    listView.setOnItemLongClickListener((parent, view, position, id) -> {
                        if (position == 0) {
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clipData = new ClipData(new ClipDescription("a", new String[0]), new ClipData.Item(user.getNumber()));
                            clipboardManager.setPrimaryClip(clipData);
                            Hey.showToast(this, getString(R.string.copied));
                        }
                        return true;
                    });
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        if (position == 0) {
                            Intent ph = new Intent(Intent.ACTION_DIAL);
                            ph.setData(Uri.parse("tel:" + user.getNumber()));
                            startActivity(ph);
                        }
                    });
                    listView.setOnItemLongClickListener((parent, view, position, id) -> {
                        if (position==0) Hey.copyToClipboard(AccountInformation.this,user.getNumber());
                        return true;
                    });
                }, errorMessage -> finish());
    }
}