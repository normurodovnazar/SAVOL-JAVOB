package com.normurodov_nazar.savol_javob.MyD;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;
import java.util.List;

public class MessageAdapterInSingleChat extends RecyclerView.Adapter {
    final List<Message> messages;
    final Context context;
    final RecyclerViewItemClickListener listener;

    public MessageAdapterInSingleChat(List<Message> messages, Context context, RecyclerViewItemClickListener listener) {
        this.messages = messages;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case 1:
                View meT = LayoutInflater.from(context).inflate(R.layout.message_from_me,parent,false);
                return new MessageFormMeHolder(meT,true);
            case 2:
                View otherT = LayoutInflater.from(context).inflate(R.layout.message_from_other,parent,false);
                return new MessageFormOtherHolder(otherT);
            case 3:
                View meI = LayoutInflater.from(context).inflate(R.layout.image_message_from_me,parent,false);
                return new MessageFormMeHolder(meI,false);
            default:
                View otherI = LayoutInflater.from(context).inflate(R.layout.image_message_from_me,parent,false);
                return new MessageFormOtherHolder(otherI);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message messageSingleChat = messages.get(position);
        if(messageSingleChat.sender==My.id){
            ((MessageFormMeHolder) holder).setChatItem(messageSingleChat,listener);
        }else {
            ((MessageFormOtherHolder) holder).setChatItem(messageSingleChat);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        Hey.print("a",message.toMap().toString());
        if(message.sender==My.id) {
            if(message.getType().equals(Keys.textMessage)) return 1; else return 3;
        } else {
            if(message.getType().equals(Keys.textMessage)) return 2; else return 4;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageFormMeHolder extends RecyclerView.ViewHolder{
        TextView message,time;
        ImageView imageView;
        final boolean isTextMessage;

        public MessageFormMeHolder(View itemView,boolean isTextMessage) {
            super(itemView);
            this.isTextMessage = isTextMessage;
            if(isTextMessage){
                message = itemView.findViewById(R.id.messageFromMe);
            }else {
                imageView = itemView.findViewById(R.id.imageMessageByMe);
            }
            time = itemView.findViewById(R.id.timeMessageFromMeInSingleChat);
        }

        void setChatItem(Message data, RecyclerViewItemClickListener listener){
            itemView.setOnClickListener(v -> listener.onItemClick(data,itemView));
            time.setText(Hey.getSeenTime(itemView.getContext(), data.time));
            if(isTextMessage) message.setText(data.message); else {
                File f = new File(My.folder+data.getId()+".png");
                Hey.print("a",f.getPath());
                if (f.exists()){
                    imageView.setImageURI(Uri.fromFile(f));
                }else imageView.setImageResource(R.drawable.download_ic);
//                    Hey.downloadFile(imageView.getContext(), Keys.chats, data.getSender() + "" + data.getTime(), f, (progress, total) -> {
//
//                    }, doc -> {
//
//                    }, errorMessage -> {
//
//                    });
//                time.setText(Hey.getSeenTime(itemView.getContext(), data.time));
            }

        }
    }

    static class MessageFormOtherHolder extends RecyclerView.ViewHolder{
        TextView message,time;

        public MessageFormOtherHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.messageFromOtherInSingleChat);
            time = itemView.findViewById(R.id.timeMessageFromOtherInSingleChat);
        }
        void setChatItem(Message data){
            message.setText(data.message);time.setText(Hey.getSeenTime(itemView.getContext(), data.time));
        }
    }
}
