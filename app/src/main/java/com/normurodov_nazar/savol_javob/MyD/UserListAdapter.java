package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.graphics.Color;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MViewHolder> {

    final RecyclerViewItemClickListener clickListener;
    final RecyclerViewItemLongClickListener longClickListener;

    public UserListAdapter(Context context, ArrayList<Long> userIds, RecyclerViewItemClickListener clickListener, RecyclerViewItemLongClickListener longClickListener) {
        this.userIds = userIds;
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }


    ArrayList<Long> userIds;
    Context context;

    @NonNull
    @Override
    public UserListAdapter.MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new MViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListAdapter.MViewHolder holder, int position) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Keys.chats).document(Hey.getChatIdFromIds(userIds.get(position),My.id));
        Hey.getDocument(context, documentReference, doc -> {
            DocumentSnapshot snapshot = (DocumentSnapshot) doc;
            Object ol = snapshot.get(Keys.newMessagesTo +My.id);
            long l = ol==null ? 0 : (long) ol;
            holder.setUser(context, userIds.get(position),l,clickListener,longClickListener,position);

        }, errorMessage -> {

        });
    }

    @Override
    public int getItemCount() {
        return userIds.size();
    }

    static class MViewHolder extends RecyclerView.ViewHolder{
        TextView name,seen;
        ImageView imageView;
        View view;
        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_user_item);
            seen = itemView.findViewById(R.id.seen_user_item);
            imageView = itemView.findViewById(R.id.profile_image_item);
            view = itemView;
        }

        void setUser(Context context,long id,long n,RecyclerViewItemClickListener clickListener,RecyclerViewItemLongClickListener longClickListener,int position){

            Hey.getDocument(context, FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(id)), doc -> {
                User user = User.fromDoc((DocumentSnapshot) doc);
                MViewHolder.this.name.setText(user.getFullName());
                changeSubtitle(context,user.getSeen(),n);
                File file = new File(user.getLocalFileName());
                Hey.workWithProfileImage(user, doc0 -> imageView.setImageURI(Uri.fromFile(file)), errorMessage -> {
                });
                DocumentReference ref = FirebaseFirestore.getInstance().collection(Keys.chats).document(Hey.getChatIdFromIds(My.id,id));
                view.setOnClickListener(v -> clickListener.onItemClick(null,null,position));
                view.setOnLongClickListener(v -> {
                    longClickListener.onItemLongClick(new Message(Collections.singletonMap(Keys.message,user.getFullName()),""),imageView,position);
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
                Hey.print("seen",Hey.getSeenTime(context,seenTime));
                s = context.getString(R.string.activity) + Hey.getSeenTime(context, seenTime);
                seen.setTextColor(Color.BLACK);
            } else {
                seen.setTextColor(Color.RED);
                s = l + " " + context.getString(R.string.newMessage);
            }
            this.seen.setText(s);
        }


    }
}
