package com.normurodov_nazar.adminapp.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.normurodov_nazar.adminapp.Activities.QuestionChat;
import com.normurodov_nazar.adminapp.Activities.QuestionFilter;
import com.normurodov_nazar.adminapp.MFunctions.Hey;
import com.normurodov_nazar.adminapp.MFunctions.Keys;
import com.normurodov_nazar.adminapp.MFunctions.My;
import com.normurodov_nazar.adminapp.MyD.Question;
import com.normurodov_nazar.adminapp.MyD.QuestionAdapter;
import com.normurodov_nazar.adminapp.MyD.QuestionFrom;
import com.normurodov_nazar.adminapp.R;

import java.util.ArrayList;
import java.util.Collections;

public class NeedQuestions extends Fragment {

    RecyclerView recyclerView;
    TextView text;
    ProgressBar progressBar;
    ImageView filter,clearFilter;

    ActivityResultLauncher<Intent> launcher;
    final CollectionReference needQuestions = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.needQuestions);
    Query query;
    String theme = "";
    boolean correct = true,descending = true,before = true;
    long time = Hey.getCurrentTime();
    int loadCount = 6;
    DocumentSnapshot limit;

    QuestionAdapter adapter;
    public NeedQuestions() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_need_questions, container, false);
        init(v);
        loadMyLastQuestions();
        return v;
    }

    private void loadMyLastQuestions(){
        showLoading();
        query = needQuestions.orderBy(Keys.time, Query.Direction.DESCENDING).limit(loadCount);
        query.get().addOnSuccessListener(this::prepareQuestions).addOnFailureListener(this::showError);
    }

    void prepareQuestions(QuerySnapshot queryDocumentSnapshots){
        ArrayList<Question> questions = new ArrayList<>();
        for (DocumentSnapshot s : queryDocumentSnapshots.getDocuments()) {
            questions.add(Question.fromDoc(s));
        }
        if (questions.size()==0) showNoQuestions(); else {
            limit = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.getDocuments().size()-1);
            showQuestions(questions,true);
        }
    }

    void prepareQuestions(ArrayList<Question> questions){
        if (questions.size()==0) showNoQuestions(); else {
            showQuestions(questions,false);
        }
    }


    private void showQuestions(ArrayList<Question> questions,boolean moreFunction) {
        adapter = new QuestionAdapter(getContext(), questions, QuestionFrom.needQuestion, moreFunction, (position, name, loadMore) -> {
            if (position != -1) {
                Intent i = new Intent(getContext(), QuestionChat.class);
                i.putExtra(Keys.id, questions.get(position).getQuestionId());
                i.putExtra(Keys.theme, questions.get(position).getTheme());
                startActivity(i);
            } else {
                query.startAfter(limit).limit(loadCount).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Question> newQ = new ArrayList<>();
                    for (DocumentSnapshot q : queryDocumentSnapshots.getDocuments())
                        newQ.add(Question.fromDoc(q));
                    if (newQ.isEmpty()) {
                        Hey.showToast(getContext(), getString(R.string.noMoreQ));
                        loadMore.setVisibility(View.GONE);
                    } else {
                        limit = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.getDocuments().size() - 1);
                        adapter.addItems(newQ);
                        Hey.setButtonAsDefault(getContext(), loadMore, getString(R.string.loadMore));
                    }
                }).addOnFailureListener(this::showError);
            }
        }, (position, name, button) -> Hey.showPopupMenu(getContext(), progressBar, new ArrayList<>(Collections.singletonList(getString(R.string.delete))), (position1, name1) -> Hey.deleteQuestion(getContext(),questions.get(position)),true));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        showResult();
    }

    private void init(View v) {
        clearFilter = v.findViewById(R.id.clearFilterNeed);
        clearFilter.setOnClickListener(a->{
            loadCount = 5;
            loadMyLastQuestions();
        });
        filter = v.findViewById(R.id.filterNeed);
        filter.setOnClickListener(a->{
            Intent i = new Intent(getContext(), QuestionFilter.class);
            i.putExtra(Keys.theme,theme);
            i.putExtra(Keys.correct,correct);
            i.putExtra(Keys.order,descending);
            i.putExtra(Keys.divider,before);
            i.putExtra(Keys.time,time);
            i.putExtra(Keys.hidden,true);
            launcher.launch(i);
        });
        progressBar = v.findViewById(R.id.progressBarNeed);
        text = v.findViewById(R.id.noQuestionsNeed);text.setVisibility(View.INVISIBLE);
        recyclerView = v.findViewById(R.id.questionsNeed);
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::onResult
        );
    }

    private void onResult(ActivityResult result){
        if (result.getResultCode()==RESULT_OK){
            Intent data = result.getData();
            assert data != null;
            theme = data.getStringExtra(Keys.theme);
            correct = data.getBooleanExtra(Keys.status,true);
            descending = data.getBooleanExtra(Keys.order,true);
            before = data.getBooleanExtra(Keys.divider,true);
            time = data.getLongExtra(Keys.time,Hey.getCurrentTime());
            showLoading();
            if (before) query = needQuestions.whereLessThanOrEqualTo(Keys.time,time).orderBy(Keys.time,descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING); else
                query = needQuestions.whereGreaterThanOrEqualTo(Keys.time,time).orderBy(Keys.time,descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);
            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                ArrayList<Question> questions = new ArrayList<>();
                for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                    Question q = Question.fromDoc(d);
                    String actual = q.getTheme().replace(Keys.incorrect,"").replace(Keys.correct,"");
                    if ((theme).equals(actual)) questions.add(q);
                }
                prepareQuestions(questions);
            }).addOnFailureListener(this::showError);
        }
    }

    private void showResult() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        text.setVisibility(View.INVISIBLE);
    }
    private void showNoQuestions() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        text.setVisibility(View.VISIBLE);
    }
    private void showLoading() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        text.setVisibility(View.INVISIBLE);
    }
    private void showError(Exception e) {
        Hey.showAlertDialog(getContext(),getString(R.string.error)+":"+e.getLocalizedMessage());
    }

}