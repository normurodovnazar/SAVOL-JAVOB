package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

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
    boolean imageViewing = false, fromChat, canScale = false;
    Button gotoChat;
    DocumentReference userDocument;


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
        LoadingDialog d = Hey.showLoadingDialog(this);
        userDocument = FirebaseFirestore.getInstance().collection(Keys.users).document(id);
        Hey.getDocument(this, userDocument,
                doc -> {
                    d.closeDialog();
                    user = User.fromDoc((DocumentSnapshot) doc);
                    fullName.setText(user.getFullName());
                    if (user.getId()==My.id) gotoChat.setVisibility(View.INVISIBLE); else gotoChat.setVisibility(View.VISIBLE);
                    seen.setText(Hey.getSeenTime(this, user.getSeen()));
                    f = new File(user.getLocalFileName());
                    Hey.workWithProfileImage(user, doc1 -> {
                        profileImage.setImageURI(Uri.fromFile(f));
                        canScale = true;
                    }, errorMessage -> {
                    });
                    int i = user.getNumberOfMyAnswers() == 0 ? 0 : (int) (user.getNumberOfCorrectAnswers() * 10000 / user.getNumberOfMyAnswers());
                    float s = i / 100f;
                    ArrayList<String> a = new ArrayList<>();
                    a.add(getString(R.string.phone_number) + " " + (user.isNumberHidden() && !id.equals(String.valueOf(My.id)) ? getString(R.string.hidden) : user.getNumber()));
                    a.add(getString(R.string.units) + " " + user.getUnits());
                    a.add(getString(R.string.numberOfPublishedQ) + " " + user.getNumberOfMyPublishedQuestions());
                    a.add(getString(R.string.numberOfAnswers) + " " + user.getNumberOfMyAnswers());
                    a.add(getString(R.string.correctAnswers) + " " + user.getNumberOfCorrectAnswers());
                    a.add(getString(R.string.incorrectAnswers) + " " + user.getNumberOfIncorrectAnswers());
                    a.add(getString(R.string.status) + " " + s + " %");
                    if (id.equals(String.valueOf(My.id)))
                        a.add(getString(user.isNumberHidden() ? R.string.makeVisible : R.string.makeInvisible));
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, a);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        if (!user.isNumberHidden() && position == 0) {
                            Intent ph = new Intent(Intent.ACTION_DIAL);
                            ph.setData(Uri.parse("tel:" + user.getNumber()));
                            startActivity(ph);
                        }
                        if (position == 7) {
                            LoadingDialog dialog = Hey.showLoadingDialog(this);
                            Hey.updateDocument(this, userDocument, Collections.singletonMap(Keys.hidden, !user.isNumberHidden()), doc12 -> {
                                dialog.closeDialog();
                                Hey.showToast(this, getString(R.string.changed));
                                downloadData();
                            }, errorMessage -> dialog.closeDialog());
                        }
                    });
                    listView.setOnItemLongClickListener((parent, view, position, id) -> {
                        if (!user.isNumberHidden() && position == 0)
                            Hey.copyToClipboard(AccountInformation.this, user.getNumber());
                        return true;
                    });
                }, errorMessage -> finish());
    }
}