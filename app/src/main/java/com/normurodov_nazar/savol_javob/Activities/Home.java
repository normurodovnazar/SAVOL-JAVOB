package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.MyD.UserListAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class Home extends AppCompatActivity {
    SharedPreferences preferences;
    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initVars();
    }

    private void initVars() {
        recyclerView = findViewById(R.id.recyclerViewHome);
        floatingActionButton = findViewById(R.id.floating);
        floatingActionButton.setOnClickListener(v -> {
            startActivity(new Intent(this,SearchUsers.class));
        });
        preferences = getPreferences(MODE_PRIVATE);
        if(!preferences.getBoolean(Keys.logged,false)){
            preferences.edit().putBoolean(Keys.logged,true).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<User> users = new ArrayList<>();
        UserListAdapter adapter = new UserListAdapter(users,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}