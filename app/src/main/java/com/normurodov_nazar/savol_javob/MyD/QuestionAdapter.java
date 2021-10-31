package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

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
        View view = LayoutInflater.from(context).inflate(R.layout.question_layout,parent,false);
        return new QViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QViewHolder holder, int position) {
        holder.setQuestion(questions.get(position),listener,position);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QViewHolder extends RecyclerView.ViewHolder{
        TextView sender,time,subject,theme,number;
        Context context;

        public QViewHolder(@NonNull View itemView) {
            super(itemView);
            sender = itemView.findViewById(R.id.sender);
            time = itemView.findViewById(R.id.time);
            subject = itemView.findViewById(R.id.subject);
            theme = itemView.findViewById(R.id.theme);
            number = itemView.findViewById(R.id.number);
            context = itemView.getContext();
        }

        void setQuestion(Question question,ItemClickListener listener,int pos){
            itemView.setOnClickListener(v -> listener.onItemClick(pos,""));
            if(question.isHiddenUser()) sender.setText(sender.getText().toString()+context.getString(R.string.unknown)); else setName(String.valueOf(question.getSender()));
            time.setText(time.getText().toString()+Hey.getSeenTime(context,question.getTime()));
            subject.setText(subject.getText().toString()+question.getSubject());
            theme.setText(theme.getText().toString()+question.getTheme());
            number.setText(number.getText().toString()+question.getNumber());
        }

        private void setName(String docId) {
            Hey.getDocument(itemView.getContext(), FirebaseFirestore.getInstance().collection(Keys.users).document(docId), doc -> {
                sender.setText(sender.getText().toString()+User.fromDoc(doc).getFullName());
            }, errorMessage -> {

            });
        }
    }
}
