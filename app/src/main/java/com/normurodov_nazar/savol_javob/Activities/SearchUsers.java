package com.normurodov_nazar.savol_javob.Activities;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.gotoPrivateChat;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.MyD.UserListAdapter;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.databinding.ActivitySearchUsersBinding;

import java.net.UnknownHostException;
import java.util.ArrayList;

public class SearchUsers extends AppCompatActivity {
    boolean byName = true;
    private ActivitySearchUsersBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySearchUsersBinding.inflate(getLayoutInflater());
        View v = b.getRoot();
        setContentView(v);
        b.back.setOnClickListener(vx -> onBackPressed());
        b.search.setOnClickListener(vx -> searchResults());
        b.filter.setOnClickListener(zx->onTapFilter());
    }

    private void setAdapterToRecyclerView(ArrayList<Long> userIds) {
        UserListAdapter adapter = new UserListAdapter(this, userIds, user -> gotoPrivateChat(this,user.getId()), user -> {

        });
        b.usersRecycler.setAdapter(adapter);
        b.usersRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void searchResults() {
        String text = b.textField.getText().toString();
        if (!text.isEmpty())
            if (Hey.withUpper(text)) {
                viewLoading();
                Hey.searchUsersFromServer(this, text, byName, docs -> {
                    ArrayList<Long> userIds = new ArrayList<>();
                    for (DocumentSnapshot ds : docs) {
                        User user = null;
                        try {
                            user = User.fromDoc(ds);
                        }catch (UnknownHostException ignored){

                        }
                        if (user!=null) if (user.getId() != My.id) if (!user.isHiddenFromSearch())userIds.add(user.getId());
                    }
                    if (userIds.isEmpty()) noResultsFound();
                    else {
                        setAdapterToRecyclerView(userIds);
                        viewResults();
                    }
                }, errorMessage -> noResultsFound());
            } else Hey.showToast(this, getString(R.string.mustBeUpper));
    }

    private void onTapFilter() {
        MyDialogWithTwoButtons d = Hey.showDeleteDialog(this, getString(byName ? R.string.filterBySurname : R.string.filterByName), null, false);
        if (byName) {
            d.setOnDismissListener(dialog -> {
                if (d.getResult()) {
                    byName = false;
                    searchResults();
                    b.filter.setText(R.string.bySurname);
                }
            });
        } else {
            d.setOnDismissListener(dialog -> {
                if (d.getResult()) {
                    byName = true;
                    searchResults();
                    b.filter.setText(R.string.byName);
                }
            });
        }
    }

    private void viewLoading() {
        b.noResult.setVisibility(View.INVISIBLE);
        b.usersRecycler.setVisibility(View.INVISIBLE);
        b.loading.setVisibility(View.VISIBLE);
    }

    private void viewResults() {
        b.noResult.setVisibility(View.INVISIBLE);
        b.usersRecycler.setVisibility(View.VISIBLE);
        b.loading.setVisibility(View.INVISIBLE);
    }

    private void noResultsFound() {
        b.noResult.setVisibility(View.VISIBLE);
        b.usersRecycler.setVisibility(View.INVISIBLE);
        b.loading.setVisibility(View.INVISIBLE);
    }
}