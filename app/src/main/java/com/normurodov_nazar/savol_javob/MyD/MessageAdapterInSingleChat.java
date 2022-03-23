package com.normurodov_nazar.savol_javob.MyD;

import static com.normurodov_nazar.savol_javob.MFunctions.Hey.getDocumentFromCache;
import static com.normurodov_nazar.savol_javob.MFunctions.Hey.print;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;

public class MessageAdapterInSingleChat extends RecyclerView.Adapter {
    final ArrayList<Message> messages;
    final Context context;
    final RecyclerViewItemClickListener listener;
    final RecyclerViewItemLongClickListener longClickListener;
    final RecyclerViewItemClickListener replyListener;
    final ItemClickForQuestion loadMore;
    final CollectionReference chats;
    final SuccessListener scrollToListener;

    public MessageAdapterInSingleChat(ArrayList<Message> messages, Context context,CollectionReference chats, RecyclerViewItemClickListener listener, RecyclerViewItemLongClickListener longClickListener, ItemClickForQuestion loadMore,RecyclerViewItemClickListener replyListener,SuccessListener scrollToListener) {
        this.messages = messages;
        this.context = context;
        this.listener = listener;
        this.chats = chats;
        this.longClickListener = longClickListener;
        this.replyListener = replyListener;
        this.loadMore = loadMore;
        this.scrollToListener = scrollToListener;
    }

    public void removeItem(Message message) {
        int i = Hey.getIndexInArray(message, messages);
        print("i", String.valueOf(i));
        if (i != -1) {
            messages.remove(i);
            notifyItemRemoved(i + 1);
        }
    }

    public void addItems(ArrayList<Message> newMessages, int itemCount) {
        int startPosition = this.messages.size();
        this.messages.addAll(newMessages);
        notifyItemRangeInserted(startPosition + 2, itemCount);
    }

    public void addItemsToTop(ArrayList<Message> newMessages) {
        messages.addAll(0, newMessages);
        notifyItemRangeInserted(1, newMessages.size());
    }

    public void changeItem(int position, Message message) {
        messages.set(position, message);
        notifyItemChanged(position + 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View loadMore = LayoutInflater.from(context).inflate(R.layout.load_more_button, parent, false);
                return new LoadMore(loadMore);
            case 1:
                View meT = LayoutInflater.from(context).inflate(R.layout.message_from_me, parent, false);
                return new TextFromMeHolder(meT);
            case 3:
                View meI = LayoutInflater.from(context).inflate(R.layout.image_message_from_me, parent, false);
                return new ImageFromMe(meI);

            case 2:
                View otherT = LayoutInflater.from(context).inflate(R.layout.message_from_other, parent, false);
                return new TextFromOther(otherT);
            case 4:
                View otherI = LayoutInflater.from(context).inflate(R.layout.image_message_from_other, parent, false);
                return new ImageFromOther(otherI);
            case -1:
                View replyMe = LayoutInflater.from(context).inflate(R.layout.reply_from_me,parent,false);
                return new ReplyFromMe(replyMe);
            case -2:
                View replyOther = LayoutInflater.from(context).inflate(R.layout.reply_form_other,parent,false);
                return new ReplyFromOther(replyOther);
            default:
                View versionM = LayoutInflater.from(context).inflate(R.layout.version_message, parent, false);
                return new VersionMessage(versionM);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position == 0 ? 0 : position - 1);
        if (position == 0) ((LoadMore) holder).setData(loadMore, messages.size() >= 50);
        else {
            if (message.sender == My.id) {
                switch (message.getType()) {
                    case Keys.textMessage:
                        ((TextFromMeHolder) holder).setChatItem(message, listener, position);
                        break;
                    case Keys.imageMessage:
                        ((ImageFromMe) holder).setChatItem(message, listener, longClickListener, position);
                        break;
                    case Keys.reply:
                        ((ReplyFromMe) holder).setData(message,chats,listener,scrollToListener,position);
                        break;
                }
            } else {
                switch (message.getType()){
                    case Keys.textMessage:
                        ((TextFromOther) holder).setChatItem(message,listener,replyListener,position);
                        break;
                    case Keys.imageMessage:
                        ((ImageFromOther)holder).setChatItem(message,listener,replyListener,position);
                        break;
                    case Keys.reply:
                        ((ReplyFromOther) holder).setData(message,chats,listener,scrollToListener,position);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position == 0 ? 0 : position - 1);
        if (position == 0) return 0;
        else if (message.sender == My.id) {
            switch (message.getType()) {
                case Keys.textMessage:
                    return 1;
                case Keys.imageMessage:
                    return 3;
                case Keys.reply:
                    return -1;
                default:
                    return -10;
            }
        } else {
            switch (message.getType()) {
                case Keys.textMessage:
                    return 2;
                case Keys.imageMessage:
                    return 4;
                case Keys.reply:
                    return -2;
                default:
                    return -20;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size() + 1;
    }



    static class ReplyFromOther extends RecyclerView.ViewHolder{
        final ConstraintLayout nested;
        final ConstraintLayout parent;
        final TextView message;
        final TextView time;
        final TextView original;
        public ReplyFromOther(@NonNull View itemView) {
            super(itemView);
            original = itemView.findViewById(R.id.original);
            parent = itemView.findViewById(R.id.parent);
            time = itemView.findViewById(R.id.time);
            nested = itemView.findViewById(R.id.nested);
            message = itemView.findViewById(R.id.message);
        }

        void setData(Message data,CollectionReference chats,RecyclerViewItemClickListener listener,SuccessListener originalLister,int i){
            time.setText(Hey.getTimeText(itemView.getContext(), data.getTime()));
            message.setText(data.getMessage());
            message.setOnClickListener(c->listener.onItemClick(data,message,i));
            if (data.getToType().equals(Keys.textMessage)){
                getDocumentFromCache(itemView.getContext(), chats.document(data.getTo()), doc -> original.setText(Message.fromDoc((DocumentSnapshot) doc).getMessage()), errorMessage -> {

                });
            }else original.setText(R.string.image);
            original.setOnClickListener(c->originalLister.onSuccess(data.getTo()));
        }
    }

    static class ReplyFromMe extends RecyclerView.ViewHolder{
        final ImageView status;
        final ConstraintLayout nested;
        final ConstraintLayout parent;
        final TextView message;
        final TextView time;
        final TextView original;
        public ReplyFromMe(@NonNull View itemView) {
            super(itemView);
            original = itemView.findViewById(R.id.original);
            parent = itemView.findViewById(R.id.parent);
            status = itemView.findViewById(R.id.status);
            time = itemView.findViewById(R.id.time);
            nested = itemView.findViewById(R.id.nested);
            message = itemView.findViewById(R.id.message);
        }

        void setData(Message data,CollectionReference chats,RecyclerViewItemClickListener listener,SuccessListener originalLister,int i){
            time.setText(Hey.getTimeText(itemView.getContext(), data.getTime()));
            if (data.isRead()) status.setImageResource(R.drawable.ic_read);
            message.setText(data.getMessage());
            message.setOnClickListener(c->listener.onItemClick(data,message,i));
            if (data.getToType().equals(Keys.textMessage)){
                getDocumentFromCache(itemView.getContext(), chats.document(data.getTo()), doc -> original.setText(Message.fromDoc((DocumentSnapshot) doc).getMessage()), errorMessage -> {

                });
            }else original.setText(R.string.image);
            original.setOnClickListener(c->originalLister.onSuccess(data.getTo()));
        }
    }

    static class TextFromMeHolder extends RecyclerView.ViewHolder {
        final TextView message;
        final TextView time;
        final ImageView read;

        public TextFromMeHolder(@NonNull View itemView) {
            super(itemView);
            read = itemView.findViewById(R.id.status);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
        }

        void setChatItem(Message data, RecyclerViewItemClickListener listener, int position) {
            time.setText(Hey.getTimeText(itemView.getContext(), data.time));
            itemView.setOnClickListener(v -> listener.onItemClick(data, itemView, position));
            message.setText(data.message);
            read.setImageResource(data.isRead() ? R.drawable.ic_read : R.drawable.ic_unread);
        }
    }

    static class ImageFromMe extends RecyclerView.ViewHolder {
        final TextView time;
        final TextView imageSize;
        final ImageView imageView;
        final ImageView read;

        public ImageFromMe(@NonNull View itemView) {
            super(itemView);
            imageSize = itemView.findViewById(R.id.imageSize);
            read = itemView.findViewById(R.id.status);
            imageView = itemView.findViewById(R.id.image);
            time = itemView.findViewById(R.id.time);
        }

        void setChatItem(Message data, RecyclerViewItemClickListener listener, RecyclerViewItemLongClickListener longClickListener, int position) {
            time.setText(Hey.getTimeText(itemView.getContext(), data.time));
            itemView.setOnClickListener(v -> listener.onItemClick(data, itemView, position));
            imageSize.setText(Hey.getMb(data.getImageSize()));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(data, itemView, position);
                return true;
            });
            Hey.workWithImageMessage(data, doc -> Hey.loadImage(itemView.getContext(),data,imageView), errorMessage -> { });
            read.setImageResource(data.isRead() ? R.drawable.ic_read : R.drawable.ic_unread);
        }
    }

    static class TextFromOther extends RecyclerView.ViewHolder {
        final TextView message;
        final TextView time;
        final ImageView reply;

        public TextFromOther(@NonNull View itemView) {
            super(itemView);
            reply = itemView.findViewById(R.id.reply);
            time = itemView.findViewById(R.id.time);
            message = itemView.findViewById(R.id.message);
        }

        void setChatItem(Message data, RecyclerViewItemClickListener itemClickListener,RecyclerViewItemClickListener replyListener, int position) {
            time.setText(Hey.getTimeText(itemView.getContext(), data.time));
            message.setText(data.message);
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(data, itemView, position));
            reply.setOnClickListener(x-> replyListener.onItemClick(data,itemView,position));
        }
    }

    static class ImageFromOther extends RecyclerView.ViewHolder {
        final TextView time;
        final TextView imageSize;
        final ImageView imageView;
        final ImageView reply;
        public ImageFromOther(@NonNull View itemView) {
            super(itemView);
            imageSize = itemView.findViewById(R.id.imageSize);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.image);
            reply = itemView.findViewById(R.id.reply);
        }

        void setChatItem(Message data, RecyclerViewItemClickListener itemClickListener,RecyclerViewItemClickListener replyListener,int position) {
            reply.setOnClickListener(c-> replyListener.onItemClick(data,itemView,position));
            time.setText(Hey.getTimeText(itemView.getContext(), data.time));
            imageSize.setText(Hey.getMb(data.getImageSize()));
            Hey.workWithImageMessage(data, doc -> Hey.loadImage(itemView.getContext(), data,imageView), errorMessage -> imageView.setImageResource(R.drawable.download_black_ic));
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(data, itemView, position));
        }
    }

    static class LoadMore extends RecyclerView.ViewHolder {
        final Button button;

        public LoadMore(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.loadMore);
        }

        void setData(ItemClickForQuestion listener, boolean showButton) {
            button.setVisibility(showButton ? View.VISIBLE : View.GONE);
            button.setOnClickListener(view -> {
                Hey.setButtonAsLoading(itemView.getContext(), button);
                listener.onItemClick(0, "", button);
            });
        }
    }

    static class VersionMessage extends RecyclerView.ViewHolder{
        final TextView t;
        public VersionMessage(@NonNull View itemView) {
            super(itemView);
            t = itemView.findViewById(R.id.versionMessage);
        }
    }
}
