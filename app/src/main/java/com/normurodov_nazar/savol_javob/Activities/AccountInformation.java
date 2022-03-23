package com.normurodov_nazar.savol_javob.Activities;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.gotoPrivateChat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.databinding.ActivityAccauntInformationBinding;

import java.io.File;
import java.util.ArrayList;

public class AccountInformation extends AppCompatActivity {
    Intent i;
    String id;
    User user;
    File f;
    boolean imageViewing = false, fromChat, canScale = false;

    private ActivityAccauntInformationBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccauntInformationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        initVars();
    }

    private void initVars() {
        binding.bigImage.setOnClickListener(v-> onTapImage());
        i = getIntent();
        id = i.getStringExtra(Keys.id);
        fromChat = i.getBooleanExtra(Keys.fromChat, false);
        binding.gotoChat.setOnClickListener(v -> {
            if (fromChat) finish();
            else {
                gotoPrivateChat(this,user.getId());
            }
        });
        if (id == null)
            Hey.showAlertDialog(this, getString(R.string.error) + ":" + getString(R.string.unknown)).setOnDismissListener(dialogInterface -> finish());
        else downloadData();
    }

    private void onTapImage() {
        if (user != null)
            if (user.hasProfileImage())
                if (canScale) {
                    if (!imageViewing) {
                        binding.main.setVisibility(View.INVISIBLE);
                        binding.bigImage.setVisibility(View.VISIBLE);
                        Hey.setBigImage(binding.bigImage,f);
                    }
                    imageViewing = !imageViewing;
                }
    }

    @Override
    public void onBackPressed() {
        if (imageViewing) {
            binding.main.setVisibility(View.VISIBLE);
            binding.bigImage.setVisibility(View.GONE);
            imageViewing = false;
        } else super.onBackPressed();
    }

    private void downloadData() {
        LoadingDialog d = Hey.showLoadingDialog(this);
        Hey.getUserFromUserId(this, id, doc -> {
            d.closeDialog();
            user = (User) doc;
            binding.fullName.setText(user.isHiddenFromQuestionChat() ? getString(R.string.hidden) : user.getFullName());
            binding.seen.setText(user.isHiddenFromQuestionChat() ? getString(R.string.hidden) : Hey.getTimeText(this, user.getSeen()));
            if (user.getId() == My.id) {
                binding.gotoChat.setVisibility(View.INVISIBLE);
            } else {
                binding.gotoChat.setVisibility(user.isHiddenFromQuestionChat() ? View.INVISIBLE : View.VISIBLE);
            }

            f = user.getLocalFile();
            if (user.hasProfileImage()){
                Hey.print(user.getFullName(),"Has profile image");
                Hey.workWithProfileImage(user, doc1 -> {
                    binding.profileImage.setImageURI(Uri.fromFile(f));
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
            binding.list.setAdapter(adapter);
            binding.list.setOnItemClickListener((parent, view, position, id) -> {
                if (!user.isNumberHidden() && position == 0) {
                    Intent ph = new Intent(Intent.ACTION_DIAL);
                    ph.setData(Uri.parse("tel:" + user.getNumber()));
                    startActivity(ph);
                }
            });
            binding.list.setOnItemLongClickListener((parent, view, position, id) -> {
                if (!user.isNumberHidden() && position == 0) Hey.copyToClipboard(AccountInformation.this, user.getNumber());
                if (position==7) Hey.copyToClipboard(this, String.valueOf(user.getId()));
                return true;
            });
        }, errorMessage -> {

        });
    }
}