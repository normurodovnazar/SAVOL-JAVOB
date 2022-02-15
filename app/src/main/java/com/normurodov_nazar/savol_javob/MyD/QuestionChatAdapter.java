package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QuestionChatAdapter extends RecyclerView.Adapter {
    final String questionId;
    final RecyclerViewItemClickListener textClick;
    final RecyclerViewItemClickListener imageClick;
    final RecyclerViewItemClickListener profileImageClick;
    final RecyclerViewItemLongClickListener longClickListener;
    final ItemClickForQuestion loadMore;
    ArrayList<Message> messages;
    final Context context;
    final String theme;

    public QuestionChatAdapter(Context context, ArrayList<Message> messages, String questionId, RecyclerViewItemClickListener textClick, RecyclerViewItemClickListener imageClick, RecyclerViewItemClickListener profileImageClick, RecyclerViewItemLongClickListener longClickListener,ItemClickForQuestion loadMore, String theme) {
        this.questionId = questionId;
        this.textClick = textClick;
        this.messages = messages;
        this.context = context;
        this.imageClick = imageClick;
        this.profileImageClick = profileImageClick;
        this.longClickListener = longClickListener;
        this.theme = theme;
        this.loadMore = loadMore;
    }

    public void addItems(ArrayList<Message> messages, int itemCount) {
        int startPosition = this.messages.size();
        this.messages.addAll(messages);
        notifyItemRangeInserted(startPosition+2, itemCount);
    }

    public void removeItem(Message message) {
        int i = Hey.getIndexInArray(message, messages);
        if (i != -1) {
            messages.remove(i);
            notifyItemRemoved(i+1);
        }
    }

    public void addItemsToTop(ArrayList<Message> newMessages){
        messages.addAll(0,newMessages);
        notifyItemRangeInserted(1, newMessages.size());
    }

    public void changeItem(Message message, int i) {
        messages.set(i, message);
        notifyItemChanged(i+1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View loadMore = LayoutInflater.from(context).inflate(R.layout.load_more_button, parent, false);
                return new LoadMore(loadMore);
            case -1:
                View meT = LayoutInflater.from(context).inflate(R.layout.message_from_me, parent, false);
                return new TextFromMe(meT);
            case -2:
                View meI = LayoutInflater.from(context).inflate(R.layout.image_message_from_me, parent, false);
                return new ImageFromMe(meI);
            case -3:
                View meA = LayoutInflater.from(context).inflate(R.layout.answer_from_me, parent, false);
                return new AnswerFromMe(meA);
            case -4:
                View meQ = LayoutInflater.from(context).inflate(R.layout.answer_from_me, parent, false);
                return new QuestionFromMe(meQ);
            case 1:
                View otherT = LayoutInflater.from(context).inflate(R.layout.text_from_other_q, parent, false);
                return new TextFromOther(otherT);
            case 2:
                View otherI = LayoutInflater.from(context).inflate(R.layout.image_from_other_q, parent, false);
                return new ImageFromOther(otherI);
            case 3:
                View otherA = LayoutInflater.from(context).inflate(R.layout.answer_from_other, parent, false);
                return new AnswerFromOther(otherA);
            default:
                View otherQ = LayoutInflater.from(context).inflate(R.layout.answer_from_other, parent, false);
                return new QuestionFromOther(otherQ);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position!=0) {
            Message message = messages.get(position-1);
            if (message.getSender() == My.id)
                switch (message.getType()) {
                    case Keys.textMessage:
                        return -1;
                    case Keys.imageMessage:
                        return -2;
                    case Keys.answer:
                        return -3;
                    case Keys.question:
                        return -4;
                }
            else switch (message.getType()) {
                case Keys.textMessage:
                    return 1;
                case Keys.imageMessage:
                    return 2;
                case Keys.answer:
                    return 3;
                case Keys.question:
                    return 4;
            }
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position==0) {
            ((LoadMore) holder).setData(loadMore,messages.size()>=50);
        }else {
            Message data = messages.get(position-1);
            if (data.getSender() == My.id) {
                switch (data.getType()) {
                    case Keys.textMessage:
                        ((TextFromMe) holder).setData(data, textClick, position);
                        break;
                    case Keys.imageMessage:
                        ((ImageFromMe) holder).setData(data, imageClick, longClickListener, position);
                        break;
                    case Keys.answer:
                        ((AnswerFromMe) holder).setData(data, questionId, textClick, imageClick, position, String.valueOf(messages.get(0).getSender()));
                        break;
                    case Keys.question:
                        ((QuestionFromMe) holder).setData(data, textClick, imageClick, longClickListener, position);
                        break;
                }
            } else {
                switch (data.getType()) {
                    case Keys.textMessage:
                        ((TextFromOther) holder).setData(data, profileImageClick, position);
                        break;
                    case Keys.imageMessage:
                        ((ImageFromOther) holder).setData(data, imageClick, profileImageClick, position);
                        break;
                    case Keys.answer:
                        ((AnswerFromOther) holder).setData(data, questionId,theme.contains(Keys.correct) ? theme.replace(Keys.correct,"") : theme.replace(Keys.incorrect,""), imageClick, profileImageClick, position, String.valueOf(messages.get(0).getSender()));
                        break;
                    case Keys.question:
                        ((QuestionFromOther) holder).setData(data, imageClick, profileImageClick, position);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size()+1;
    }

    static class TextFromMe extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView read;

        public TextFromMe(@NonNull View itemView) {
            super(itemView);
            read = itemView.findViewById(R.id.statusOfMessage);
            message = itemView.findViewById(R.id.messageFromMe);
            time = itemView.findViewById(R.id.timeMessageFromMeInSingleChat);
        }

        void setData(Message data, RecyclerViewItemClickListener listener, int i) {
            time.setText(Hey.getSeenTime(itemView.getContext(), data.getTime()));
            message.setText(data.getMessage());
            itemView.setOnClickListener(v -> listener.onItemClick(data, itemView, i));
            read.setImageResource(data.isRead() ? R.drawable.ic_read : R.drawable.ic_unread);
        }
    }

    static class ImageFromMe extends RecyclerView.ViewHolder {
        TextView time;
        ImageView imageView, read;

        public ImageFromMe(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.timeImageFromMeFromMe);
            read = itemView.findViewById(R.id.statusOfImageMessage);
            imageView = itemView.findViewById(R.id.imageMessageByMe);
        }

        void setData(Message data, RecyclerViewItemClickListener imageClick, RecyclerViewItemLongClickListener longClickListener, int i) {
            time.setText(Hey.getSeenTime(itemView.getContext(), data.getTime()));
            Hey.workWithImageMessage(data, doc -> imageView.setImageURI(Uri.fromFile(new File(Hey.getLocalFile(data)))), errorMessage -> {});
            read.setImageResource(data.isRead() ? R.drawable.ic_read : R.drawable.ic_unread);
            itemView.setOnClickListener(v -> imageClick.onItemClick(data, itemView, i));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(data, itemView, i);
                return true;
            });
        }
    }

    static class AnswerFromMe extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView read, image;
        ConstraintLayout answerFromMe;

        Button correct, incorrect;
        DocumentReference reference, thisAnswer, numbersDocument;
        long cN, iN;
        String correctText = "?", incorrectText = "?";

        public AnswerFromMe(@NonNull View itemView) {
            super(itemView);
            answerFromMe = itemView.findViewById(R.id.answerFromMe);
            message = itemView.findViewById(R.id.textAnswerFromMe);
            time = itemView.findViewById(R.id.timeAnswerFromMe);
            read = itemView.findViewById(R.id.statusAnswerFromMe);
            image = itemView.findViewById(R.id.imageAnswerFromMe);
            correct = itemView.findViewById(R.id.correctFromMe);
            correct.setVisibility(View.VISIBLE);
            incorrect = itemView.findViewById(R.id.incorrectFromMe);
            incorrect.setVisibility(View.VISIBLE);
        }

        void setData(Message data, String questionId, RecyclerViewItemClickListener textClick, RecyclerViewItemClickListener imageClick, int i, String senderId) {
            message.setText(data.getMessage());
            time.setText(Hey.getSeenTime(itemView.getContext(), data.getTime()));
            read.setImageResource(data.isRead() ? R.drawable.ic_read : R.drawable.ic_unread);
            Hey.workWithImageMessage(data, doc -> image.setImageURI(Uri.fromFile(new File(Hey.getLocalFile(data)))), errorMessage -> {});
            image.setOnClickListener(v -> imageClick.onItemClick(data, itemView, i));
            message.setOnClickListener(v -> textClick.onItemClick(data, itemView, i));
            thisAnswer = FirebaseFirestore.getInstance().collection(Keys.chats).document(questionId).collection(Keys.chats).document(data.getId());
            reference = thisAnswer.collection(Keys.users).document(senderId);
            numbersDocument = thisAnswer.collection(Keys.users).document(Keys.number);
            Hey.addDocumentListener(itemView.getContext(), numbersDocument, doc -> {
                Long iO = doc.getLong(Keys.incorrect), cO = doc.getLong(Keys.correct);
                iN = iO == null ? 0 : iO;
                cN = cO == null ? 0 : cO;
                correctText = itemView.getContext().getString(R.string.correct) + "(" + cN + ")";
                incorrectText = itemView.getContext().getString(R.string.incorrect) + "(" + iN + ")";
                correct.setText(correctText);
                incorrect.setText(incorrectText);
            }, errorMessage -> {

            });
            Hey.addDocumentListener(itemView.getContext(), reference, doc -> {
                if (doc.exists()){
                    Boolean condition = doc.getBoolean(Keys.correct);
                    if (condition!=null) if (condition){
                        answerFromMe.setBackgroundResource(R.drawable.message_my_me_green);
                    }else {
                        answerFromMe.setBackgroundResource(R.drawable.message_by_me_red);
                    }
                } else answerFromMe.setBackgroundResource(R.drawable.message_by_me_bg);
            }, errorMessage -> {

            });
        }
    }

    static class QuestionFromMe extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView read, image;

        public QuestionFromMe(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.textAnswerFromMe);
            time = itemView.findViewById(R.id.timeAnswerFromMe);
            read = itemView.findViewById(R.id.statusAnswerFromMe);
            image = itemView.findViewById(R.id.imageAnswerFromMe);
        }

        void setData(Message data, RecyclerViewItemClickListener textClick, RecyclerViewItemClickListener imageClick, RecyclerViewItemLongClickListener longClickListener, int i) {
            message.setText(data.getMessage());
            time.setText(Hey.getSeenTime(itemView.getContext(), data.getTime()));
            read.setImageResource(data.isRead() ? R.drawable.ic_read : R.drawable.ic_unread);
            Hey.workWithImageMessage(data, doc -> image.setImageURI(Uri.fromFile(new File(Hey.getLocalFile(data)))), errorMessage -> {});
            image.setOnClickListener(v -> imageClick.onItemClick(data, itemView, i));
            message.setOnClickListener(v -> textClick.onItemClick(data, itemView, i));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(data, itemView, i);
                return true;
            });
        }
    }

    static class TextFromOther extends RecyclerView.ViewHolder {
        TextView message, time, fullName;
        ImageView imageView;

        public TextFromOther(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.textFromOther);
            imageView = itemView.findViewById(R.id.imageTextFromOther);
            time = itemView.findViewById(R.id.timeTextFromOther);
            fullName = itemView.findViewById(R.id.fullNameTextFromOther);
        }

        void setData(Message data, RecyclerViewItemClickListener profileImageClick, int i) {
            message.setText(data.getMessage());
            time.setText(Hey.getSeenTime(itemView.getContext(), data.getTime()));
            Hey.setUserNameToMessage(itemView, data, fullName);
            Hey.getUserFromUserId(itemView.getContext(), String.valueOf(data.getSender()), doc -> {
                User user = (User) doc;
                Hey.workWithProfileImage(user, doc1 -> {
                    File f = new File(user.getLocalFileName());
                    imageView.setImageURI(Uri.fromFile(f));
                }, errorMessage -> {
                });
            }, errorMessage -> {

            });
            imageView.setOnClickListener(v -> profileImageClick.onItemClick(data, itemView, i));
        }
    }

    static class ImageFromOther extends RecyclerView.ViewHolder {
        TextView time, fullName;
        ImageView imageView, profileImage;

        public ImageFromOther(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.timeImageFromOtherQ);
            fullName = itemView.findViewById(R.id.fullNameImageFromOtherQ);
            imageView = itemView.findViewById(R.id.imageByOtherQ);
            profileImage = itemView.findViewById(R.id.profileQ);
        }

        void setData(Message data, RecyclerViewItemClickListener imageClick, RecyclerViewItemClickListener profileImageClick, int i) {
            time.setText(Hey.getSeenTime(itemView.getContext(), data.getTime()));

            Hey.getUserFromUserId(itemView.getContext(), String.valueOf(data.getSender()), doc -> {
                User user = (User) doc;
                Hey.workWithProfileImage(user, doc1 -> {
                    File f = new File(user.getLocalFileName());
                    profileImage.setImageURI(Uri.fromFile(f));
                }, errorMessage -> {
                });
            }, errorMessage -> {

            });
            profileImage.setOnClickListener(v -> profileImageClick.onItemClick(data, profileImage, i));
            Hey.setUserNameToMessage(itemView, data, fullName);
            Hey.workWithImageMessage(data, doc -> imageView.setImageURI(Uri.fromFile(new File(Hey.getLocalFile(data)))), errorMessage -> { });
            itemView.setOnClickListener(v -> imageClick.onItemClick(data, itemView, i));
        }
    }

    static class AnswerFromOther extends RecyclerView.ViewHolder {
        ImageView imageView, profileImage;
        ConstraintLayout answerFromOther;
        TextView message, time, fullName;
        Button correct, incorrect;
        DocumentReference myDoc, thisAnswer, numbersDocument, senderDoc,otherDoc,thisQuestionInAll;
        Relation relation;
        long cN, iN, senderCN, senderIN, senderU;
        boolean loading = true;
        String correctText = "?", incorrectText = "?";

        public AnswerFromOther(@NonNull View itemView) {
            super(itemView);
            answerFromOther = itemView.findViewById(R.id.answerFromOther);
            imageView = itemView.findViewById(R.id.imageAnswerFromOther);
            profileImage = itemView.findViewById(R.id.profileImageAnswerFromOther);
            message = itemView.findViewById(R.id.textAnswerFromOther);
            time = itemView.findViewById(R.id.timeAnswerFromOther);
            fullName = itemView.findViewById(R.id.fullNameAnswerFromOther);
            correct = itemView.findViewById(R.id.correct);
            incorrect = itemView.findViewById(R.id.incorrect);
        }

        void setData(Message data, String questionId, String actualTheme, RecyclerViewItemClickListener imageClick, RecyclerViewItemClickListener profileImageClick, int i, String senderId) {
            time.setText(Hey.getSeenTime(itemView.getContext(), data.getTime()));
            message.setText(data.getMessage());
            Hey.setUserNameToMessage(itemView, data, fullName);
            Hey.workWithImageMessage(data, doc -> imageView.setImageURI(Uri.fromFile(new File(Hey.getLocalFile(data)))), errorMessage -> {});
            imageView.setOnClickListener(v -> imageClick.onItemClick(data, null, i));
            profileImage.setOnClickListener(v -> profileImageClick.onItemClick(data, null, i));
            thisQuestionInAll = FirebaseFirestore.getInstance().collection(Keys.allQuestions).document(questionId);
            thisAnswer = FirebaseFirestore.getInstance().collection(Keys.chats).document(questionId).collection(Keys.chats).document(data.getId());
            myDoc = thisAnswer.collection(Keys.users).document(String.valueOf(My.id));
            otherDoc = thisAnswer.collection(Keys.users).document(senderId);
            numbersDocument = thisAnswer.collection(Keys.users).document(Keys.number);
            senderDoc = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(data.getSender()));
            Hey.addDocumentListener(itemView.getContext(), senderDoc, doc -> {
                if (!doc.getMetadata().isFromCache()) {
                    Long l = doc.getLong(Keys.numberOfCorrectAnswers), ll = doc.getLong(Keys.numberOfIncorrectAnswers), u = doc.getLong(Keys.units);
                    senderCN = l == null ? 0 : l;
                    senderIN = ll == null ? 0 : ll;
                    senderU = u == null ? 0 : u;
                }
                User user = User.fromDoc(doc);
                Hey.workWithProfileImage(user, doc1 -> {
                    File f = new File(user.getLocalFileName());
                    profileImage.setImageURI(Uri.fromFile(f));
                }, errorMessage -> {
                });
            }, errorMessage -> { });
            loadingView();

            Hey.addDocumentListener(itemView.getContext(), numbersDocument, doc -> {
                Long iO = doc.getLong(Keys.incorrect), cO = doc.getLong(Keys.correct);
                iN = iO == null ? 0 : iO;
                cN = cO == null ? 0 : cO;
                correctText = itemView.getContext().getString(R.string.correct) + "(" + cN + ")";
                incorrectText = itemView.getContext().getString(R.string.incorrect) + "(" + iN + ")";
                correct.setText(correctText);
                incorrect.setText(incorrectText);
                if (relation != null) setView();
            }, errorMessage -> {

            });

            Hey.addDocumentListener(itemView.getContext(), otherDoc, doc -> {
                if (doc.exists()){
                    Boolean condition = doc.getBoolean(Keys.correct);
                    if (condition!=null) if (condition){
                        answerFromOther.setBackgroundResource(R.drawable.message_by_other_green);
                    }else {
                        answerFromOther.setBackgroundResource(R.drawable.message_by_other_red);
                    }
                }
                else answerFromOther.setBackgroundResource(R.drawable.message_by_other);
            }, errorMessage -> { });

            Hey.addDocumentListener(itemView.getContext(), myDoc, doc -> {
                if (doc.exists()){
                    Boolean condition = doc.getBoolean(Keys.correct);
                    if (condition!=null) if (condition) {
                        relation = Relation.correct;
                    } else {
                        relation = Relation.incorrect;
                    }
                }
                else relation = Relation.none;
                setView();
            }, errorMessage -> {

            });

            correct.setOnClickListener(v -> {
                if (!loading) {
                    loadingView();
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            switch (relation) {
                                case none:
                                    if (Long.parseLong(senderId)==My.id) thisQuestionInAll.set(Collections.singletonMap(Keys.theme,actualTheme+Keys.correct),SetOptions.merge());
                                    incrementCorrectForUser();
                                    numbersDocument.set(Collections.singletonMap(Keys.correct, cN + 1), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    myDoc.set(Collections.singletonMap(Keys.correct, true), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    break;
                                case incorrect:
                                    if (Long.parseLong(senderId)==My.id) thisQuestionInAll.set(Collections.singletonMap(Keys.theme,actualTheme+Keys.correct),SetOptions.merge());
                                    incrementCorrectForUser();
                                    decrementIncorrectForUser();
                                    numbersDocument.set(Collections.singletonMap(Keys.correct, cN + 1), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    numbersDocument.set(Collections.singletonMap(Keys.incorrect, iN - 1), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    myDoc.update(Collections.singletonMap(Keys.correct, true)).addOnFailureListener(e -> notLoadingView());
                                    break;
                                case correct:
                                    if (Long.parseLong(senderId)==My.id) thisQuestionInAll.set(Collections.singletonMap(Keys.theme,actualTheme+Keys.incorrect),SetOptions.merge());
                                    decrementCorrectForUser();
                                    numbersDocument.set(Collections.singletonMap(Keys.correct, cN - 1), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    myDoc.delete().addOnFailureListener(e -> notLoadingView());
                                    break;
                            }
                        }

                        @Override
                        public void offline() {
                            notLoadingView();
                            Hey.showToast(itemView.getContext(), itemView.getContext().getString(R.string.error_connection));
                        }
                    }, errorMessage -> notLoadingView(), itemView.getContext());
                } else
                    Hey.showToast(itemView.getContext(), itemView.getContext().getString(R.string.wait));
            });
            incorrect.setOnClickListener(v -> {
                if (!loading) {
                    loadingView();
                    Hey.amIOnline(new StatusListener() {
                        @Override
                        public void online() {
                            switch (relation) {
                                case none:
                                    if (Long.parseLong(senderId)==My.id) thisQuestionInAll.set(Collections.singletonMap(Keys.theme,actualTheme+Keys.incorrect),SetOptions.merge());
                                    incrementIncorrectForUser(false);
                                    numbersDocument.set(Collections.singletonMap(Keys.incorrect, iN + 1), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    myDoc.set(Collections.singletonMap(Keys.correct, false)).addOnFailureListener(e -> notLoadingView());
                                    break;
                                case correct:
                                    if (Long.parseLong(senderId)==My.id) thisQuestionInAll.set(Collections.singletonMap(Keys.theme,actualTheme+Keys.incorrect),SetOptions.merge());
                                    incrementIncorrectForUser(true);
                                    decrementCorrectForUser();
                                    numbersDocument.set(Collections.singletonMap(Keys.incorrect, iN + 1), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    numbersDocument.set(Collections.singletonMap(Keys.correct, cN - 1), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    myDoc.update(Collections.singletonMap(Keys.correct, false)).addOnFailureListener(e -> notLoadingView());
                                    break;
                                case incorrect:
                                    if (Long.parseLong(senderId)==My.id) thisQuestionInAll.set(Collections.singletonMap(Keys.theme,actualTheme+Keys.correct),SetOptions.merge());
                                    decrementIncorrectForUser();
                                    numbersDocument.set(Collections.singletonMap(Keys.incorrect, iN - 1), SetOptions.merge()).addOnFailureListener(e -> notLoadingView());
                                    myDoc.delete().addOnFailureListener(e -> notLoadingView());
                                    break;
                            }
                        }

                        @Override
                        public void offline() {
                            notLoadingView();
                            Hey.showToast(itemView.getContext(), itemView.getContext().getString(R.string.error_connection));
                        }
                    }, errorMessage -> notLoadingView(), itemView.getContext());
                } else
                    Hey.showToast(itemView.getContext(), itemView.getContext().getString(R.string.wait));
            });
        }

        private void setView() {
            notLoadingView();
            switch (relation) {
                case incorrect:
                    setIncorrectView();
                    break;
                case correct:
                    setCorrectView();
                    break;
                case none:
                    setNoneView();
                    break;
            }
        }

        void setCorrectView() {
            setButtonAsDefault(incorrect);
            setButtonAsSelected(correct);
        }

        void setIncorrectView() {
            setButtonAsDefault(correct);
            setButtonAsSelected(incorrect);
        }

        void setNoneView() {
            setButtonAsDefault(correct);
            setButtonAsDefault(incorrect);
        }

        void setButtonAsSelected(Button b) {
            b.setBackgroundResource(R.drawable.button_bg_pressed);
            b.setTextColor(Color.WHITE);
        }

        void setButtonAsDefault(Button b) {
            b.setBackgroundResource(R.drawable.sss);
            b.setTextColor(Color.BLACK);
        }

        void loadingView() {
            Hey.setButtonAsLoading(itemView.getContext(), correct);
            Hey.setButtonAsLoading(itemView.getContext(), incorrect);
            loading = true;
        }

        void notLoadingView() {
            Hey.setButtonAsDefault(itemView.getContext(), correct, correctText);
            Hey.setButtonAsDefault(itemView.getContext(), incorrect, incorrectText);
            loading = false;
        }

        void incrementCorrectForUser() {
            Map<String, Object> d = new HashMap<>();
            d.put(Keys.numberOfCorrectAnswers, senderCN + 1);
            d.put(Keys.units, senderU + My.unitsForPerDay);
            senderDoc.set(d, SetOptions.merge());
        }

        void decrementCorrectForUser() {
            Map<String, Object> d = new HashMap<>();
            d.put(Keys.numberOfCorrectAnswers, senderCN - 1);
            d.put(Keys.units, senderU - My.unitsForPerDay < 0 ? 0 : senderU - My.unitsForPerDay);
            senderDoc.set(d, SetOptions.merge());
        }

        void incrementIncorrectForUser(boolean changeUnit) {
            Map<String, Object> d = new HashMap<>();
            d.put(Keys.numberOfIncorrectAnswers, senderIN + 1);
            if (changeUnit) d.put(Keys.units, senderU - My.unitsForPerDay < 0 ? 0 : senderU - My.unitsForPerDay);
            senderDoc.set(d, SetOptions.merge());
        }

        void decrementIncorrectForUser() {
            Map<String, Object> d = new HashMap<>();
            d.put(Keys.numberOfIncorrectAnswers, senderIN - 1);
            senderDoc.set(d, SetOptions.merge());
        }
    }

    static class QuestionFromOther extends RecyclerView.ViewHolder {
        ImageView imageView, profileImage;
        TextView message, time, fullName;
        Button correct, incorrect;

        public QuestionFromOther(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageAnswerFromOther);
            profileImage = itemView.findViewById(R.id.profileImageAnswerFromOther);
            message = itemView.findViewById(R.id.textAnswerFromOther);
            time = itemView.findViewById(R.id.timeAnswerFromOther);
            fullName = itemView.findViewById(R.id.fullNameAnswerFromOther);
            correct = itemView.findViewById(R.id.correct);
            incorrect = itemView.findViewById(R.id.incorrect);
        }

        void setData(Message data, RecyclerViewItemClickListener imageClick, RecyclerViewItemClickListener profileImageClick, int i) {
            time.setText(Hey.getSeenTime(itemView.getContext(), data.getTime()));
            message.setText(data.getMessage());
            Hey.setUserNameToMessage(itemView, data, fullName);
            Hey.workWithImageMessage(data, doc -> imageView.setImageURI(Uri.fromFile(new File(Hey.getLocalFile(data)))), errorMessage -> {});
            correct.setVisibility(View.INVISIBLE);
            incorrect.setVisibility(View.INVISIBLE);

            Hey.getUserFromUserId(itemView.getContext(), String.valueOf(data.getSender()), doc -> {
                User user = (User) doc;

                Hey.workWithProfileImage(user, doc1 -> {
                    File f = new File(user.getLocalFileName());
                    profileImage.setImageURI(Uri.fromFile(f));
                }, errorMessage -> {
                });
            }, errorMessage -> {

            });

            profileImage.setOnClickListener(v -> profileImageClick.onItemClick(data, null, i));
            imageView.setOnClickListener(v -> imageClick.onItemClick(data, imageView, i));
        }
    }

    static class LoadMore extends RecyclerView.ViewHolder{
        Button button;
        public LoadMore(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.loadMore);
        }

        void setData(ItemClickForQuestion listener,boolean showButton){
            button.setVisibility(showButton ? View.VISIBLE : View.GONE);
            button.setOnClickListener(view -> {
                Hey.setButtonAsLoading(itemView.getContext(), button);
                listener.onItemClick(0,"",button);
            });
        }
    }
}
