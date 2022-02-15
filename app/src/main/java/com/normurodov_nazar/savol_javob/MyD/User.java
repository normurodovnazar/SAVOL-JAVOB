package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    private final String name,surname,number,token,id;
    private final boolean numberHidden;
    private final Long units,imageSize,seen,numberOfMyPublishedQuestions,numberOfMyAnswers,numberOfCorrectAnswers,numberOfIncorrectAnswers;
    public String fullName,localFileName;

    public String getLocalFileName() {
        return localFileName;
    }

    public User(String name, String surname,Long imageSize, Long seen, String number, String id, Long numberOfMyPublishedQuestions, Long numberOfMyAnswers, Long numberOfCorrectAnswers, Long numberOfIncorrectAnswers, Long questionOpportunity,String token,boolean numberHidden) {
        this.token = token;
        this.imageSize = imageSize;
        this.name = name;
        this.surname = surname;
        this.seen = seen;
        this.number = number;
        this.id = id;
        this.numberOfMyPublishedQuestions = numberOfMyPublishedQuestions;
        this.numberOfMyAnswers = numberOfMyAnswers;
        this.numberOfCorrectAnswers = numberOfCorrectAnswers;
        this.numberOfIncorrectAnswers = numberOfIncorrectAnswers;
        this.units = questionOpportunity;
        this.numberHidden = numberHidden;
        fullName = name+" "+surname;
        localFileName = My.folder + id + imageSize +".png";
        if (name==null || surname==null || number==null || seen==null || imageSize==null ||id==null||numberOfMyPublishedQuestions==null||numberOfMyAnswers==null|| numberOfCorrectAnswers==null||numberOfIncorrectAnswers==null||questionOpportunity==null|| token.equals("a"))
            My.noProblem = false;
    }

    public String getFullName() {
        return fullName;
    }

    public static User fromDoc(DocumentSnapshot doc){
        return new User(doc.getString(Keys.name),doc.getString(Keys.surname),doc.getLong(Keys.imageSize),doc.getLong(Keys.seen),doc.getString(Keys.number),doc.getId(),doc.getLong(Keys.numberOfMyPublishedQuestions),doc.getLong(Keys.numberOfMyAnswers),doc.getLong(Keys.numberOfCorrectAnswers),doc.getLong(Keys.numberOfIncorrectAnswers),doc.getLong(Keys.units),doc.getString(Keys.token),doc.getBoolean(Keys.hidden));
    }

    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put(Keys.name,name);
        map.put(Keys.surname,surname);
        map.put(Keys.imageSize,imageSize);
        map.put(Keys.seen,seen);
        map.put(Keys.number,number);
        map.put(Keys.id,id);
        map.put(Keys.numberOfMyPublishedQuestions,numberOfMyPublishedQuestions);
        map.put(Keys.numberOfMyAnswers,numberOfMyAnswers);
        map.put(Keys.numberOfCorrectAnswers,numberOfCorrectAnswers);
        map.put(Keys.numberOfIncorrectAnswers,numberOfIncorrectAnswers);
        map.put(Keys.units, units);
        map.put(Keys.token,token);
        map.put(Keys.hidden,numberHidden);
        return map;
    }

    public boolean isNumberHidden() {
        return numberHidden;
    }

    public String getName() {
        return name;
    }

    public String getToken(){return token;}

    public String getSurname() {
        return surname;
    }

    public long getImageSize(){return imageSize;}

    public long getSeen() {
        return seen;
    }

    public String getNumber() {
        return number;
    }

    public long getId() {
        return Long.parseLong(id);
    }

    public long  getNumberOfMyPublishedQuestions() {
        return numberOfMyPublishedQuestions;
    }

    public long getNumberOfMyAnswers() {
        return numberOfMyAnswers;
    }

    public long getNumberOfCorrectAnswers() {
        return numberOfCorrectAnswers;
    }

    public long getNumberOfIncorrectAnswers() {
        return numberOfIncorrectAnswers;
    }

    public long getUnits() {
        return units;
    }
}
