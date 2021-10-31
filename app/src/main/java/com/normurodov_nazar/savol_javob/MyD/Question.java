package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.util.HashMap;
import java.util.Map;

public class Question {
    long time,sender;
    String subject,theme,number,questionId;
    boolean hiddenUser;
    final Map<String,Object> data;

    public String getQuestionId() {
        return questionId;
    }


    public Question(Map<String, Object> data) {
        this.data = data;
        time = (long) data.get(Keys.time);
        sender = (long) data.get(Keys.sender);
        subject = (String) data.get(Keys.subject);
        theme = (String) data.get(Keys.theme);
        number = (String) data.get(Keys.number);
        hiddenUser = (boolean) data.get(Keys.hidden);
        questionId = sender+""+time;
    }

    public static Question fromDoc(DocumentSnapshot doc){
        Map<String,Object> data = new HashMap<>();
        data.put(Keys.time,doc.get(Keys.time));
        data.put(Keys.sender,doc.get(Keys.sender));
        data.put(Keys.subject,doc.get(Keys.subject));
        data.put(Keys.theme,doc.get(Keys.theme));
        data.put(Keys.number,doc.get(Keys.number));
        data.put(Keys.hidden,doc.get(Keys.hidden));
        return new Question(data);
    }

    public long getTime() {
        return time;
    }

    public long getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getTheme() {
        return theme;
    }

    public String getNumber() {
        return number;
    }

    public boolean isHiddenUser() {
        return hiddenUser;
    }

    public Map<String, Object> toMap() {
        return data;
    }
}
