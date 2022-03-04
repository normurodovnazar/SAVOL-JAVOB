package com.normurodov_nazar.adminapp.Activities;

import android.content.Intent;
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

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.normurodov_nazar.adminapp.MFunctions.Hey;
import com.normurodov_nazar.adminapp.MFunctions.Keys;
import com.normurodov_nazar.adminapp.MFunctions.My;
import com.normurodov_nazar.adminapp.MyD.LoadingDialog;
import com.normurodov_nazar.adminapp.MyD.User;
import com.normurodov_nazar.adminapp.R;

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
    boolean imageViewing = false, fromChat, canScale = false;
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
        if (user != null)
            if (user.hasProfileImage())
                if (canScale) {
                    if (!imageViewing) {
                        main.setVisibility(View.INVISIBLE);
                        imageSide.setVisibility(View.VISIBLE);
                        Hey.setBigImage(bigImage,f);
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
        Hey.getUserFromUserId(this, id, doc -> {
            d.closeDialog();
            user = (User) doc;
            fullName.setText(user.isHiddenFromQuestionChat() ? getString(R.string.hidden) : user.getFullName());
            seen.setText(user.isHiddenFromQuestionChat() ? getString(R.string.hidden) : Hey.getTimeText(this, user.getSeen()));
            if (user.getId() == My.id) {
                gotoChat.setVisibility(View.INVISIBLE);
            } else {
                gotoChat.setVisibility(user.isHiddenFromQuestionChat() ? View.INVISIBLE : View.VISIBLE);
            }
            f = new File(user.getLocalFileName());
            if (user.hasProfileImage()){
                Hey.print(user.getFullName(),"Has profile image");
                Hey.workWithProfileImage(user, doc1 -> {
                    profileImage.setImageURI(Uri.fromFile(f));
                    canScale = true;
                }, errorMessage -> {
                });
            }else Hey.print(user.getFullName(),"Hasn't profile image");
            int i = user.getNumberOfMyAnswers() == 0 ? 0 : (int) (user.getNumberOfCorrectAnswers() * 10000 / user.getNumberOfMyAnswers());
            float s = i / 100f;
            ArrayList<String> a = new ArrayList<>();
            a.add(getString(R.string.phone_number) + " " + (user.isNumberHidden() ? getString(R.string.hidden) : user.getNumber()));
            a.add(getString(R.string.units) + " " + user.getUnits());
            a.add(getString(R.string.numberOfPublishedQ) + " " + user.getNumberOfMyPublishedQuestions());
            a.add(getString(R.string.numberOfAnswers) + " " + user.getNumberOfMyAnswers());
            a.add(getString(R.string.correctAnswers) + " " + user.getNumberOfCorrectAnswers());
            a.add(getString(R.string.incorrectAnswers) + " " + user.getNumberOfIncorrectAnswers());
            a.add(getString(R.string.status) + " " + s + " %");
            a.add(getString(R.string.id)+" "+user.getId());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, a);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (!user.isNumberHidden() && position == 0) {
                    Intent ph = new Intent(Intent.ACTION_DIAL);
                    ph.setData(Uri.parse("tel:" + user.getNumber()));
                    startActivity(ph);
                }
            });
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                if (!user.isNumberHidden() && position == 0) Hey.copyToClipboard(AccountInformation.this, user.getNumber());
                if (position==7) Hey.copyToClipboard(this, String.valueOf(user.getId()));
                return true;
            });
        }, errorMessage -> {

        });
    }
}