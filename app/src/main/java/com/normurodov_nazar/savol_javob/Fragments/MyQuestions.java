package com.normurodov_nazar.savol_javob.Fragments;

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
import com.normurodov_nazar.savol_javob.Activities.QuestionChat;
import com.normurodov_nazar.savol_javob.Activities.QuestionFilter;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.QuestionAdapter;
import com.normurodov_nazar.savol_javob.MyD.QuestionFrom;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class MyQuestions extends Fragment {

    RecyclerView recyclerView;
    TextView text;
    ProgressBar progressBar;
    ImageView filter,clearFilter;

    ActivityResultLauncher<Intent> launcher;
    final CollectionReference myQuestions = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.allQuestions);
    Query query;
    String theme = "";
    boolean correct = true,descending = true,before = true;
    long time = Hey.getCurrentTime();
    int loadCount = 6;
    DocumentSnapshot limit;

    QuestionAdapter adapter;
    public MyQuestions() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_questions, container, false);
        initVars(v);
        loadMyLastQuestions();
        return v;
    }

    private void loadMyLastQuestions(){
        showLoading();
        query = myQuestions.orderBy(Keys.time, Query.Direction.DESCENDING).limit(loadCount);
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
        if (questions.size()==0) showNoQuestions(); else showQuestions(questions,false);
    }

    private void showQuestions(ArrayList<Question> questions,boolean moreFunction) {
        adapter = new QuestionAdapter(getContext(), questions, QuestionFrom.myQuestion,moreFunction, (position, name, loadMore) -> {
            if (position!=-1){
                Intent i = new Intent(getContext(),QuestionChat.class);
                i.putExtra(Keys.id,questions.get(position).getQuestionId());
                i.putExtra(Keys.theme,questions.get(position).getTheme());
                startActivity(i);
            }else {
                query.startAfter(limit).limit(loadCount).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Question> newQ = new ArrayList<>();
                    for(DocumentSnapshot q : queryDocumentSnapshots.getDocuments()) newQ.add(Question.fromDoc(q));
                    if (newQ.isEmpty()){
                        Hey.showToast(getContext(),getString(R.string.noMoreQ));
                        loadMore.setVisibility(View.GONE);
                    }else {
                        limit = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.getDocuments().size()-1);
                        adapter.addItems(newQ);
                        Hey.setButtonAsDefault(getContext(),loadMore,getString(R.string.loadMore));
                    }
                }).addOnFailureListener(this::showError);

            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        showResult();
    }

    private void initVars(View v) {
        clearFilter = v.findViewById(R.id.clearFilterMy);
        clearFilter.setOnClickListener(a->{
            loadCount = 1;
            loadMyLastQuestions();
        });
        filter = v.findViewById(R.id.filterMy);
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
        recyclerView = v.findViewById(R.id.questionsMy);
        text = v.findViewById(R.id.noQuestionsMy);
        progressBar = v.findViewById(R.id.progressBarMy);
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
            if (before) query = myQuestions.whereLessThanOrEqualTo(Keys.time,time).orderBy(Keys.time,descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING); else
                query = myQuestions.whereGreaterThanOrEqualTo(Keys.time,time).orderBy(Keys.time,descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);
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