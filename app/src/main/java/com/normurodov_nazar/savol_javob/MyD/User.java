package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private final Object name,surname,imageUrl,seen,number,id,numberOfMyPublishedQuestions,numberOfMyAnswers,numberOfCorrectAnswers,numberOfIncorrectAnswers;
    private final Object questionOpportunity;
    public String fullName,localFileName;

    public String getLocalFileName() {
        return localFileName;
    }

    public User(Object name, Object surname, Object imageUrl, Object seen, Object number, Object id, Object numberOfMyPublishedQuestions, Object numberOfMyAnswers, Object numberOfCorrectAnswers, Object numberOfIncorrectAnswers, Object questionOpportunity) {
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
        fullName = surname+" "+name;
        localFileName = id + imageUrl.toString().substring(imageUrl.toString().length() - 5)+".png";
    }

    public String getFullName() {
        return fullName;
    }

    public static User fromDoc(DocumentSnapshot doc){
        return new User(doc.get(Keys.name),doc.get(Keys.surname),doc.get(Keys.imageUrl),doc.get(Keys.seen),doc.get(Keys.number),doc.get(Keys.id),doc.get(Keys.numberOfMyPublishedQuestions),doc.get(Keys.numberOfMyAnswers),doc.get(Keys.numberOfCorrectAnswers),doc.get(Keys.numberOfIncorrectAnswers),doc.get(Keys.myQuestionOpportunity));
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

    public long getQuestionOpportunity() {
        return Long.parseLong(questionOpportunity.toString());
    }
}
