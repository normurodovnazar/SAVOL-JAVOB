package com.normurodov_nazar.adminapp.MyD;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.normurodov_nazar.adminapp.MFunctions.Hey;
import com.normurodov_nazar.adminapp.MFunctions.Keys;
import com.normurodov_nazar.adminapp.MFunctions.My;
import com.normurodov_nazar.adminapp.R;

import java.util.ArrayList;

public class MessageAdapterInSingleChat extends RecyclerView.Adapter {
    final ArrayList<Message> messages;
    final Context context;
    final RecyclerViewItemClickListener listener;
    final RecyclerViewItemLongClickListener longClickListener;
    final ItemClickForQuestion loadMore;

    public MessageAdapterInSingleChat(ArrayList<Message> messages, Context context, RecyclerViewItemClickListener listener, RecyclerViewItemLongClickListener longClickListener,ItemClickForQuestion loadMore) {
        this.messages = messages;
        this.context = context;
        this.listener = listener;
        this.longClickListener=longClickListener;
        this.loadMore = loadMore;
    }

    public void removeItem(Message message){
        int i = Hey.getIndexInArray(message, messages);
        Hey.print("i", String.valueOf(i));
        if (i!=-1){
            messages.remove(i);
            notifyItemRemoved(i+1);
        }
    }

    public void addItems(ArrayList<Message> newMessages, int itemCount){
        int startPosition = this.messages.size();
        this.messages.addAll(newMessages);
        notifyItemRangeInserted(startPosition+2,itemCount);
    }

    public void addItemsToTop(ArrayList<Message> newMessages){
        messages.addAll(0,newMessages);
        notifyItemRangeInserted(1,newMessages.size());
    }

    public void changeItem(int position, Message message){
        messages.set(position, message);
        notifyItemChanged(position+1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case 0:
                View loadMore = LayoutInflater.from(context).inflate(R.layout.load_more_button,parent,false);
                return new LoadMore(loadMore);
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
        Message messageSingleChat = messages.get(position==0 ? 0 : position-1);
        if (position==0) ((LoadMore) holder).setData(loadMore,messages.size()>=50); else if(messageSingleChat.sender==My.id){
            ((MessageFromMeHolder) holder).setChatItem(messageSingleChat,listener,longClickListener,position-1);
        }else {
            ((MessageFromOtherHolder) holder).setChatItem(messageSingleChat,listener,position-1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position==0 ? 0 : position-1);
        if (position==0) return 0; else if(message.sender==My.id) {
            if(message.getType().equals(Keys.textMessage)) return 1; else return 3;
        } else {
            if(message.getType().equals(Keys.textMessage)) return 2; else return 4;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size()+1;
    }

    static class MessageFromMeHolder extends RecyclerView.ViewHolder{
        TextView message;
        final TextView time;
        TextView imageSize;
        ImageView imageView;
        final ImageView read;
        final boolean isTextMessage;

        public MessageFromMeHolder(View itemView, boolean isTextMessage) {
            super(itemView);
            this.isTextMessage = isTextMessage;
            if(isTextMessage){
                read = itemView.findViewById(R.id.statusOfMessage);
                message = itemView.findViewById(R.id.messageFromMe);
                time = itemView.findViewById(R.id.timeMessageFromMeInSingleChat);
            }else {
                imageSize = itemView.findViewById(R.id.imageSize);
                read = itemView.findViewById(R.id.statusOfImageMessage);
                imageView = itemView.findViewById(R.id.imageMessageByMe);
                time = itemView.findViewById(R.id.timeImageFromMeFromMe);
            }
        }

        void setChatItem(Message data, RecyclerViewItemClickListener listener, RecyclerViewItemLongClickListener longClickListener, int position){
            time.setText(Hey.getTimeText(itemView.getContext(), data.time));
            itemView.setOnClickListener(v -> listener.onItemClick(data,itemView,position));
            if(isTextMessage) {
                message.setText(data.message);
            } else {
                imageSize.setText(Hey.getMb(data.getImageSize()));
                itemView.setOnLongClickListener(v -> {
                    longClickListener.onItemLongClick(data,itemView,position);
                    return true;
                });
                Hey.workWithImageMessage(data, doc -> imageView.setImageURI(Uri.fromFile(Hey.getLocalFile(data))), errorMessage -> {});
            }
            read.setImageResource(data.isRead() ? R.drawable.ic_read : R.drawable.ic_unread);
        }
    }

    static class MessageFromOtherHolder extends RecyclerView.ViewHolder{
        TextView message;
        final TextView time;
        TextView imageSize;
        ImageView imageView;
        final boolean isTextMessage;

        public MessageFromOtherHolder(@NonNull View itemView,boolean isTextMessage) {
            super(itemView);
            this.isTextMessage = isTextMessage;
            if (isTextMessage) {
                time = itemView.findViewById(R.id.timeMessageFromOtherInSingleChat);
                message = itemView.findViewById(R.id.messageFromOtherInSingleChat);
            } else {
                imageSize = itemView.findViewById(R.id.imageSize);
                time = itemView.findViewById(R.id.timeImageMessageFromOtherInSingleChat);
                imageView = itemView.findViewById(R.id.imageMessageByOther);
            }
        }

        void setChatItem(Message data, RecyclerViewItemClickListener itemClickListener, int position){
            time.setText(Hey.getTimeText(itemView.getContext(), data.time));
            if (isTextMessage){
                message.setText(data.message);
            }else {
                imageSize.setText(Hey.getMb(data.getImageSize()));
                Hey.workWithImageMessage(data, doc -> imageView.setImageURI(Uri.fromFile(Hey.getLocalFile(data))), errorMessage -> imageView.setImageResource(R.drawable.download_black_ic));
            }
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(data,itemView,position));
        }
    }

    static class LoadMore extends RecyclerView.ViewHolder{
        final Button button;
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
