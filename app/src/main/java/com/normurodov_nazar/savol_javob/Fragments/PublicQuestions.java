package com.normurodov_nazar.savol_javob.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.normurodov_nazar.savol_javob.Activities.QuestionChat;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.QuestionAdapter;
import com.normurodov_nazar.savol_javob.MyD.QuestionFrom;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Arrays;

public class PublicQuestions extends Fragment {

    ImageView lang;
    RecyclerView recyclerView;
    TextView text;
    ProgressBar progressBar;
    final ArrayList<Question> questions = new ArrayList<>();
    ListenerRegistration registration;
    Context c = null;

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
        if (c!=null)loadQuestions(getString(R.string.lang));
        return v;
    }

    private void loadQuestions(String l) {
        if (registration!=null) registration.remove();
        registration = FirebaseFirestore.getInstance().collection(Keys.publicQuestions + l).orderBy(Keys.time, Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {
            if (value != null) {
                questions.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Question question = Question.fromDoc(doc);
                    if (question.getVisibleTime() <= Hey.getCurrentTime()) {
                        FirebaseFirestore.getInstance().collection(Keys.publicQuestions + l).document(question.getQuestionId()).delete();
                    } else questions.add(question);
                }
                if (questions.size() == 0) showNoQuestions();
                else showQuestions();
            } else if (error != null)
                Hey.showAlertDialog(c, error.getLocalizedMessage());
            else Hey.showUnknownError(c);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        c = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        c = null;
    }

    private void showQuestions() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        text.setVisibility(View.INVISIBLE);
        QuestionAdapter adapter = new QuestionAdapter(c, questions, QuestionFrom.publicQuestion, false, (position, name, button) -> {
            Intent i = new Intent(c, QuestionChat.class);
            i.putExtra(Keys.id, questions.get(position).getQuestionId());
            i.putExtra(Keys.theme, questions.get(position).getTheme());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(c));
    }

    private void showNoQuestions() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.INVISIBLE);
        text.setVisibility(View.VISIBLE);
    }


    private void init(View v) {
        lang = v.findViewById(R.id.langPublic);
        lang.setOnClickListener(view -> {
            if (c!=null) {
                SharedPreferences preferences = Hey.getPreferences(c);
                if (preferences.getBoolean(Keys.first, true)) {
                    preferences.edit().putBoolean(Keys.first,false).apply();
                    Hey.showAlertDialog(c, getString(R.string.langFirst).replaceAll("xxx", getString(getString(R.string.lang).equals("eng") ? R.string.eng : R.string.uz)))
                            .setOnDismissListener(x -> Hey.showPopupMenu(c, lang, new ArrayList<>(Arrays.asList(getString(R.string.uz), getString(R.string.eng))), (position, name) -> loadQuestions(position==0 ? "uz" : "eng"), true));
                }else {
                    Hey.showPopupMenu(c, lang, new ArrayList<>(Arrays.asList(getString(R.string.uz), getString(R.string.eng))), (position, name) -> loadQuestions(position==0 ? "uz" : "eng"), true);
                }
            }
        });
        progressBar = v.findViewById(R.id.progressBarP);
        text = v.findViewById(R.id.noQuestions);
        text.setVisibility(View.INVISIBLE);
        recyclerView = v.findViewById(R.id.questions);
    }
}