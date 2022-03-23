package com.normurodov_nazar.savol_javob.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.QuestionAdapter;
import com.normurodov_nazar.savol_javob.MyD.QuestionFrom;
import com.normurodov_nazar.savol_javob.R;
import com.normurodov_nazar.savol_javob.databinding.ActivitySearchQuestionsBinding;

import java.util.ArrayList;

public class SearchQuestions extends AppCompatActivity {
    final int loadCount = 6;

    QuestionAdapter adapter;
    Query query;
    ActivityResultLauncher<Intent> launcher;
    final CollectionReference allQuestions = FirebaseFirestore.getInstance().collection(Keys.allQuestions);

    String theme = "";
    boolean correct = true, descending = true, before = true;
    long time = Hey.getCurrentTime();
    DocumentSnapshot limit;

    private ActivitySearchQuestionsBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySearchQuestionsBinding.inflate(getLayoutInflater());
        View v = b.getRoot();
        setContentView(v);
        initVars();
        loadLastQuestions();
    }

    private void loadLastQuestions() {
        showLoading();
        query = allQuestions.orderBy(Keys.time, Query.Direction.DESCENDING).limit(loadCount);
        query.get().addOnSuccessListener(this::prepareQuestions).addOnFailureListener(this::showError);
    }

    private void initVars() {
        b.searchById.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Editable bx = b.searchById.getText();
                if (bx != null)
                    b.filter.setImageResource(bx.toString().isEmpty() ? R.drawable.ic_filter : R.drawable.search_glass);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        b.back.setOnClickListener(v -> finish());
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
        b.filter.setOnClickListener(view -> {
            Editable d = b.searchById.getText();
            if (d != null) if (d.toString().isEmpty()) {
                Intent i = new Intent(this, QuestionFilter.class);
                i.putExtra(Keys.theme, theme);
                i.putExtra(Keys.correct, correct);
                i.putExtra(Keys.order, descending);
                i.putExtra(Keys.divider, before);
                i.putExtra(Keys.time, time);
                launcher.launch(i);
            } else {
                showLoading();
                Hey.getDocument(this, allQuestions.document(d.toString()), doc -> {
                    DocumentSnapshot document = (DocumentSnapshot) doc;
                    if (document.exists()) {
                        ArrayList<Question> questions = new ArrayList<>();
                        questions.add(Question.fromDoc(document));
                        showQuestions(questions, false);
                    } else showNoQuestions();
                }, errorMessage -> {
                });
            }
        });

    }

    private void onResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            theme = data.getStringExtra(Keys.theme);
            correct = data.getBooleanExtra(Keys.status, true);
            descending = data.getBooleanExtra(Keys.order, true);
            before = data.getBooleanExtra(Keys.divider, true);
            time = data.getLongExtra(Keys.time, Hey.getCurrentTime());
            showLoading();
            if (before) {
                query = allQuestions
                        .whereEqualTo(Keys.theme, theme + (correct ? Keys.correct : Keys.incorrect))
                        .whereLessThan(Keys.time, time)
                        .orderBy(Keys.time, descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);
            } else {
                query = allQuestions
                        .whereEqualTo(Keys.theme, theme + (correct ? Keys.correct : Keys.incorrect))
                        .whereGreaterThan(Keys.time, time)
                        .orderBy(Keys.time, descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);
            }
            query.limit(loadCount).get().addOnSuccessListener(this::prepareQuestions).addOnFailureListener(this::showError);
        }
    }

    void prepareQuestions(QuerySnapshot queryDocumentSnapshots) {
        ArrayList<Question> questions = new ArrayList<>();
        for (DocumentSnapshot s : queryDocumentSnapshots.getDocuments()) {
            questions.add(Question.fromDoc(s));
        }
        if (questions.isEmpty()) showNoQuestions();
        else {
            limit = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.getDocuments().size()-1);
            showQuestions(questions, true);
        }
    }

    private void showQuestions(ArrayList<Question> questions, boolean moreFunc) {
        adapter = new QuestionAdapter(this, questions, QuestionFrom.searchQuestion, moreFunc, (position, name, loadMore) -> {
            if (position != -1) {
                Intent i = new Intent(this, QuestionChat.class);
                i.putExtra(Keys.id, questions.get(position).getQuestionId());
                i.putExtra(Keys.theme, questions.get(position).getTheme());
                startActivity(i);
            } else {
                query.startAfter(limit).limit(loadCount).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Question> newQ = new ArrayList<>();
                    for (DocumentSnapshot s : queryDocumentSnapshots.getDocuments()) newQ.add(Question.fromDoc(s));
                    if (newQ.isEmpty()) {
                        Hey.showToast(this, getString(R.string.noMoreQ));
                        loadMore.setVisibility(View.GONE);
                    } else {
                        limit = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.getDocuments().size()-1);
                        adapter.addItems(newQ);
                        Hey.setButtonAsDefault(this, loadMore, getString(R.string.loadMore));
                    }
                }).addOnFailureListener(this::showError);
            }
        });
        b.recycler.setAdapter(adapter);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        showResult();
    }

    private void showResult() {
        b.recycler.setVisibility(View.VISIBLE);
        b.bar.setVisibility(View.INVISIBLE);
        b.noResult.setVisibility(View.INVISIBLE);
    }

    private void showNoQuestions() {
        b.recycler.setVisibility(View.INVISIBLE);
        b.bar.setVisibility(View.INVISIBLE);
        b.noResult.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        b.recycler.setVisibility(View.INVISIBLE);
        b.bar.setVisibility(View.VISIBLE);
        b.noResult.setVisibility(View.INVISIBLE);
    }

    private void showError(Exception e) {
        Hey.showAlertDialog(this, getString(R.string.error) + ":" + e.getLocalizedMessage());
    }
}