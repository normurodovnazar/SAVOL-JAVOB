package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.graphics.Color;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.DrawerItem;
import com.normurodov_nazar.savol_javob.MyD.DrawerItemsAdapter;
import com.normurodov_nazar.savol_javob.MyD.EditMode;
import com.normurodov_nazar.savol_javob.MyD.HomeFragmentStateAdapter;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {
    TextView name, nameDrawer, numberDrawer;
    ImageView addIcon, backDrawer, menuDrawer, profileImage;
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
        initVars();
        setTabs();
        downloadMyData();
    }

    private void downloadMyData() {
        if (My.id == 0)
            Hey.showAlertDialog(this, getString(R.string.error) + ":" + getString(R.string.unknown)).setOnDismissListener(dialog -> finish());
        else {
            my = Hey.addDocumentListener(this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), doc -> {
                My.setDataFromDoc(doc);
                My.user = User.fromDoc(doc);
                if (!My.noProblem)
                    Hey.showAlertDialog(this, getString(R.string.error) + ":" + getString(R.string.unknown)).setOnDismissListener(dialog -> finish());
                setMyData();
            }, errorMessage -> finish());
            numbers = Hey.addDocumentListener(this, FirebaseFirestore.getInstance().collection(Keys.appNumbers).document(Keys.appNumbers), doc -> {
                Long ql = doc.getLong(Keys.questionLimit), uPD = doc.getLong(Keys.unitsForPerDay);
                My.questionLimit = ql == null ? 5 : ql;
                My.unitsForPerDay = uPD == null ? 10 : uPD;
            }, errorMessage -> {

            });
        }
    }

    private void setMyData() {
        String t = Hey.getPreferences(this).getString(Keys.token, "a");
        Map<String, Object> d = new HashMap<>();
        if (!t.equals("a") && !t.equals(My.token)) {
            d.put(Keys.token, t);
            FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).update(d);
        } else {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
                d.put(Keys.token, s);
                FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).update(d);
            }).addOnFailureListener(e -> {

            });
        }
        nameDrawer.setText(My.fullName);
        name.setText(My.fullName);
        numberDrawer.setText(My.number);
        f = new File(My.user.getLocalFileName());
        Hey.workWithProfileImage(My.user, doc -> {
                    showProfileImage();
                    profileImage.setImageURI(Uri.parse(f.getPath()));
                },
                errorMessage ->
                        Hey.downloadFile(this, Keys.users, String.valueOf(My.id), f, (progress, total) -> {
                            Hey.print("down", "value:" + Hey.getPercentage(progress, total));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                progressBar.setProgress(Hey.getPercentage(progress, total), true);
                            else progressBar.setProgress(Hey.getPercentage(progress, total));
                        }, doc -> {
                            showProfileImage();
                            profileImage.setImageURI(Uri.parse(f.getPath()));
                            Hey.print("success", "Downloaded");
                        }, e -> {

                        }));
    }

    private void showProfileImage() {
        profileImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void initVars() {
        p = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
        items = findViewById(R.id.items);
        workWithRecycler();
        progressBar = findViewById(R.id.progressHome);
        nameDrawer = findViewById(R.id.nameDrawer);
        numberDrawer = findViewById(R.id.numberDrawer);
        name = findViewById(R.id.nameHome);
        addIcon = findViewById(R.id.home_search_icon);
        addIcon.setOnClickListener(v -> doWithAddIcon());
        menuDrawer = findViewById(R.id.menuDrawer);
        menuDrawer.setOnClickListener(v -> doWithDrawer());
        backDrawer = findViewById(R.id.backDrawer);
        backDrawer.setOnClickListener(v -> doWithDrawer());
        Hey.print("Home logged", "isLoggedIn:" + Hey.isLoggedIn(Hey.getPreferences(this)));
        if (!Hey.isLoggedIn(Hey.getPreferences(this))) {
            Hey.getPreferences(this).edit().putBoolean(Keys.logged, true).putLong(Keys.id, My.id).apply();
            Hey.print("Home logged", "is not logged in new value of isLoggedIn:" + Hey.isLoggedIn(Hey.getPreferences(this)));
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
        hideProfileImage();
    }

    private void workWithRecycler() {
        ArrayList<DrawerItem> drawerItems = new ArrayList<>(Arrays.asList(
                new DrawerItem(R.string.profileInfo, R.drawable.info_ic),
                new DrawerItem(R.string.accountSettings, R.drawable.settings_ic),
                new DrawerItem(R.string.searchQuestions,R.drawable.search_question),
                new DrawerItem(R.string.logout,R.drawable.logout_ic)
        ));
        DrawerItemsAdapter adapter = new DrawerItemsAdapter(this, drawerItems, (message, itemView, position) -> {
            switch (position) {
                case 0:
                    startActivity(new Intent(this, AccountInformation.class).putExtra(Keys.id, String.valueOf(My.id)).putExtra(Keys.fromChat, true));
                    break;
                case 1:
                    Hey.showPopupMenu(this, itemView, new ArrayList<>(Arrays.asList(getString(R.string.editName), getString(R.string.editSurname), getString(R.string.change_image))), (pos, name) -> {
                        DocumentReference d = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id));
                        switch (pos) {
                            case 0:
                                Hey.editMessage(this, Collections.singletonMap(Keys.name, My.name), d, EditMode.name, doc -> {

                                });
                                break;
                            case 1:
                                Hey.editMessage(this, Collections.singletonMap(Keys.surname, My.surname), d, EditMode.surname, doc -> {

                                });
                                break;
                            case 2:
                                Hey.pickImage(p);
                                break;
                        }
                    }, true);
                    break;
                case 2:
                    startActivity(new Intent(this,SearchQuestions.class));
                    break;
                default:
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
                    Hey.uploadImageForProfile(this, file.getPath(), String.valueOf(My.id), doc -> {
                        Hey.print("a", "uploaded");
                        Map<String, Object> x = new HashMap<>();
                        x.put(Keys.imageSize, file.length());
                        FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).update(x).addOnSuccessListener(unused -> {
                            if (a.isShowing()) a.dismiss();
                            Hey.print("a", "updated");
                        }).addOnFailureListener(e -> Hey.showAlertDialog(this, getString(R.string.error) + ":" + e.getMessage()));
                    }, (position, name) -> {
                        if (a.isShowing()) a.dismiss();
                    }, errorMessage -> {
                        if (a.isShowing()) a.dismiss();
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
        if (!imageViewing) {
            drawer.setVisibility(View.INVISIBLE);
            main.setVisibility(View.INVISIBLE);
            imageSide.setVisibility(View.VISIBLE);
            bigImage.setImage(ImageSource.uri(Uri.fromFile(f)));
            bigImage.setBackgroundColor(Color.BLACK);
            bigImage.setMaxScale(15);
            bigImage.setMinScale(0.1f);
            imageViewing = true;
        }
    }

    private void doWithAddIcon() {
        Intent i;
        if (viewPager2.getCurrentItem() == 0) {
            i = new Intent(this, SearchUsers.class);
            startActivity(i);
        } else if (My.unitsForPerDay <= My.units) {
            i = new Intent(this, NewQuestionActivity.class);
            startActivity(i);
        } else
            Hey.showAlertDialog(this, getString(R.string.noEnoughUnits).replace("xxx", String.valueOf(My.units)).replace("yyy", String.valueOf(My.unitsForPerDay)));
    }

    private void hideProfileImage() {
        profileImage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setTabs() {
        HomeFragmentStateAdapter adapter = new HomeFragmentStateAdapter(this);
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.personal);
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
            Hey.print("a", "drawer is opening");
        } else {
            Hey.animateHorizontal(main, 0, 100);
            Hey.animateHorizontal(drawer, width, 100);
            Hey.print("a", "drawer is closing");
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
        my.remove();
        numbers.remove();
    }
}