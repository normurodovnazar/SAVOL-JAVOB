package com.normurodov_nazar.savol_javob.MFunctions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MyD.User;

public class My {
    public static String number,name,surname,imageUrl,fullName;

    public static long id,seen,myQuestionOpportunity,numberOfMyPublishedQuestions,numberOfMyAnswers,numberOfCorrectAnswers,numberOfIncorrectAnswers;

    public static FirebaseAuth auth;

    public static boolean timedOut = false,verificationCompleted=false;

    public static void setDataFromDoc(DocumentSnapshot doc){
        Object SN = doc.get(Keys.seen),iD = doc.getId(),iU = doc.get(Keys.imageUrl),n = doc.get(Keys.number),nm = doc.get(Keys.name),sm = doc.get(Keys.surname),mQO = doc.get(Keys.myQuestionOpportunity),nOMPQ = doc.get(Keys.numberOfMyPublishedQuestions),
        nOMA = doc.get(Keys.numberOfMyAnswers),nOCA = doc.get(Keys.numberOfCorrectAnswers),nOIA = doc.get(Keys.numberOfIncorrectAnswers);
        if(n!=null) number = n.toString(); if(nm!=null) name=nm.toString(); if(sm!=null) surname = sm.toString();
        if(mQO!=null) myQuestionOpportunity = (long) mQO; if(nOMPQ!=null) numberOfMyPublishedQuestions = (long) nOMPQ;
        if(nOMA!=null) numberOfMyAnswers = (long) nOMA; if(nOCA!=null) numberOfCorrectAnswers = (long) nOCA;
        if(nOIA!=null) numberOfIncorrectAnswers = (long) nOIA;if(iU!=null) imageUrl = iU.toString();
        id = Long.parseLong(iD.toString());if(SN!=null) seen = (long) SN;
        fullName = surname+" "+name;
    }

    public static void setDataFromUser(User user){
        number = user.getNumber();
        name = user.getName();surname = user.getSurname();imageUrl = user.getImageUrl();id = user.getId();seen = user.getSeen();
        myQuestionOpportunity = user.getQuestionOpportunity();
        numberOfMyPublishedQuestions = user.getNumberOfMyPublishedQuestions();numberOfMyAnswers = user.getNumberOfMyAnswers();
        numberOfCorrectAnswers = user.getNumberOfCorrectAnswers();numberOfIncorrectAnswers = user.getNumberOfIncorrectAnswers();id = user.getId();
        fullName = surname+" "+name;
    }
}
