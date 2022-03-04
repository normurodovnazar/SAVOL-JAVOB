package com.normurodov_nazar.adminformath.MyD;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;
import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MViewHolder> {

    final UserClickListener click;
    final UserClickListener longClick;

    public UserListAdapter(Context context, ArrayList<Long> userIds, UserClickListener userClickListener, UserClickListener longClick) {
        this.userIds = userIds;
        this.context = context;
        this.click = userClickListener;
        this.longClick = longClick;
    }


    final ArrayList<Long> userIds;
    final Context context;

    @NonNull
    @Override
    public MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new MViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Keys.chats).document(Hey.getChatIdFromIds(userIds.get(position),My.id));
        holder.animateView();
        Hey.getDocument(context, documentReference, doc -> {
            DocumentSnapshot snapshot = (DocumentSnapshot) doc;
            Object ol = snapshot.get(Keys.newMessagesTo +My.id);
            long l = ol==null ? 0 : (long) ol;
            holder.setUser(context, userIds.get(position),l, click,longClick);
        }, errorMessage -> {

        });
    }

    @Override
    public int getItemCount() {
        return userIds.size();
    }

    static class MViewHolder extends RecyclerView.ViewHolder{
        final TextView name;
        final TextView seen;
        final ImageView imageView;
        final View view;
        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_user_item);
            seen = itemView.findViewById(R.id.seen_user_item);
            imageView = itemView.findViewById(R.id.profile_image_item);
            view = itemView;
        }

        void animateView(){
            Hey.setIconButtonAsLoading(itemView);
        }

        void setUser(Context context, long id, long n, UserClickListener userClickListener, UserClickListener longClick){
            Hey.getDocument(context, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(id)), doc -> {
                Hey.setIconButtonAsDefault(itemView);
                User user = User.fromDoc((DocumentSnapshot) doc);
                MViewHolder.this.name.setText(user.getFullName());
                changeSubtitle(context,user.getSeen(),n);
                File file = new File(user.getLocalFileName());
                if (user.hasProfileImage()) {
                    Hey.print(user.getFullName(),"Has profile image");
                    Hey.workWithProfileImage(user, doc0 -> imageView.setImageURI(Uri.fromFile(file)), errorMessage -> { });
                }else Hey.print(user.getFullName(),"Hasn't profile image");
                DocumentReference ref = FirebaseFirestore.getInstance().collection(Keys.chats).document(Hey.getChatIdFromIds(My.id,id));
                view.setOnClickListener(v -> userClickListener.onUserClick(user));
                view.setOnLongClickListener(v -> {
                    longClick.onUserClick(user);
                    return true;
                });
                Hey.addDocumentListener(context, ref, doc1 -> {
                    Object o = doc1.get(Keys.newMessagesTo+My.id);
                    long l = o==null ? 0 : (long) o;
                    changeSubtitle(context,user.getSeen(),l);
                }, errorMessage -> {

                });
            }, errorMessage -> { });
        }

        private void changeSubtitle(Context context,long seenTime,long l) {
            String s;
            if (l == 0) {
                Hey.print("seen",Hey.getTimeText(context,seenTime));
                s = context.getString(R.string.activity) + Hey.getTimeText(context, seenTime);
                seen.setTextColor(context.getResources().getColor(R.color.black));
            } else {
                seen.setTextColor(context.getResources().getColor(R.color.red));
                s = l + " " + context.getString(R.string.newMessage);
            }
            this.seen.setText(s);
        }


    }
}
