package com.normurodov_nazar.adminapp.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.normurodov_nazar.adminapp.MFunctions.Keys;
import com.normurodov_nazar.adminapp.MyD.ThemeAdapter;
import com.normurodov_nazar.adminapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NotificationSettings extends AppCompatActivity {
    ProgressBar bar;
    RecyclerView recycler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        initVars();
    }

    private void initVars() {
        bar = findViewById(R.id.barNS);
        recycler = findViewById(R.id.ns);
        ArrayList<Map<String,String>> settings = new ArrayList<>();
        settings.add(Collections.singletonMap(Keys.theme,getString(R.string.sound)));
        settings.add(Collections.singletonMap(Keys.theme,getString(R.string.vibrate)));
        settings.add(Collections.singletonMap(Keys.theme,getString(R.string.nPrivate)));
        FirebaseFirestore.getInstance().collection(Keys.theme).orderBy(Keys.time, Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            bar.setVisibility(View.INVISIBLE);
            for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                Map<String,String> s = new HashMap<>();
                s.put(Keys.theme,d.getString(getString(R.string.lang)));
                s.put(Keys.id, String.valueOf(d.getLong(Keys.time)));
                settings.add(s);
            }
            ThemeAdapter adapter = new ThemeAdapter(this, settings, false, (message, itemView, position) -> {});
            recycler.setAdapter(adapter);
            recycler.setLayoutManager(new LinearLayoutManager(this));
        }).addOnFailureListener(e -> {

        });
    }
}