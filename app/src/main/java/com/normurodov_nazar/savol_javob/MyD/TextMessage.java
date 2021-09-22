package com.normurodov_nazar.savol_javob.MyD;

import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.util.HashMap;
import java.util.Map;

public class TextMessage {
    final String message,time,sender;

    public TextMessage(String message, String time, String sender) {
        this.message = message;
        this.time = time;
        this.sender = sender;
    }

    public Map<String,String> toMap(){
        Map<String,String> temp = new HashMap<>();
        temp.put(Keys.message,message);
        temp.put(Keys.time,time);
        temp.put(Keys.sender,sender);
        temp.put(Keys.type,Keys.textMessage);
        return temp;
    }
}
