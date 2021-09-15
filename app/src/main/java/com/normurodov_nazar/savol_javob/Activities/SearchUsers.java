package com.normurodov_nazar.savol_javob.Activities;

import android.os.Bundle;
import android.view.View;
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
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.MyD.UserListAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchUsers extends AppCompatActivity {
    ImageView back,search;
    TextView noResults;
    EditText searchField;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    CollectionReference collection = FirebaseFirestore.getInstance().collection(Keys.users);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        initState();
        back.setOnClickListener(v -> onBackPressed());
        search.setOnClickListener(v -> {
            String text = searchField.getText().toString();
            if(!text.isEmpty()){
                viewLoading();
                collection.addSnapshotListener((value, error) -> {
                    if (value != null) {
                        ArrayList<User> users = new ArrayList<>();
                        List<DocumentSnapshot> list = value.getDocuments();
                        for(DocumentSnapshot ds : list){
                            if((Objects.requireNonNull(ds.get(Keys.name)).toString()+ Objects.requireNonNull(ds.get(Keys.surname)).toString()).contains(text)){
                                users.add(new User(
                                        ds.get(Keys.name).toString(),
                                        ds.get(Keys.surname).toString(),
                                        ds.get(Keys.imageUrl).toString(),
                                        ds.get(Keys.seen).toString(),
                                        ds.get(Keys.number).toString(),
                                        ds.get(Keys.uId).toString(),
                                        ds.get(Keys.numberOfMyPublishedQuestions).toString(),
                                        ds.get(Keys.numberOfMyAnswers).toString(),
                                        ds.get(Keys.numberOfCorrectAnswers).toString(),
                                        ds.get(Keys.numberOfIncorrectAnswers).toString()
                                        )
                                );
                            }
                        }
                        if(users.isEmpty()) noResultsFound(); else {
                            setAdapterToRecyclerView(users);
                            viewResults();
                        }
                    }else Hey.showUnknownError(this);
                });
            }
        });
    }

    private void setAdapterToRecyclerView(ArrayList<User> users) {
        UserListAdapter adapter = new UserListAdapter(users,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initState() {
        noResults = findViewById(R.id.no_results);
        searchField = findViewById(R.id.search_users_field);
        recyclerView = findViewById(R.id.search_results);
        progressBar = findViewById(R.id.loading);
        back = findViewById(R.id.back);
        search = findViewById(R.id.search);
    }

    private void viewLoading(){
        noResults.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void viewResults(){
        noResults.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void noResultsFound(){
        noResults.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }
}