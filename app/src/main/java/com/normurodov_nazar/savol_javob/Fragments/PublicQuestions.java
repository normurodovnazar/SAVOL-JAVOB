package com.normurodov_nazar.savol_javob.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.normurodov_nazar.savol_javob.Activities.QuestionChat;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.QuestionAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class PublicQuestions extends Fragment {

    RecyclerView recyclerView;
    TextView text;
    ProgressBar progressBar;
    ArrayList<Question> questions = new ArrayList<>();
    ListenerRegistration registration;

    public PublicQuestions() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_public_questions, container, false);
        init(v);
        registration = Hey.setCollectionListener(getContext(), FirebaseFirestore.getInstance().collection(Keys.publicQuestions), docs -> {
            questions.clear();
            for (DocumentSnapshot doc : docs) {
                Question question = Question.fromDoc(doc);
                if (question.getVisibleTime()<=Hey.getCurrentTime()) {
                    FirebaseFirestore.getInstance().collection(Keys.publicQuestions).document(question.getQuestionId()).delete();
                } else questions.add(question);
            }
            Hey.print("questions",questions.toString());
            if(questions.size()==0) showNoQuestions(); else showQuestions();
        }, errorMessage -> {

        });
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    private void showQuestions() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        text.setVisibility(View.INVISIBLE);
        QuestionAdapter adapter = new QuestionAdapter(getContext(), questions, (position, name) -> {
            Intent i = new Intent(getContext(),QuestionChat.class);
            i.putExtra(Keys.id,questions.get(position).getQuestionId());
            i.putExtra(Keys.theme,questions.get(position).getTheme());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void showNoQuestions() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.INVISIBLE);
        text.setVisibility(View.VISIBLE);
    }


    private void init(View v) {
        progressBar = v.findViewById(R.id.progressBarP);
        text = v.findViewById(R.id.noQuestions);text.setVisibility(View.INVISIBLE);
        recyclerView = v.findViewById(R.id.questions);
    }
}