package com.normurodov_nazar.savol_javob.MyD;

import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private final Object name,surname,imageUrl,seen,number,id,numberOfMyPublishedQuestions,numberOfMyAnswers,numberOfCorrectAnswers,numberOfIncorrectAnswers;
    private final Object chats,questionOpportunity;
    public String fullName;
    public User(Object name, Object surname, Object imageUrl, Object seen, Object number, Object id, Object numberOfMyPublishedQuestions, Object numberOfMyAnswers, Object numberOfCorrectAnswers, Object numberOfIncorrectAnswers, Object chats, Object questionOpportunity) {
        this.name = name;
        this.surname = surname;
        this.imageUrl = imageUrl;
        this.seen = seen;
        this.number = number;
        this.id = id;
        this.numberOfMyPublishedQuestions = numberOfMyPublishedQuestions;
        this.numberOfMyAnswers = numberOfMyAnswers;
        this.numberOfCorrectAnswers = numberOfCorrectAnswers;
        this.numberOfIncorrectAnswers = numberOfIncorrectAnswers;
        this.questionOpportunity = questionOpportunity;
        this.chats = chats;
        fullName = surname+" "+name;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put(Keys.name,name);
        map.put(Keys.surname,surname);
        map.put(Keys.imageUrl,imageUrl);
        map.put(Keys.seen,seen);
        map.put(Keys.number,number);
        map.put(Keys.id,id);
        map.put(Keys.numberOfMyPublishedQuestions,numberOfMyPublishedQuestions);
        map.put(Keys.numberOfMyAnswers,numberOfMyAnswers);
        map.put(Keys.numberOfCorrectAnswers,numberOfCorrectAnswers);
        map.put(Keys.numberOfIncorrectAnswers,numberOfIncorrectAnswers);
        map.put(Keys.myQuestionOpportunity,questionOpportunity);
        map.put(Keys.chats,chats);
        return map;
    }

    public String getName() {
        return name.toString();
    }

    public String getSurname() {
        return surname.toString();
    }

    public String getImageUrl() {
        return imageUrl.toString();
    }

    public long getSeen() {
        return Long.parseLong(seen.toString());
    }

    public String getNumber() {
        return number.toString();
    }

    public long getId() {
        return Long.parseLong(id.toString());
    }

    public long  getNumberOfMyPublishedQuestions() {
        return Long.parseLong(numberOfMyPublishedQuestions.toString());
    }

    public long getNumberOfMyAnswers() {
        return Long.parseLong(numberOfMyAnswers.toString());
    }

    public long getNumberOfCorrectAnswers() {
        return Long.parseLong(numberOfCorrectAnswers.toString());
    }

    public long getNumberOfIncorrectAnswers() {
        return Long.parseLong(numberOfIncorrectAnswers.toString());
    }

    public ArrayList<String> getChats() {
        return (ArrayList<String>) chats;
    }

    public long getQuestionOpportunity() {
        return Long.parseLong(questionOpportunity.toString());
    }
}
