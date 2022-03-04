package com.normurodov_nazar.adminapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.adminapp.MFunctions.Hey;
import com.normurodov_nazar.adminapp.MFunctions.Keys;
import com.normurodov_nazar.adminapp.MFunctions.My;
import com.normurodov_nazar.adminapp.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.adminapp.MyD.User;
import com.normurodov_nazar.adminapp.MyD.UserListAdapter;
import com.normurodov_nazar.adminapp.R;

import java.util.ArrayList;

public class SearchUsers extends AppCompatActivity {
    Button filter;
    ImageView back, search;
    TextView noResults;
    EditText searchField;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    boolean byName = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        initState();
        back.setOnClickListener(v -> onBackPressed());
        search.setOnClickListener(v -> searchResults());
    }

    private void setAdapterToRecyclerView(ArrayList<Long> userIds) {
        UserListAdapter adapter = new UserListAdapter(this, userIds, user -> {
            Intent i = new Intent(this, SingleChat.class);
            i.putExtra(Keys.chatId, Hey.getChatIdFromIds(My.id,user.getId()));
            i.putExtra(Keys.privateChat,false);
            startActivity(i);
        }, user -> {

        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void searchResults() {
        String text = searchField.getText().toString();
        if (!text.isEmpty())
            if (Hey.withUpper(text)) {
                viewLoading();
                Hey.searchUsersFromServer(this, text, byName, docs -> {
                    ArrayList<Long> userIds = new ArrayList<>();
                    for (DocumentSnapshot ds : docs) {
                        User user = User.fromDoc(ds);
                        if (user.getId() != My.id) if (!user.isHiddenFromSearch())userIds.add(user.getId());
                    }
                    if (userIds.isEmpty()) noResultsFound();
                    else {
                        setAdapterToRecyclerView(userIds);
                        viewResults();
                    }
                }, errorMessage -> noResultsFound());
            } else Hey.showToast(this, getString(R.string.mustBeUpper));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        } else return super.onKeyDown(keyCode, event);
    }

    private void initState() {
        filter = findViewById(R.id.searchFilter);
        filter.setOnClickListener(v -> onTapFilter());
        noResults = findViewById(R.id.no_results);
        searchField = findViewById(R.id.search_users_field);
        recyclerView = findViewById(R.id.search_results);
        progressBar = findViewById(R.id.loading);
        back = findViewById(R.id.back);
        search = findViewById(R.id.search);
    }

    private void onTapFilter() {
        MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(byName ? R.string.filterBySurname : R.string.filterByName), null, false);
        if (byName) {
            d.setOnDismissListener(dialog -> {
                if (d.getResult()) {
                    byName = false;
                    searchResults();
                    filter.setText(R.string.bySurname);
                }
            });
        } else {
            d.setOnDismissListener(dialog -> {
                if (d.getResult()) {
                    byName = true;
                    searchResults();
                    filter.setText(R.string.byName);
                }
            });
        }
    }

    private void viewLoading() {
        noResults.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void viewResults() {
        noResults.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void noResultsFound() {
        noResults.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }
}