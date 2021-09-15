package com.normurodov_nazar.savol_javob.MyD;

import java.io.Serializable;

public class User implements Serializable {
    private final String name,surname,imageUrl,seen,number,uId,numberOfMyPublishedQuestions,numberOfMyAnswers,numberOfCorrectAnswers,numberOfIncorrectAnswers;

    public User(String name, String surname, String imageUrl, String seen, String number, String uId, String numberOfMyPublishedQuestions, String numberOfMyAnswers, String numberOfCorrectAnswers, String numberOfIncorrectAnswers) {
        this.name = name;
        this.surname = surname;
        this.imageUrl = imageUrl;
        this.seen = seen;
        this.number = number;
        this.uId = uId;
        this.numberOfMyPublishedQuestions = numberOfMyPublishedQuestions;
        this.numberOfMyAnswers = numberOfMyAnswers;
        this.numberOfCorrectAnswers = numberOfCorrectAnswers;
        this.numberOfIncorrectAnswers = numberOfIncorrectAnswers;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSeen() {
        return seen;
    }

    public String getNumber() {
        return number;
    }

    public String getuId() {
        return uId;
    }

    public String getNumberOfMyPublishedQuestions() {
        return numberOfMyPublishedQuestions;
    }

    public String getNumberOfMyAnswers() {
        return numberOfMyAnswers;
    }

    public String getNumberOfCorrectAnswers() {
        return numberOfCorrectAnswers;
    }

    public String getNumberOfIncorrectAnswers() {
        return numberOfIncorrectAnswers;
    }
}
