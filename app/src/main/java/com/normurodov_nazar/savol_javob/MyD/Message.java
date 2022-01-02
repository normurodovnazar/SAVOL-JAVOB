package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.util.Map;

public class Message {

    final Map<String,Object> data;
    String message,id;
    long sender,time;

    public Message(Map<String, Object> data) {
        this.data = data;
        message = (String) data.get(Keys.message);
        sender = (long) data.get(Keys.sender);
        time = (long) data.get(Keys.time);
        id = sender+""+time;
    }

    public void setMessage(String message) {
        this.message = message;
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
}
