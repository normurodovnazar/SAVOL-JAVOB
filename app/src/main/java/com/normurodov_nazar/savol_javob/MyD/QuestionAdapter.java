package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QViewHolder> {
    final Context context;
    final ArrayList<Question> questions;
    final ItemClickListener listener;

    public QuestionAdapter(Context context, ArrayList<Question> questions, ItemClickListener listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.question_layout, parent, false);
        return new QViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.setQuestion(question, listener, position);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout form;
        LinearLayout l;
        TextView sender, time, theme,whoNeed,visibleTime;
        Button iNeed;
        Context context;
        Long n;
        Boolean need = null;
        DocumentReference myIdInUsers, questionDoc,referenceInMyNeedQuestions;
        CollectionReference userList;
        Question question;
        boolean loading = true;
        String text = "";


        public QViewHolder(@NonNull View itemView) {
            super(itemView);
            form = itemView.findViewById(R.id.formQ);
            l = itemView.findViewById(R.id.linearQ);
            sender = itemView.findViewById(R.id.sender);
            time = itemView.findViewById(R.id.time);
            theme = itemView.findViewById(R.id.theme);
            iNeed = itemView.findViewById(R.id.iNeed);
            visibleTime = itemView.findViewById(R.id.visibleTime);
            context = itemView.getContext();
            whoNeed = itemView.findViewById(R.id.whoNeed);
        }

        void setQuestion(Question question, ItemClickListener listener, int pos) {
            if (listener!=null){
                form.setVisibility(View.VISIBLE);
                this.question = question;
                Hey.print("question", question.toMap().toString());
                itemView.setOnClickListener(v -> listener.onItemClick(pos, ""));
                setName(String.valueOf(question.getSender()));
                time.setText(time.getText().toString() + Hey.getSeenTime(context, question.getTime()));
                visibleTime.setText(visibleTime.getText().toString()+Hey.getSeenTime(context,question.getVisibleTime()));
                String t = question.getTheme(),x = t.contains(Keys.incorrect) ? t.replace(Keys.incorrect,"") : t.replace(Keys.correct,"");
                theme.setText(theme.getText().toString() + x);
                questionDoc = FirebaseFirestore.getInstance().collection(Keys.allQuestions).document(question.getQuestionId());
                userList = questionDoc.collection(Keys.users);
                myIdInUsers = userList.document(String.valueOf(My.id));
                referenceInMyNeedQuestions = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.needQuestions).document(question.getQuestionId());
                if (question.getSender()!=My.id) {
                    whoNeed.setVisibility(View.GONE);
                    iNeed.setVisibility(View.VISIBLE);
                    setAsLoading();
                    Hey.addDocumentListener(itemView.getContext(), questionDoc, doc -> {
                        Long N = doc.getLong(Keys.number);
                        n = N == null ? 0 : N;
                        Hey.print("n", String.valueOf(n));
                        text = context.getText(R.string.need) + "(" + n + ")";
                        setAsNotLoading();
                        if (need != null)
                            if (need) setAsSelected();
                            else setAsUnselected();
                    }, errorMessage -> setAsNotLoading());

                    Hey.addDocumentListener(context, myIdInUsers, doc -> {
                        if (doc.exists()) {
                            setAsSelected();
                            need = true;
                        } else {
                            setAsUnselected();
                            need = false;
                        }
                    }, errorMessage -> setAsNotLoading());

                    iNeed.setOnClickListener(v -> onPress());
                } else {
                    iNeed.setVisibility(View.GONE);
                    Hey.addDocumentListener(itemView.getContext(), questionDoc, doc -> {
                        Long N = doc.getLong(Keys.number);
                        n = N == null ? 0 : N;
                        Hey.print("n", String.valueOf(n));
                        whoNeed.setText(context.getString(R.string.needsUsers).replace("xxx",String.valueOf(n)));
                    }, errorMessage -> setAsNotLoading());
                }
            } else l.setVisibility(View.VISIBLE);
        }

        private void onPress() {
            if (!loading) {
                setAsLoading();
                Hey.amIOnline(new StatusListener() {
                    @Override
                    public void online() {
                        if (need != null)
                            if (need) {
                                setAsUnselected();
                                referenceInMyNeedQuestions.delete();
                                myIdInUsers.delete();
                                questionDoc.set(Collections.singletonMap(Keys.number, n - 1), SetOptions.merge());
                            } else {
                                setAsSelected();
                                referenceInMyNeedQuestions.set(question.toMap());
                                myIdInUsers.set(new HashMap<>());
                                questionDoc.set(Collections.singletonMap(Keys.number, n + 1), SetOptions.merge());
                            }
                        else Hey.showToast(context, context.getString(R.string.error_connection));
                    }

                    @Override
                    public void offline() {
                        Hey.showToast(context, context.getString(R.string.error_connection));
                        setAsNotLoading();
                    }
                }, errorMessage -> setAsNotLoading(), context);
            } else Hey.showToast(context, context.getString(R.string.wait));
        }

        void setAsSelected() {
            setAsNotLoading();
            iNeed.setBackgroundResource(R.drawable.button_bg_pressed);
            iNeed.setTextColor(Color.WHITE);
            iNeed.setText(text);
        }

        void setAsUnselected() {
            setAsNotLoading();
            iNeed.setBackgroundResource(R.drawable.sss);
            iNeed.setTextColor(Color.BLACK);
            iNeed.setText(text);
        }

        void setAsLoading() {
            loading = true;
            Hey.setButtonAsLoading(context.getApplicationContext(), iNeed);
        }

        void setAsNotLoading() {
            loading = false;
            Hey.setButtonAsDefault(context.getApplicationContext(), iNeed, text);
        }

        private void setName(String docId) {
            Hey.getUserFromUserId(itemView.getContext(), docId, doc -> {
                User user = (User) doc;
                sender.setText(sender.getText().toString() + user.getFullName());
            }, errorMessage -> {

            });
        }
    }
}
