package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.util.HashMap;
import java.util.Map;

public class Question {
    final Long time;
    final Long sender;
    final Long visibleTime;
    final String message;
    final String questionId;
    final String theme;
    final Map<String,Object> data;

    public String getQuestionId() {
        return questionId;
    }


    public Question(Map<String, Object> data) {
        this.data = data;
        time = (long) data.get(Keys.time);
        sender = (long) data.get(Keys.sender);
        message = (String) data.get(Keys.message);
        theme = (String) data.get(Keys.theme);
        visibleTime = (long) data.get(Keys.visibleTime);
        questionId = sender+""+time;
    }

    public static Question fromDoc(DocumentSnapshot doc){
        Map<String,Object> data = new HashMap<>();
        data.put(Keys.time,doc.get(Keys.time));
        data.put(Keys.sender,doc.get(Keys.sender));
        data.put(Keys.message,doc.get(Keys.message));
        data.put(Keys.theme,doc.get(Keys.theme));
        data.put(Keys.visibleTime,doc.get(Keys.visibleTime));
        return new Question(data);
    }

    public long getTime() {
        return time;
    }

    public long getSender() {
        return sender;
    }

    public String getNumber() {
        return message;
    }

    public Map<String, Object> toMap() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getTheme() {
        return theme;
    }

    public long getVisibleTime() {
        return visibleTime;
    }
}
