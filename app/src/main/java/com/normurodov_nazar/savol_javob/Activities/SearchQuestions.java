package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.QuestionAdapter;
import com.normurodov_nazar.savol_javob.MyD.QuestionFrom;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchQuestions extends AppCompatActivity {
    TextView noResult;
    RecyclerView recyclerView;
    ProgressBar bar;
    ImageView back,filter;
    TextInputEditText byId;

    int loadCount = 5;

    QuestionAdapter adapter;
    Query query;
    ActivityResultLauncher<Intent> launcher;
    CollectionReference allQuestions = FirebaseFirestore.getInstance().collection(Keys.allQuestions);

    String theme = "";
    boolean correct = true,descending = true,before = true;
    long time = Hey.getCurrentTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_questions);
        initVars();
        loadLastQuestions();
    }

    private void loadLastQuestions() {
        showLoading();
        query = allQuestions.orderBy(Keys.time, Query.Direction.DESCENDING).limit(loadCount);
        query.get().addOnSuccessListener(this::prepareQuestions).addOnFailureListener(this::showError);
    }

    private void initVars() {
        byId = findViewById(R.id.searchById);byId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Editable b = byId.getText();
                if (b!=null) filter.setImageResource(b.toString().isEmpty() ? R.drawable.ic_filter : R.drawable.search_glass);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        noResult = findViewById(R.id.no_resultsQ);
        recyclerView = findViewById(R.id.searchResultsQ);
        bar = findViewById(R.id.barQ);
        back = findViewById(R.id.backQ);back.setOnClickListener(v -> finish());
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
        filter = findViewById(R.id.filter);filter.setOnClickListener(view -> {
            Editable d = byId.getText();
            if (d!=null) if (d.toString().isEmpty()){
                Intent i = new Intent(this,QuestionFilter.class);
                i.putExtra(Keys.theme,theme);
                i.putExtra(Keys.correct,correct);
                i.putExtra(Keys.order,descending);
                i.putExtra(Keys.divider,before);
                i.putExtra(Keys.time,time);
                launcher.launch(i);
            }else {
                showLoading();
                Hey.getDocument(this, allQuestions.document(d.toString()), doc -> {
                    DocumentSnapshot document = (DocumentSnapshot) doc;
                    if (document.exists()) {
                        ArrayList<Question> questions = new ArrayList<>();questions.add(Question.fromDoc(document));
                        showQuestions(questions,false);
                    }else showNoQuestions();
                }, errorMessage -> {});
            }
        });

    }

    private void onResult(ActivityResult result) {
        if (result.getResultCode()==RESULT_OK){
            Intent data = result.getData();
            assert data != null;
            theme = data.getStringExtra(Keys.theme);
            correct = data.getBooleanExtra(Keys.status,true);
            descending = data.getBooleanExtra(Keys.order,true);
            before = data.getBooleanExtra(Keys.divider,true);
            time = data.getLongExtra(Keys.time,Hey.getCurrentTime());
            showLoading();
            if (before){
                query = allQuestions
                        .whereEqualTo(Keys.theme,theme+(correct ? Keys.correct : Keys.incorrect))
                        .whereLessThan(Keys.time,time)
                        .orderBy(Keys.time,descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);
            }else {
                query = allQuestions
                        .whereEqualTo(Keys.theme,theme+(correct ? Keys.correct : Keys.incorrect))
                        .whereGreaterThan(Keys.time,time)
                        .orderBy(Keys.time,descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);
            }
            query.limit(5).get().addOnSuccessListener(this::prepareQuestions).addOnFailureListener(this::showError);
        }
    }

    void prepareQuestions(QuerySnapshot queryDocumentSnapshots){
        ArrayList<Question> questions = new ArrayList<>();
        for (DocumentSnapshot s : queryDocumentSnapshots.getDocuments()) {
            questions.add(Question.fromDoc(s));
        }
        if (questions.size()==0) showNoQuestions(); else showQuestions(questions,true);
    }

    private void showQuestions(ArrayList<Question> questions,boolean moreFunc) {
        adapter = new QuestionAdapter(this, questions, QuestionFrom.searchQuestion,moreFunc, (position, name, loadMore) -> {
            if (position!=-1){
                Intent i = new Intent(this,QuestionChat.class);
                i.putExtra(Keys.id,questions.get(position).getQuestionId());
                i.putExtra(Keys.theme,questions.get(position).getTheme());
                startActivity(i);
            }else {
                query.limit(loadCount+5).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    loadCount+=5;
                    ArrayList<Question> newQ = new ArrayList<>();
                    for (DocumentSnapshot s : queryDocumentSnapshots.getDocuments()) {
                        newQ.add(Question.fromDoc(s));
                    }
                    if (Hey.getDifferenceOfQuestions(newQ,questions).size()==0) Hey.showToast(this,getString(R.string.noMoreQ));
                    adapter.addItems(Hey.getDifferenceOfQuestions(newQ,questions));
                    Hey.setButtonAsDefault(this,loadMore,getString(R.string.loadMore));
                }).addOnFailureListener(this::showError);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showResult();
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