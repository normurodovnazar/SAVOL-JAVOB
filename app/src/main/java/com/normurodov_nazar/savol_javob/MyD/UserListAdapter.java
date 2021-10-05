package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.normurodov_nazar.savol_javob.Activities.SingleChat;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MViewHolder> {

    public UserListAdapter(Context context,ArrayList<User> users,boolean fromSearch) {
        this.users = users;
        this.context = context;
    }

    ArrayList<User> users;
    Context context;

    @NonNull
    @Override
    public UserListAdapter.MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new MViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListAdapter.MViewHolder holder, int position) {
        holder.setUser(context,users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class MViewHolder extends RecyclerView.ViewHolder{
        TextView name,seen;
        View view;
        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_user_item);
            seen = itemView.findViewById(R.id.seen_user_item);
            view = itemView;
        }

        void setUser(Context context,User user){
            String name = user.getName(),surname = user.getSurname(),full = surname+" "+name;
            this.name.setText(full);
            String s = context.getString(R.string.activity)+Hey.getSeenTime(context,user.getSeen());this.seen.setText(s);
            view.setOnClickListener(v->{
                Intent i = new Intent(context, SingleChat.class);
                i.putExtra(Keys.chatId,Hey.getChatIdFromIds(My.id,user.getId()));
                context.startActivity(i);
            });
        }
    }
}
