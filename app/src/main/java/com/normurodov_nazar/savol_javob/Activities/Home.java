package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.DrawerItem;
import com.normurodov_nazar.savol_javob.MyD.DrawerItemsAdapter;
import com.normurodov_nazar.savol_javob.MyD.HomeFragmentStateAdapter;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {
    TextView name, nameDrawer, numberDrawer, privacy;
    ImageView addIcon, backDrawer, menuDrawer, profileImage, dayNight;
    TabLayout tabLayout;
    ProgressBar progressBar;
    ViewPager2 viewPager2;
    ConstraintLayout main, drawer, imageSide;
    boolean drawerIsOpened = false, imageViewing = false;
    int width;
    RecyclerView items;
    SubsamplingScaleImageView bigImage;
    File f;
    ActivityResultLauncher<Intent> p;
    ListenerRegistration numbers, my;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        My.activeId = Keys.id;
        initVars();
        setTabs();
        My.updated = false;
        downloadMyData();
    }

    private void downloadMyData() {
        if (My.id == 0)
            Hey.showAlertDialog(this, getString(R.string.error) + ":" + getString(R.string.unknown)).setOnDismissListener(dialog -> finish());
        else {
            my = Hey.addDocumentListener(this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), doc -> {
                Hey.print("updated", "my document");
                My.setDataFromDoc(doc);
                My.user = User.fromDoc(doc);
                Long bT = doc.getLong(Keys.blockTime);
                My.blockTime = bT == null ? 0 : bT;
                if (!My.noProblem)
                    Hey.showAlertDialog(this, getString(R.string.error) + ":" + getString(R.string.unknown)).setOnDismissListener(dialog -> finish());
                setMyData();
                String type = getIntent().getStringExtra(Keys.type);
                Intent i;
                String x = getIntent().getStringExtra(Keys.id) == null ? "asd" : getIntent().getStringExtra(Keys.id);
                if (type != null && !My.actionCompleted) {
                    My.actionCompleted = true;
                    My.activeId = x;
                    Hey.print("called", "AA");
                    switch (type) {
                        case Keys.privateChat:
                            String id = getIntent().getStringExtra(Keys.id);
                            if (id != null) {
                                i = new Intent(this, SingleChat.class);
                                i.putExtra(Keys.chatId, Hey.getChatIdFromIds(My.id, Long.parseLong(id)));
                                startActivity(i);
                            }
                            break;
                        case Keys.publicQuestions:
                        case Keys.needQuestions:
                            i = new Intent(this, QuestionChat.class);
                            i.putExtra(Keys.id, getIntent().getStringExtra(Keys.id));
                            i.putExtra(Keys.theme, getIntent().getStringExtra(Keys.theme));
                            startActivity(i);
                            break;
                    }
                }
                Hey.updateActivity();
            }, errorMessage -> finish());
            numbers = Hey.addDocumentListener(this, FirebaseFirestore.getInstance().collection(Keys.appNumbers).document(Keys.appNumbers), doc -> {
                Long ql = doc.getLong(Keys.questionLimit), uPD = doc.getLong(Keys.unitsForPerDay), r = doc.getLong(Keys.unitsForAd);
                String s = doc.getString(Keys.id);
                My.questionLimit = ql == null ? 5 : ql;
                My.unitsForPerDay = uPD == null ? 10 : uPD;
                My.unitsForAd = r == null ? 10 : r;
                My.petName = s == null ? "" : Keys.petKey + Hey.reverseString(s);
            }, errorMessage -> {
            });
        }
    }

    private void setMyData() {
        String t = Hey.getPreferences(this).getString(Keys.token, "a");
        Map<String, Object> d = new HashMap<>();
        if (!t.equals("a")) {
            if (!t.equals(My.token)) {
                Hey.print("token", "updated");
                d.put(Keys.token, t);
                FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).update(d);
            } else Hey.print("token", "all right");
        } else {
            Hey.print("token", "generated");
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
                Hey.getPreferences(this).edit().putString(Keys.token, s).apply();
                d.put(Keys.token, s);
                FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).update(d);
            }).addOnFailureListener(e -> Hey.showToast(this, getString(R.string.error) + ":" + e.getLocalizedMessage()));
        }
        nameDrawer.setText(My.fullName);
        name.setText(My.fullName);
        numberDrawer.setText(My.number);
        f = new File(My.user.getLocalFileName());
        if (My.user.hasProfileImage()) {
            Hey.print(My.user.getFullName(), "Has profile image");
            Hey.workWithProfileImage(My.user, doc -> {
                showProfileImage();
                profileImage.setImageURI(Uri.parse(f.getPath()));
            }, errorMessage -> Hey.downloadFile(this, Keys.users, String.valueOf(My.id), f, (progress, total) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    progressBar.setProgress(Hey.getPercentage(progress, total), true);
                else progressBar.setProgress(Hey.getPercentage(progress, total));
            }, doc -> {
                showProfileImage();
                profileImage.setImageURI(Uri.parse(f.getPath()));
            }, e -> {

            }));
        } else {
            profileImage.setImageResource(R.drawable.user_icon);
            Hey.print(My.user.getFullName(), "Hasn't profile image");
        }

    }

    private void showProfileImage() {
        profileImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void initVars() {
        MobileAds.initialize(this, initializationStatus -> Hey.print("data", "a" + initializationStatus.getAdapterStatusMap().toString()));
        p = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
        privacy = findViewById(R.id.privacy);Hey.gotoPrivacy(this,privacy);
        items = findViewById(R.id.items);
        workWithRecycler();
        progressBar = findViewById(R.id.progressHome);
        nameDrawer = findViewById(R.id.nameDrawer);
        numberDrawer = findViewById(R.id.numberDrawer);
        name = findViewById(R.id.nameHome);
        addIcon = findViewById(R.id.home_search_icon);
        addIcon.setOnClickListener(v -> workWithAddIcon());
        menuDrawer = findViewById(R.id.menuDrawer);
        menuDrawer.setOnClickListener(v -> doWithDrawer());
        backDrawer = findViewById(R.id.backDrawer);
        backDrawer.setOnClickListener(v -> doWithDrawer());
        if (!Hey.isLoggedIn(Hey.getPreferences(this))) {
            Hey.getPreferences(this).edit().putBoolean(Keys.logged, true).putLong(Keys.id, My.id).apply();
        }

        viewPager2 = findViewById(R.id.viewPager2);
        tabLayout = findViewById(R.id.tabLayout);

        main = findViewById(R.id.aaaaa);
        drawer = findViewById(R.id.drawer);
        imageSide = findViewById(R.id.bigImageSide);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        My.width = width;
        drawer.setTranslationX(width);
        profileImage = findViewById(R.id.profileImageHome);
        profileImage.setOnClickListener(v -> tapImage());
        bigImage = findViewById(R.id.bigProfileImage);
        dayNight = findViewById(R.id.dayNight);
        dayNight.setOnClickListener(view -> Hey.showPopupMenu(this, dayNight, new ArrayList<>(Arrays.asList(getString(R.string.day), getString(R.string.night), getString(R.string.followSystem))), (position, name) -> {
            switch (position) {
                case 0:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Hey.getPreferences(this).edit().putString(Keys.dayNight, Keys.day).apply();
                    break;
                case 1:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Hey.getPreferences(this).edit().putString(Keys.dayNight, Keys.night).apply();
                    break;
                case 2:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    Hey.getPreferences(this).edit().putString(Keys.dayNight, Keys.system).apply();
                    break;
            }
        }, true));
        hideProfileImage();
    }

    private void workWithRecycler() {
        ArrayList<DrawerItem> drawerItems = new ArrayList<>(Arrays.asList(
                new DrawerItem(R.string.profileInfo, R.drawable.info_ic),//0
                new DrawerItem(R.string.accountSettings, R.drawable.settings_ic),//1
                new DrawerItem(R.string.searchQuestions, R.drawable.search_question),//2
                new DrawerItem(R.string.addUnits, R.drawable.ic_units),//3
                new DrawerItem(R.string.notificationSettings, R.drawable.ic_notification),//4
                new DrawerItem(R.string.unitValues,R.drawable.ic_unit_values)
                //new DrawerItem(R.string.logout, R.drawable.logout_ic)
        ));
        DrawerItemsAdapter adapter = new DrawerItemsAdapter(this, drawerItems, (message, itemView, position) -> {
            switch (position) {
                case 0:
                    startActivity(new Intent(this, AccountInformation.class).putExtra(Keys.id, String.valueOf(My.id)).putExtra(Keys.fromChat, true));
                    break;
                case 1:
                    startActivity(new Intent(this,AccountSettings.class));
                    break;
                case 2:
                    startActivity(new Intent(this, SearchQuestions.class));
                    break;
                case 3:
                    startActivity(new Intent(this, ShowAd.class));
                    break;
                case 4:
                    startActivity(new Intent(this, NotificationSettings.class));
                    break;
                case 5:
                    String s = getString(R.string.rulesFull)
                            .replaceAll("xxx", String.valueOf(My.unitsForPerDay))
                            .replaceAll("yyy", String.valueOf(My.unitsForPerDay*5))
                            .replaceAll("zzz",getString(R.string.addUnits))
                            .replaceAll("nnn","\n");
                    Hey.showAlertDialog(this,s);
                    break;
                default:
                    FirebaseAuth.getInstance().signOut();
                    Hey.getPreferences(this).edit().clear().apply();
                    startActivity(new Intent(this, AuthUser.class));
                    this.finish();
                    break;
            }
        });
        items.setAdapter(adapter);
        items.setLayoutManager(new LinearLayoutManager(this));
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

    private void tapImage() {
        if (My.user.hasProfileImage) {
            if (!imageViewing) {
                drawer.setVisibility(View.INVISIBLE);
                main.setVisibility(View.INVISIBLE);
                imageSide.setVisibility(View.VISIBLE);
                Hey.setBigImage(bigImage, f);
                imageViewing = true;
            }
        }
    }

    private void workWithAddIcon() {
        if (!Hey.amIBlocked()) {
            if (viewPager2.getCurrentItem() == 0) {
                Intent i = new Intent(this, SearchUsers.class);
                startActivity(i);
            } else if (My.unitsForPerDay <= My.units) {
                CollectionReference publicQuestions = FirebaseFirestore.getInstance().collection(Keys.publicQuestions);
                LoadingDialog d = Hey.showLoadingDialog(this);
                Hey.getCollection(this, publicQuestions, docs -> {
                    if (My.questionLimit > docs.size()) {
                        Intent i = new Intent(this, NewQuestionActivity.class);
                        startActivity(i);
                    } else
                        Hey.showAlertDialog(this, getString(R.string.questionLimitError).replace("xxx", String.valueOf(My.questionLimit)));
                    d.closeDialog();
                }, errorMessage -> d.closeDialog());
            } else {
                MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(R.string.noEnoughUnits).replace("xxx", String.valueOf(My.units)).replace("yyy", String.valueOf(My.unitsForPerDay)), null, false);
                d.setOnDismissListener(dialogInterface -> {
                    if (d.getResult()) {
                        startActivity(new Intent(this, ShowAd.class));
                    }
                });
            }
        } else Hey.showYouBlockedDialog(this);
    }

    private void hideProfileImage() {
        profileImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setTabs() {
        HomeFragmentStateAdapter adapter = new HomeFragmentStateAdapter(this);
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.privateS);
                    break;
                case 1:
                    tab.setText(R.string.pu_blic);
                    break;
                case 2:
                    tab.setText(R.string.my_questions);
                    break;
                default:
                    tab.setText(R.string.needQuestions);
            }
        }).attach();
    }

    private void doWithDrawer() {
        if (!drawerIsOpened) {
            Hey.animateHorizontal(main, -width, 100);
            Hey.animateHorizontal(drawer, 0, 100);
        } else {
            Hey.animateHorizontal(main, 0, 100);
            Hey.animateHorizontal(drawer, width, 100);
        }
        drawerIsOpened = !drawerIsOpened;
    }

    @Override
    public void onBackPressed() {
        if (drawerIsOpened) {
            if (!imageViewing) doWithDrawer();
            else {
                drawer.setVisibility(View.VISIBLE);
                main.setVisibility(View.VISIBLE);
                imageSide.setVisibility(View.GONE);
                imageViewing = false;
            }
        } else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        My.applied = false;
        if (my != null) my.remove();
        if (numbers != null) numbers.remove();
    }
}