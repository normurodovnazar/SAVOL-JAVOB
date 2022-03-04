package com.normurodov_nazar.adminformath.MyD;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.util.Map;

public class Message {

    final Map<String,Object> data;
    final String message;
    String id;
    long sender,time,incorrect,correct,imageSize;
    boolean read;

    /**
     * Used for sending messages
     * @param data map for message data
     */
    public Message(Map<String, Object> data) {
        this.data = data;
        message = (String) data.get(Keys.message);
        sender = (long) data.get(Keys.sender);
        time = (long) data.get(Keys.time);
        read = (boolean) data.get(Keys.read);
        Object c = data.get(Keys.correct),i = data.get(Keys.incorrect);
        correct = c==null ? 0 : (long) c;
        incorrect = i==null ? 0 : (long) i;
        Long a = (Long) data.get(Keys.imageSize);
        imageSize = a==null ? -1 : a;
        id = sender+""+time;
    }

    public Message(Map<String, Object> data,String a){
        this.data = data;
        message = (String) data.get(Keys.message);
    }

    public long getImageSize() {
        return imageSize;
    }

    public Map<String,Object> toMap(){
        return data;
    }

    public static Message fromDoc(DocumentSnapshot doc){
        Map<String,Object> data = doc.getData();
        assert data != null;
        return new Message(data);
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public long getSender() {
        return sender;
    }

    public String getType(){return (String) data.get(Keys.type);}

    public boolean isRead(){return read;}
}
