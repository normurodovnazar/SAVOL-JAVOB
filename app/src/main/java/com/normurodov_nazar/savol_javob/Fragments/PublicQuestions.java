package com.normurodov_nazar.savol_javob.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.ItemClickListener;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.QuestionAdapter;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class PublicQuestions extends Fragment {

    RecyclerView recyclerView;
    TextView text;
    ArrayList<Question> questions = new ArrayList<>();

    public PublicQuestions() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_public_questions, container, false);
        init(v);
        Hey.collectionListener(getContext(), FirebaseFirestore.getInstance().collection(Keys.publicQuestions), docs -> {
            questions.clear();
            for (DocumentSnapshot doc : docs) {
                questions.add(Question.fromDoc(doc));
            }
            if(questions.size()==0) showNoQuestions(); else showQuestions();
        }, errorMessage -> {

        });
        return v;
    }

    private void showQuestions() {
        recyclerView.setVisibility(View.VISIBLE);
        text.setVisibility(View.INVISIBLE);
        QuestionAdapter adapter = new QuestionAdapter(getContext(), questions, (position, name) -> {

        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void showNoQuestions() {
        recyclerView.setVisibility(View.INVISIBLE);
        text.setVisibility(View.VISIBLE);
    }


    private void init(View v) {
        text = v.findViewById(R.id.noQuestions);
        recyclerView = v.findViewById(R.id.questions);
    }


}