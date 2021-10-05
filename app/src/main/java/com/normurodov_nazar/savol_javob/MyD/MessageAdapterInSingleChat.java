package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapterInSingleChat extends RecyclerView.Adapter {
    final List<TextMessage> messages;
    final Context context;
    final RecyclerViewItemClickListener listener;

    public MessageAdapterInSingleChat(List<TextMessage> messages, Context context,RecyclerViewItemClickListener listener) {
        this.messages = messages;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1){
            View view = LayoutInflater.from(context).inflate(R.layout.message_from_me,parent,false);
            return new MessageFormMeHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.message_from_other,parent,false);
            return new MessageFormOtherHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TextMessage messageSingleChat = messages.get(position);
        if(messageSingleChat.sender==My.id){
            ((MessageFormMeHolder) holder).setChatItem(messageSingleChat,listener);
        }else {
            ((MessageFormOtherHolder) holder).setChatItem(messageSingleChat);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).sender==My.id) return 1; else return 2;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageFormMeHolder extends RecyclerView.ViewHolder{
        TextView message,time;

        public MessageFormMeHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.messageFromMe);
            time = itemView.findViewById(R.id.timeMessageFromMeInSingleChat);
        }
        void setChatItem(TextMessage data,RecyclerViewItemClickListener listener){
            itemView.setOnClickListener(v -> listener.onItemClick(data,itemView));
            message.setText(data.message);time.setText(Hey.getSeenTime(itemView.getContext(), data.time));
        }
    }
    static class MessageFormOtherHolder extends RecyclerView.ViewHolder{
        TextView message,time;

        public MessageFormOtherHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.messageFromOtherInSingleChat);
            time = itemView.findViewById(R.id.timeMessageFromOtherInSingleChat);
        }
        void setChatItem(TextMessage data){
            message.setText(data.message);time.setText(Hey.getSeenTime(itemView.getContext(), data.time));
        }
    }
}
