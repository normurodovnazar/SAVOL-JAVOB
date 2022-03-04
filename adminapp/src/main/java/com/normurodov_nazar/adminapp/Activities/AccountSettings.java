package com.normurodov_nazar.adminapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.normurodov_nazar.adminapp.MFunctions.Hey;
import com.normurodov_nazar.adminapp.MFunctions.Keys;
import com.normurodov_nazar.adminapp.MFunctions.My;
import com.normurodov_nazar.adminapp.MyD.DrawerItem;
import com.normurodov_nazar.adminapp.MyD.DrawerItemsAdapter;
import com.normurodov_nazar.adminapp.MyD.EditMode;
import com.normurodov_nazar.adminapp.MyD.LoadingDialog;
import com.normurodov_nazar.adminapp.MyD.StatusListener;
import com.normurodov_nazar.adminapp.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AccountSettings extends AppCompatActivity {
    RecyclerView recyclerView;
    ActivityResultLauncher<Intent> p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        recyclerView = findViewById(R.id.accountSettings);
        p = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
        setItems();
    }

    private void setItems() {
        boolean hiddenFromSearch = My.user.isHiddenFromSearch(), hiddenFromQC = My.user.isHiddenFromQuestionChat(),hiddenNumber = My.user.isNumberHidden();
        ArrayList<DrawerItem> drawerItems = new ArrayList<>(Arrays.asList(
                new DrawerItem(R.string.editName,R.drawable.settings_ic),
                new DrawerItem(R.string.editSurname,R.drawable.settings_ic),
                new DrawerItem(R.string.change_image,R.drawable.settings_ic),
                new DrawerItem(R.string.deleteProfileImage,R.drawable.settings_ic),
                new DrawerItem(hiddenFromSearch ? R.string.unhideFromSearch : R.string.hideFromSearch,R.drawable.settings_ic),
                new DrawerItem(hiddenFromQC ? R.string.unhideFromQC : R.string.hideFromQC,R.drawable.settings_ic),
                new DrawerItem(hiddenNumber ? R.string.unhideNumber : R.string.hideNumber,R.drawable.settings_ic)
        ));
        DrawerItemsAdapter adapter = new DrawerItemsAdapter(this, drawerItems, (message, itemView, position) -> {
            DocumentReference d = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id));
            switch (position){
                case 0:
                    Hey.editMessage(this, Collections.singletonMap(Keys.name, My.name), d, EditMode.name, doc -> { });
                    break;
                case 1:
                    Hey.editMessage(this, Collections.singletonMap(Keys.surname, My.surname), d, EditMode.surname, doc -> { });
                    break;
                case 2:
                    LoadingDialog x = Hey.showLoadingDialog(this);
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            x.closeDialog();
                            Hey.pickImage(p);
                        }

                        @Override
                        public void offline() {
                            x.closeDialog();
                            Hey.showToast(AccountSettings.this, getString(R.string.error_connection));
                        }
                    }, errorMessage -> x.closeDialog(), this);
                    break;
                case 3:
                    LoadingDialog ld = Hey.showLoadingDialog(AccountSettings.this);
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            Hey.updateDocument(AccountSettings.this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)),
                                    Collections.singletonMap(Keys.imageSize, -1),
                                    doc -> FirebaseStorage.getInstance().getReference().child(Keys.users).child(String.valueOf(My.id)).delete().addOnSuccessListener(unused -> ld.closeDialog()).addOnFailureListener(e -> {
                                        Hey.showAlertDialog(AccountSettings.this, e.getLocalizedMessage());
                                        ld.closeDialog();
                                    }), errorMessage -> {
                                    });
                        }

                        @Override
                        public void offline() {
                            ld.closeDialog();
                            Hey.showToast(AccountSettings.this, getString(R.string.error_connection));
                        }
                    }, errorMessage -> ld.closeDialog(), this);
                    break;
                case 4:
                    LoadingDialog l = Hey.showLoadingDialog(AccountSettings.this);
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            Hey.updateDocument(AccountSettings.this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), Collections.singletonMap(Keys.hiddenFromSearch, !hiddenFromSearch), doc -> {
                                setItems();
                                Hey.showToast(AccountSettings.this, getString(R.string.changed));
                                l.closeDialog();
                            }, errorMessage -> l.closeDialog());
                        }
                        @Override
                        public void offline() {
                            Hey.showToast(AccountSettings.this,getString(R.string.error_connection));
                            l.closeDialog();
                        }
                    }, errorMessage -> l.closeDialog(),this);
                    break;
                case 5:
                    LoadingDialog lx = Hey.showLoadingDialog(AccountSettings.this);
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            Hey.updateDocument(AccountSettings.this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), Collections.singletonMap(Keys.hiddenFromQuestionChat, !hiddenFromQC), doc -> {
                                setItems();
                                Hey.showToast(AccountSettings.this, getString(R.string.changed));
                                lx.closeDialog();
                            }, errorMessage -> lx.closeDialog());
                        }

                        @Override
                        public void offline() {
                            Hey.showToast(AccountSettings.this,getString(R.string.error_connection));
                            lx.closeDialog();
                        }
                    }, errorMessage -> lx.closeDialog(),this);
                    break;
                case 6:
                    LoadingDialog ln = Hey.showLoadingDialog(AccountSettings.this);
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            Hey.updateDocument(AccountSettings.this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), Collections.singletonMap(Keys.hidden, !hiddenNumber), doc -> {
                                setItems();
                                Hey.showToast(AccountSettings.this, getString(R.string.changed));
                                ln.closeDialog();
                            }, errorMessage -> ln.closeDialog());
                        }

                        @Override
                        public void offline() {
                            Hey.showToast(AccountSettings.this,getString(R.string.error_connection));
                            ln.closeDialog();
                        }
                    }, errorMessage -> ln.closeDialog(),this);
                    break;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void onResult(ActivityResult result) {
        if (result.getData() != null) {
            Uri uri = result.getData().getData();
            Hey.cropImage(this, this, uri, new File(My.folder + Calendar.getInstance().getTimeInMillis()), true, errorMessage -> {
            });
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
                    Hey.uploadImageForProfile(this, file.getPath(), String.valueOf(My.id), doc -> {
                        Map<String, Object> x = new HashMap<>();
                        x.put(Keys.imageSize, file.length());
                        FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).update(x).addOnSuccessListener(unused -> {
                            if (a.isShowing()) a.closeDialog();
                        }).addOnFailureListener(e -> Hey.showAlertDialog(this, getString(R.string.error) + ":" + e.getMessage()));
                    }, (position, name) -> {
                        if (a.isShowing()) a.closeDialog();
                    }, errorMessage -> {
                        if (a.isShowing()) a.closeDialog();
                    });
                }
            } else if (data != null) {
                Log.e("onActivityResult", "Result came with error");
                Throwable throwable = UCrop.getError(data);
                String mes;
                if (throwable != null) mes = throwable.getMessage();
                else mes = getString(R.string.unknown);
                Hey.showAlertDialog(this, getString(R.string.error_unknown) + mes);
            }

    }
}