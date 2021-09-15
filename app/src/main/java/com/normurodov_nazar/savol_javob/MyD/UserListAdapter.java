package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MViewHolder> {

    public UserListAdapter(ArrayList<User> users,Context context) {
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
        holder.setUser(users.get(position));
        Log.e("e", String.valueOf(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class MViewHolder extends RecyclerView.ViewHolder{
        TextView name,seen;

        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_user_item);
            seen = itemView.findViewById(R.id.seen_user_item);
        }

        void setUser(User user){
            String name = user.getName(),surname = user.getSurname(),full = surname+" "+name;
            this.name.setText(full);
            this.seen.setText(user.getSeen());
            //TODO: need fix image
        }
    }
}
