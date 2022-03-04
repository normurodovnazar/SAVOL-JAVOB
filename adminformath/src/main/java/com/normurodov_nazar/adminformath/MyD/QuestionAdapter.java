package com.normurodov_nazar.adminformath.MyD;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QuestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final Context context;
    final ArrayList<Question> questions;
    final ItemClickForQuestion listener;
    final boolean loadMoreFunction;
    final QuestionFrom from;

    public QuestionAdapter(Context context, ArrayList<Question> questions, QuestionFrom from,boolean loadMoreFunction, ItemClickForQuestion listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
        this.from = from;
        this.loadMoreFunction = loadMoreFunction;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.question_layout, parent, false);
            return new QViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.load_more_button, parent, false);
            return new BViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (loadMoreFunction) {
            if (position != questions.size()) {
                Question question = questions.get(position);
                ((QViewHolder) holder).setQuestion(question, listener, position,from);
            } else ((BViewHolder) holder).setData(listener);
        } else {
            Question question = questions.get(position);
            ((QViewHolder) holder).setQuestion(question, listener, position,from);
        }
    }

    @Override
    public int getItemCount() {
        return (loadMoreFunction) ? questions.size() + 1 : questions.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (loadMoreFunction) ? position == questions.size() ? 1 : 0 : 0;
    }

    public void addItems(ArrayList<Question> questions) {
        this.questions.addAll(questions);
        notifyItemRangeInserted(this.questions.size(), questions.size());
    }

    static class QViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout form;
        final TextView sender;
        final TextView time;
        final TextView theme;
        final TextView whoNeed;
        final TextView visibleTime;
        final Button iNeed;
        final Button makePublic;
        final Context context;
        Long n;
        Boolean need = null;
        DocumentReference myIdInUsers, questionDoc, referenceInMyNeedQuestions;
        CollectionReference userList;
        Question question;
        boolean loading = true;
        String text = "";

        public QViewHolder(@NonNull View itemView) {
            super(itemView);
            form = itemView.findViewById(R.id.formQ);
            sender = itemView.findViewById(R.id.sender);
            time = itemView.findViewById(R.id.time);
            theme = itemView.findViewById(R.id.theme);
            makePublic = itemView.findViewById(R.id.makePublic);
            iNeed = itemView.findViewById(R.id.iNeed);
            visibleTime = itemView.findViewById(R.id.visibleTime);
            context = itemView.getContext();
            whoNeed = itemView.findViewById(R.id.whoNeed);
        }

        void setQuestion(Question question, ItemClickForQuestion listener, int pos,QuestionFrom from) {
            if (from==QuestionFrom.myQuestion) sender.setVisibility(View.GONE); else setName(String.valueOf(question.getSender()));
            if (from!=QuestionFrom.publicQuestion) visibleTime.setVisibility(View.GONE);
            form.setVisibility(View.VISIBLE);
            this.question = question;
            itemView.setOnClickListener(v -> listener.onItemClick(pos, "", null));
            String a = context.getString(R.string.time) + Hey.getTimeText(context, question.getTime());
            time.setText(a);
            String b = context.getString(R.string.visibleTime) + Hey.getTimeText(context, question.getVisibleTime());
            visibleTime.setText(b);
            String t = question.getTheme(), x = t.contains(Keys.incorrect) ? t.replace(Keys.incorrect, "") : t.replace(Keys.correct, "");
            String c = context.getString(R.string.theme) + x;
            theme.setText(c);
            questionDoc = FirebaseFirestore.getInstance().collection(Keys.allQuestions).document(question.getQuestionId());
            userList = questionDoc.collection(Keys.users);
            myIdInUsers = userList.document(String.valueOf(My.id));
            referenceInMyNeedQuestions = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.needQuestions).document(question.getQuestionId());
            if (from==QuestionFrom.myQuestion || from==QuestionFrom.needQuestion || from==QuestionFrom.searchQuestion) makePublic.setVisibility(View.VISIBLE);
            makePublic.setOnClickListener(v->makingPublic());
            if (question.getSender() != My.id) {
                whoNeed.setVisibility(View.GONE);
                iNeed.setVisibility(View.VISIBLE);
                setAsLoading();
                Hey.addDocumentListener(itemView.getContext(), questionDoc, doc -> {
                    Long N = doc.getLong(Keys.number);
                    n = N == null ? 0 : N;
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
                    whoNeed.setText(context.getString(R.string.needsUsers).replace("xxx", String.valueOf(n)));
                }, errorMessage -> setAsNotLoading());
            }
        }

        private void makingPublic() {
            Hey.showAlertDialog(context,context.getString(R.string.chooseHowManyDays)).setOnDismissListener(x->{
                ArrayList<String> days = new ArrayList<>();
                for (int i=1;i<=9;i++) {
                    if (i==1) days.add(i+" "+context.getString(R.string.dayVisible)); else days.add(i+" "+context.getString(R.string.daysVisible));
                }
                int[] d = new int[1];
                d[0] = -1;
                Hey.showPopupMenu(context, makePublic, days, (position, name) -> d[0] = position+1,true).setOnDismissListener(xa->{
                    if (d[0]!=-1){
                        loadingPublic();
                        LoadingDialog loadingDialog = Hey.showLoadingDialog(context);
                        Map<String,Object> data = question.toMap();
                        long time = Hey.getCurrentTime();
                        data.put(Keys.visibleTime,time + 24L * 60 * 60 * 1000 * d[0]);
                        Hey.publishQuestion(context, new Question(data), d[0], null, true, doc -> {
                            notLoadingPublic();
                            loadingDialog.closeDialog();
                            Hey.showToast(context,context.getString(R.string.posted));
                        }, errorMessage -> {
                            notLoadingPublic();
                            loadingDialog.closeDialog();
                        });
                    }
                });
            });
        }

        private void loadingPublic() {
            loading = true;
            Hey.setButtonAsLoading(context,makePublic);
        }

        private void notLoadingPublic() {
            loading = false;
            Hey.setButtonAsDefault(context,makePublic,context.getString(R.string.makePublic));
        }

        private void onPress() {
            if (!loading) {
                if (need){
                    MyDialogWithTwoButtons dialog = Hey.showDeleteDialog(context,context.getString(R.string.confirmDeleteFromINeed),null,false);
                    dialog.setOnDismissListener(dialogInterface -> {
                        if (dialog.getResult()) changeVal();
                    });
                } else changeVal();
            } else Hey.showToast(context, context.getString(R.string.wait));
        }

        private void changeVal(){
            Hey.amIOnline(new StatusListener() {
                @Override
                public void online() {
                    if (need != null)
                        if (need) {
                            setAsLoading();
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(Keys.topics + question.getQuestionId()).addOnSuccessListener(unused -> { }).addOnFailureListener(e -> { });
                            setAsUnselected();
                            referenceInMyNeedQuestions.delete();
                            myIdInUsers.delete();
                            questionDoc.set(Collections.singletonMap(Keys.number, n - 1), SetOptions.merge());
                        } else {
                            FirebaseMessaging.getInstance().subscribeToTopic(Keys.topics + question.getQuestionId()).addOnSuccessListener(unused -> { }).addOnFailureListener(e -> { });
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
        }

        void setAsSelected() {
            setAsNotLoading();
            iNeed.setBackgroundResource(R.drawable.button_bg_pressed);
            iNeed.setTextColor(context.getResources().getColor(R.color.white));
            iNeed.setText(text);
        }

        void setAsUnselected() {
            setAsNotLoading();
            iNeed.setBackgroundResource(R.drawable.button_background);
            iNeed.setTextColor(context.getResources().getColor(R.color.black));
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
                String x = context.getString(R.string.sender) + (!user.isHiddenFromQuestionChat() ? user.getFullName() : context.getString(R.string.hidden));
                sender.setText(x);
            }, errorMessage -> {

            });
        }
    }

    static class BViewHolder extends RecyclerView.ViewHolder {
        final Button loadMore;

        public BViewHolder(@NonNull View itemView) {
            super(itemView);
            loadMore = itemView.findViewById(R.id.loadMore);
        }

        void setData(ItemClickForQuestion listener) {
            loadMore.setOnClickListener(view -> {
                Hey.setButtonAsLoading(itemView.getContext(), loadMore);
                listener.onItemClick(-1, "", loadMore);
            });
        }
    }
}
