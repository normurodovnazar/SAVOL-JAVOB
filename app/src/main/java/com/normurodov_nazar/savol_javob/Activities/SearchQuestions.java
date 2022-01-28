package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.QuestionAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class SearchQuestions extends AppCompatActivity {
    TextView noResult;
    RecyclerView recyclerView;
    ProgressBar bar;
    ImageView back,filter;

    ActivityResultLauncher<Intent> launcher;
    CollectionReference allQuestions = FirebaseFirestore.getInstance().collection(Keys.allQuestions);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_questions);
        initVars();
        loadLastQuestions();
    }

    private void loadLastQuestions() {
        showLoading();
        allQuestions.orderBy(Keys.time, Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(this::prepareQuestions).addOnFailureListener(this::showError);
    }

    private void showQuestions(ArrayList<Question> questions) {
        QuestionAdapter adapter = new QuestionAdapter(this, questions, (position, name) -> {
            Intent i = new Intent(this,QuestionChat.class);
            i.putExtra(Keys.id,questions.get(position).getQuestionId());
            i.putExtra(Keys.theme,questions.get(position).getTheme());
            startActivity(new Intent());
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showResult();
    }
    private void initVars() {
        noResult = findViewById(R.id.no_resultsQ);
        recyclerView = findViewById(R.id.searchResultsQ);
        bar = findViewById(R.id.barQ);
        back = findViewById(R.id.backQ);back.setOnClickListener(v -> finish());
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
        filter = findViewById(R.id.filter);filter.setOnClickListener(view -> launcher.launch(new Intent(this,QuestionFilter.class)));

    }

    private void onResult(ActivityResult result) {
        if (result.getResultCode()==RESULT_OK){
            Intent data = result.getData();
            assert data != null;
            String theme = data.getStringExtra(Keys.theme);
            boolean correct = data.getBooleanExtra(Keys.status,true), descending = data.getBooleanExtra(Keys.order,true),
            before = data.getBooleanExtra(Keys.divider,true);
            long time = data.getLongExtra(Keys.time,Hey.getCurrentTime());
            showLoading();
            Query q = allQuestions.whereEqualTo(Keys.theme,theme+(correct ? Keys.correct : Keys.incorrect)).orderBy(Keys.time,descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);
            if (before){
                q.whereLessThan(Keys.time,time).get().addOnSuccessListener(this::prepareQuestions).addOnFailureListener(this::showError);
            }else {
                q.whereGreaterThan(Keys.time,time).get().addOnSuccessListener(this::prepareQuestions).addOnFailureListener(this::showError);
            }
        }
    }

    void prepareQuestions(QuerySnapshot queryDocumentSnapshots){
        ArrayList<Question> questions = new ArrayList<>();
        for (DocumentSnapshot s : queryDocumentSnapshots.getDocuments()) {
            Hey.print("q",Question.fromDoc(s).toMap().toString());
            questions.add(Question.fromDoc(s));
        }
        if (questions.size()==0) showNoQuestions(); else showQuestions(questions);
    }

    private void showResult() {
        recyclerView.setVisibility(View.VISIBLE);
        bar.setVisibility(View.INVISIBLE);
        noResult.setVisibility(View.INVISIBLE);
    }
    private void showNoQuestions() {
        recyclerView.setVisibility(View.INVISIBLE);
        bar.setVisibility(View.INVISIBLE);
        noResult.setVisibility(View.VISIBLE);
    }
    private void showLoading() {
        recyclerView.setVisibility(View.INVISIBLE);
        bar.setVisibility(View.VISIBLE);
        noResult.setVisibility(View.INVISIBLE);
    }
    private void showError(Exception e) {
        Hey.showAlertDialog(this,getString(R.string.error)+":"+e.getLocalizedMessage());
    }
}