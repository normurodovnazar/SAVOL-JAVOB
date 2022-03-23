package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.ThemeAdapter;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.databinding.ActivitySelectThemeBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectTheme extends AppCompatActivity {
    final ArrayList<Map<String,String>> allThemes = new ArrayList<>();
    boolean forSelection;

    private ActivitySelectThemeBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySelectThemeBinding.inflate(getLayoutInflater());
        View v = b.getRoot();
        setContentView(v);
        initVars();
    }

    private void initVars() {
        forSelection = getIntent().getBooleanExtra("s",true);
        FirebaseFirestore.getInstance().collection(Keys.theme).orderBy(Keys.time, Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            b.progress.setVisibility(View.INVISIBLE);
            String lang = getString(R.string.lang);
            for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                Map<String,String> data = new HashMap<>();
                data.put(Keys.theme,d.getString(lang));
                data.put(Keys.id, String.valueOf(d.getLong(Keys.time)));
                allThemes.add(data);
            }

            ThemeAdapter adapter = new ThemeAdapter(SelectTheme.this, allThemes, forSelection, (message, itemView, position) -> {
                Intent i = new Intent();
                i.putExtra(Keys.theme, allThemes.get(position).get(Keys.theme));
                i.putExtra(Keys.id,allThemes.get(position).get(Keys.id));
                setResult(RESULT_OK,i);
                finish();
            });
            b.recycler.setAdapter(adapter);
            b.recycler.setLayoutManager(new LinearLayoutManager(SelectTheme.this));
        }).addOnFailureListener(e -> {

        });
    }
}