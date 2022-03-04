package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.ThemeAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectTheme extends AppCompatActivity {
    RecyclerView recycler;
    ProgressBar bar;
    final ArrayList<Map<String,String>> allThemes = new ArrayList<>();
    boolean forSelection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_theme);
        initVars();
    }

    private void initVars() {
        forSelection = getIntent().getBooleanExtra("s",true);
        bar = findViewById(R.id.progressTheme);
        recycler = findViewById(R.id.recyclerThemes);
        FirebaseFirestore.getInstance().collection(Keys.theme).orderBy(Keys.time, Query.Direction.ASCENDING).get().addOnSuccessListener(queryDocumentSnapshots -> {
            bar.setVisibility(View.INVISIBLE);
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
            recycler.setAdapter(adapter);
            recycler.setLayoutManager(new LinearLayoutManager(SelectTheme.this));
        }).addOnFailureListener(e -> {

        });
    }
}