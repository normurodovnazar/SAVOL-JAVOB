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

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;
import java.util.List;

public class MessageAdapterInSingleChat extends RecyclerView.Adapter {
    List<Message> messages;
    final Context context;
    final RecyclerViewItemClickListener listener;
    final RecyclerViewItemLongClickListener longClickListener;

    public MessageAdapterInSingleChat(List<Message> messages, Context context, RecyclerViewItemClickListener listener,RecyclerViewItemLongClickListener longClickListener) {
        this.messages = messages;
        this.context = context;
        this.listener = listener;
        this.longClickListener=longClickListener;
    }

    public void removeItem(Message message){
        int index= messages.indexOf(message);
        messages.remove(message);
        notifyItemRemoved(index);
    }

    public void addItem(Message message){
        messages.add(message);
        notifyItemInserted(messages.size()-1);
    }

    public void changeItem(Message message){
        int index = messages.indexOf(message);
        messages.set(index, message);
        notifyItemChanged(index);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case 1:
                View meT = LayoutInflater.from(context).inflate(R.layout.message_from_me,parent,false);
                return new MessageFromMeHolder(meT,true);
            case 3:
                View meI = LayoutInflater.from(context).inflate(R.layout.image_message_from_me,parent,false);
                return new MessageFromMeHolder(meI,false);
            case 2:
                View otherT = LayoutInflater.from(context).inflate(R.layout.message_from_other,parent,false);
                return new MessageFromOtherHolder(otherT,true);
            default:
                View otherI = LayoutInflater.from(context).inflate(R.layout.image_message_from_other,parent,false);
                return new MessageFromOtherHolder(otherI,false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message messageSingleChat = messages.get(position);
        if(messageSingleChat.sender==My.id){
            ((MessageFromMeHolder) holder).setChatItem(messageSingleChat,listener,longClickListener,position);
        }else {
            ((MessageFromOtherHolder) holder).setChatItem(messageSingleChat,listener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
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

    static class MessageFromMeHolder extends RecyclerView.ViewHolder{
        TextView message,time;
        ImageView imageView;
        final boolean isTextMessage;

        public MessageFromMeHolder(View itemView, boolean isTextMessage) {
            super(itemView);
            this.isTextMessage = isTextMessage;
            if(isTextMessage){
                message = itemView.findViewById(R.id.messageFromMe);
            }else {
                imageView = itemView.findViewById(R.id.imageMessageByMe);
            }
            time = itemView.findViewById(R.id.timeMessageFromMeInSingleChat);
        }

        void setChatItem(Message data, RecyclerViewItemClickListener listener,RecyclerViewItemLongClickListener longClickListener,int position){

            time.setText(Hey.getSeenTime(itemView.getContext(), data.time));
            itemView.setOnClickListener(v -> listener.onItemClick(data,itemView));
            if(isTextMessage) {
                message.setText(data.message);
            } else {
                itemView.setOnLongClickListener(v -> {
                    longClickListener.onItemLongClick(data,itemView,position);
                    return true;
                });
                File f = new File(Hey.getLocalFile(data));
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

    static class MessageFromOtherHolder extends RecyclerView.ViewHolder{
        TextView message,time;
        ImageView imageView;
        boolean isTextMessage;

        public MessageFromOtherHolder(@NonNull View itemView,boolean isTextMessage) {
            super(itemView);
            this.isTextMessage = isTextMessage;
            if (isTextMessage) {
                time = itemView.findViewById(R.id.timeMessageFromOtherInSingleChat);
                message = itemView.findViewById(R.id.messageFromOtherInSingleChat);
            } else {
                time = itemView.findViewById(R.id.timeImageMessageFromOtherInSingleChat);
                imageView = itemView.findViewById(R.id.imageMessageByOther);
            }
        }

        void setChatItem(Message data,RecyclerViewItemClickListener itemClickListener){
            time.setText(Hey.getSeenTime(itemView.getContext(), data.time));
            if (isTextMessage){
                message.setText(data.message);
            }else {
                itemView.setOnClickListener(v -> itemClickListener.onItemClick(data,itemView));
                File f = new File(Hey.getLocalFile(data));
                Hey.print("a",f.getPath());
                if (f.exists()){
                    imageView.setImageURI(Uri.fromFile(f));
                }else imageView.setImageResource(R.drawable.download_ic);
            }
        }
    }
}
