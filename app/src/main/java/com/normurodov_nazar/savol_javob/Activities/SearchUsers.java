package com.normurodov_nazar.savol_javob.Activities;

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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.MyD.UserListAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class SearchUsers extends AppCompatActivity {
    Button filter;
    ImageView back, search;
    TextView noResults;
    EditText searchField;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    CollectionReference collection = FirebaseFirestore.getInstance().collection(Keys.users);
    boolean byName = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        initState();
        back.setOnClickListener(v -> onBackPressed());
        search.setOnClickListener(v -> searchResults());
    }

    private void setAdapterToRecyclerView(ArrayList<User> users) {
        UserListAdapter adapter = new UserListAdapter(this, users, true, (message, itemView, position) -> {
            Intent i = new Intent(this, SingleChat.class);
            i.putExtra(Keys.chatId, Hey.getChatIdFromIds(My.id, users.get(position).getId()));
            startActivity(i);
            Hey.addToChats(this, My.id, users.get(position).getId());
        }, (message, itemView, position) -> {

        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void searchResults() {
        String text = searchField.getText().toString();
        if (!text.isEmpty())
            if (Hey.withUpper(text)){
                if (text.length() >= 4) {
                    viewLoading();
                    Hey.searchUsersFromServer(this, text, byName, docs -> {
                        ArrayList<User> users = new ArrayList<>();
                        for (DocumentSnapshot ds : docs) {
                            User user = User.fromDoc(ds);
                            if (user.getId() != My.id ) users.add(user);
                        }
                        if (users.isEmpty()) noResultsFound();
                        else {
                            setAdapterToRecyclerView(users);
                            viewResults();
                        }
                    }, errorMessage -> noResultsFound());
                }
                else Hey.showToast(this, getString(R.string.characterError));
            }else Hey.showToast(this,getString(R.string.mustBeUpper));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            return true;
        }else return super.onKeyDown(keyCode, event);
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
                    Hey.print("byName", "false");
                }
            });
        } else {
            d.setOnDismissListener(dialog -> {
                if (d.getResult()) {
                    byName = true;
                    searchResults();
                    filter.setText(R.string.byName);
                    Hey.print("byName", "true");
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