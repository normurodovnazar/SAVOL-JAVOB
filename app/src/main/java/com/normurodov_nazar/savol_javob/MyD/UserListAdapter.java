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

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MViewHolder> {

    final RecyclerViewItemClickListener clickListener;
    final RecyclerViewItemLongClickListener longClickListener;

    public UserListAdapter(Context context,ArrayList<User> users,boolean fromSearch,RecyclerViewItemClickListener clickListener,RecyclerViewItemLongClickListener longClickListener) {
        this.users = users;
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }


    ArrayList<User> users;
    Context context;

    @NonNull
    @Override
    public UserListAdapter.MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new MViewHolder(view);
    }

    public void removeItem(int position){
        notifyItemRemoved(position);
        users.remove(position);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListAdapter.MViewHolder holder, int position) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Keys.chats).document(Hey.getChatIdFromIds(users.get(position).getId(),My.id));
        Hey.getDocument(context, documentReference, doc -> {
            DocumentSnapshot snapshot = (DocumentSnapshot) doc;
            Object ol = snapshot.get(Keys.newMessagesTo +My.id);
            long l = ol==null ? 0 : (long) ol;
            holder.setUser(context,users.get(position),l,clickListener,longClickListener,position);
        }, errorMessage -> {

        });
    }

    @Override
    public int getItemCount() {
        return users.size();
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

        void setUser(Context context,User user,long n,RecyclerViewItemClickListener clickListener,RecyclerViewItemLongClickListener longClickListener,int position){
            String name = user.getName(),surname = user.getSurname(),full = surname+" "+name;
            this.name.setText(full);
            changeSubtitle(context,user,n);
            File file = new File(user.getLocalFileName());
            Hey.workWithProfileImage(user, doc -> imageView.setImageURI(Uri.fromFile(file)), errorMessage -> {

            });
            view.setOnClickListener(v -> clickListener.onItemClick(null,null,position));
            view.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(null,imageView,position);
                return true;
            });
            DocumentReference ref = FirebaseFirestore.getInstance().collection(Keys.chats).document(Hey.getChatIdFromIds(My.id,user.getId()));
            Hey.addDocumentListener(context, ref, doc -> {
                Hey.print("listener","new message");
                Object o = ((DocumentSnapshot) doc).get(Keys.newMessagesTo+My.id);
                long l = o==null ? 0 : (long) o;
                changeSubtitle(context,user,l);
            }, errorMessage -> {

            });
        }

        private void changeSubtitle(Context context,User user,long l) {
            String s;
            if (l == 0) {
                s = context.getString(R.string.activity) + Hey.getSeenTime(context, user.getSeen());
                seen.setTextColor(Color.BLACK);
            }
            else {
                seen.setTextColor(Color.RED);
                s = l + " " + context.getString(R.string.newMessage);
            }
            this.seen.setText(s);
        }


    }
}
