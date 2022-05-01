package com.normurodov_nazar.calcuator;

import static com.normurodov_nazar.calcuator.Hey.print;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

public class HelpActivity extends AppCompatActivity {
    AdView banner;
    TextView t;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        banner = findViewById(R.id.banner);
        banner.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Hey.print("banner",loadAdError.getMessage());
            }
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                banner.setVisibility(View.VISIBLE);
            }
        });
        banner.loadAd(new AdRequest.Builder().build());
        preferences = getSharedPreferences("b",MODE_PRIVATE);
        t = findViewById(R.id.h);
        t.setText(getString(R.string.r)
        .replaceAll("#","\n"));
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX,preferences.getFloat("bx",50));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        float f = t.getTextSize();
        print("textSize", String.valueOf(t.getTextSize()));
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            f = f * 1.1f;
            preferences.edit().putFloat("bx", f).apply();
            t.setTextSize(TypedValue.COMPLEX_UNIT_PX,f);
            print("textSizeIs", String.valueOf(f));
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            f = f * 0.9f;
            preferences.edit().putFloat("bx", f).apply();
            t.setTextSize(TypedValue.COMPLEX_UNIT_PX,f);
            print("textSizeIs", String.valueOf(f));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}