package com.normurodov_nazar.savol_javob.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.DocumentsListener;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectTheme extends AppCompatActivity {
    ListView themesList;
    ArrayList<String> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_theme);
        initVars();
    }

    private void initVars() {
        themesList = findViewById(R.id.themes);
        Hey.getCollection(this, FirebaseFirestore.getInstance().collection(Keys.theme), docs -> {
            for (DocumentSnapshot d : docs) arrayList.add(d.getId());
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item,arrayList);
            themesList.setAdapter(arrayAdapter);
            themesList.setOnItemClickListener((adapterView, view, i, l) -> {
                setResult(RESULT_OK,new Intent().putExtra(Keys.theme,arrayList.get(i)));
                finish();
            });
        }, errorMessage -> {

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setResult(RESULT_CANCELED);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            String s = "Chiziqli tenglamalar";
            Hey.addDocumentToCollection(this, FirebaseFirestore.getInstance().collection(Keys.theme), s, new HashMap<>(), doc -> {
                Hey.print("added",s);
            }, errorMessage -> {
                Hey.print("error",errorMessage);
            });
            return true;
        }
        else return super.onKeyDown(keyCode, event);
    }
}