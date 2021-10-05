package com.normurodov_nazar.savol_javob.MyD;

import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.util.HashMap;
import java.util.Map;

public class TextMessage {
    final String message;
    final long time,sender;

    public TextMessage(String message, long time, long sender) {
        this.message = message;
        this.time = time;
        this.sender = sender;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> temp = new HashMap<>();
        temp.put(Keys.message,message);
        temp.put(Keys.time,time);
        temp.put(Keys.sender,sender);
        temp.put(Keys.type,Keys.textMessage);
        return temp;
    }
}
