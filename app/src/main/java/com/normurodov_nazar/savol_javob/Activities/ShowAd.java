package com.normurodov_nazar.savol_javob.Activities;

import static com.normurodov_nazar.savol_javob.MFunctions.Keys.adId;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyEventTracker;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.ItemClickListener;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.R;

import java.util.Collections;

public class ShowAd extends AppCompatActivity {
    AdView banner;
    TextView textView;
    Button button;

    FullScreenContentCallback fullScreenContentCallback;
    RewardedAd rewarded;
    boolean loading = false;
    LoadingDialog d;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ad);
        d = Hey.showLoadingDialog(this, (position, name) -> finish());
        initVars();
    }

    private void initVars() {
        fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        };
        button = findViewById(R.id.showAd);button.setOnClickListener(view -> {
            if (!loading) loadRewarded(); else Hey.showToast(this,getString(R.string.wait));
        });
        textView = findViewById(R.id.textInAd);textView.setText(getString(R.string.showAdText).replaceAll("xxx", String.valueOf(My.unitsForAd)).replaceAll("aaa",getString(R.string.showAd)));
        banner = findViewById(R.id.banner);
        banner.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                d.closeDialog();
                Hey.print("banner",loadAdError.getMessage());
                if (loadAdError.getCode()==0) {
                    Hey.showAlertDialog(ShowAd.this,getString(R.string.error_connection)).setOnDismissListener(dialogInterface -> finish());
                }else {
                    banner.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                d.closeDialog();
            }
        });
        banner.loadAd(new AdRequest.Builder().build());
    }

    private void loadRewarded() {
        showLoading();
        RewardedAd.load(this, adId, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                super.onAdLoaded(rewardedAd);
                rewarded = rewardedAd;
                showRewarded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                showNotLoading();
                Hey.showAlertDialog(ShowAd.this,getString(R.string.error)+":"+loadAdError.getMessage());
            }
        });
    }

    private void showRewarded() {
        if (rewarded!=null){
            rewarded.show(this, rewardItem -> {
                showNotLoading();
                Hey.updateDocument(this, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)), Collections.singletonMap(Keys.units, My.units + My.unitsForAd), doc -> Hey.showToast(this,getString(R.string.addedUnits).replaceAll("xxx", String.valueOf(My.unitsForAd))), errorMessage -> {

                });
            });
        }
    }

    private void showNotLoading() {
        loading = false;
        Hey.setButtonAsDefault(this,button,getString(R.string.showAd));
    }

    private void showLoading() {
        loading = true;
        Hey.setButtonAsLoading(this,button);
    }
}