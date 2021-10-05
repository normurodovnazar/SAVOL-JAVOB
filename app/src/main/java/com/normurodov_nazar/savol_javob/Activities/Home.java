package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.HomeFragmentStateAdapter;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;

public class Home extends AppCompatActivity {
    TextView name,nameDrawer,numberDrawer;
    ImageView searchIcon,backDrawer,menuDrawer,profileImage;
    TextInputEditText editText;
    TabLayout tabLayout;
    ProgressBar progressBar;
    ViewPager2 viewPager2;
    ConstraintLayout constraintLayout, drawer;
    boolean drawerIsOpened = false, searchBarOpened = false;
    int width;
    LinearLayout logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initVars();
        setTabs();
        downloadMyData();
    }

    private void downloadMyData() {
        FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).addSnapshotListener((value, error) -> {
            if(value!=null){
                if(value.exists()) {
                    My.setDataFromDoc(value);
                    setMyData();
                } else {
                    showError().setOnDismissListener(d->finish());
                }
            }else {
                if (error != null) Hey.showAlertDialog(Home.this,getString(R.string.error_unknown)+":"+ error.getMessage()).setOnDismissListener(d->finish()); else showError().setOnDismissListener(d->finish());
            }
        });
    }

    private void setMyData() {
        nameDrawer.setText(My.fullName);name.setText(My.fullName);
        numberDrawer.setText(My.number);
        File f = new File(getExternalFilesDir("images").toString()+File.separatorChar+My.id);
        if(f.exists()){
            showProfileImage();
            profileImage.setImageURI(Uri.parse(f.getPath()));
        }else {
          //TODO download image
            FirebaseStorage.getInstance().getReference().child(Keys.users).child(String.valueOf(My.id)).getFile(f).addOnFailureListener(e -> {
                Hey.showAlertDialog(getApplicationContext(),getString(R.string.error_download_file)+":"+e.getLocalizedMessage());
            })
            .addOnSuccessListener(taskSnapshot -> {
                setMyData();
                Hey.print("success","Downloaded");
            })
            .addOnProgressListener(snapshot -> {
                Hey.print("down","value:"+Hey.getPercentage(snapshot));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) progressBar.setProgress(Hey.getPercentage(snapshot),true);
                else progressBar.setProgress(Hey.getPercentage(snapshot));
            });
        }
    }

    private void showProfileImage() {
        profileImage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void initVars() {
        progressBar = findViewById(R.id.progressHome);
        nameDrawer = findViewById(R.id.nameDrawer);
        numberDrawer = findViewById(R.id.numberDrawer);
        logout = findViewById(R.id.logout);logout.setOnClickListener(v->{
            Hey.getPreferences(this).edit().clear().apply();
            startActivity(new Intent(this,AuthUser.class));
            finish();
        });
        editText = findViewById(R.id.textFieldHome);
        name = findViewById(R.id.nameHome);
        searchIcon = findViewById(R.id.home_search_icon);
        searchIcon.setOnClickListener(v -> doWithSearchIcon());
        menuDrawer = findViewById(R.id.menuDrawer);
        menuDrawer.setOnClickListener(v -> doWithDrawer());
        backDrawer = findViewById(R.id.backDrawer);
        backDrawer.setOnClickListener(v -> doWithDrawer());
        Hey.print("Home logged","isLoggedIn:"+Hey.isLoggedIn(Hey.getPreferences(this)));
        if (!Hey.isLoggedIn(Hey.getPreferences(this))) {
            Hey.getPreferences(this).edit().putBoolean(Keys.logged, true).putLong(Keys.id,My.id).apply();
            Hey.print("Home logged","is not logged in new value of isLoggedIn:"+Hey.isLoggedIn(Hey.getPreferences(this)));
        }

        viewPager2 = findViewById(R.id.viewPager2);
        tabLayout = findViewById(R.id.tabLayout);

        constraintLayout = findViewById(R.id.aaaaa);
        drawer = findViewById(R.id.drawer);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        drawer.setTranslationX(width);
        editText.setTranslationX(width);
        profileImage = findViewById(R.id.profileImageHome);
        hideProfileImage();
    }

    private void hideProfileImage() {
        profileImage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }


    private void setTabs(){
        HomeFragmentStateAdapter adapter = new HomeFragmentStateAdapter(this);
        viewPager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 1:
                    tab.setText(getString(R.string.pu_blic));
                    break;
                case 2:
                    tab.setText(getString(R.string.my_questions));
                    break;
                default:
                    tab.setText(getString(R.string.personal));
            }
        }).attach();
    }

    private void doWithSearchIcon() {
        if (searchBarOpened) {
            searchIcon.setImageResource(R.drawable.search_glass);
            Hey.animateHorizontal(name, 0, 100);
            Hey.animateHorizontal(editText, width, 100);
            editText.clearFocus();
        } else {
            searchIcon.setImageResource(R.drawable.redo_ic);
            Hey.animateHorizontal(name, -width, 100);
            Hey.animateHorizontal(editText, 0, 100);
            editText.requestFocus();
        }
        searchBarOpened = !searchBarOpened;
    }

    private void doWithDrawer() {
        if (!drawerIsOpened) {
            Hey.animateHorizontal(constraintLayout, -width, 100);
            Hey.animateHorizontal(drawer, 0, 100);
            Hey.print("a", "drawer is opening");
        } else {
            Hey.animateHorizontal(constraintLayout, 0, 100);
            Hey.animateHorizontal(drawer, width, 100);
            Hey.print("a", "drawer is closing");
        }
        drawerIsOpened = !drawerIsOpened;
    }

    @Override
    public void onBackPressed() {
        if (drawerIsOpened) doWithDrawer();
        else super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            Hey.print("a","a"+My.id);
            return true;
        }else
        return super.onKeyDown(keyCode, event);
    }

    MyDialog showError(){
        return Hey.showUnknownError(this);
    }
}