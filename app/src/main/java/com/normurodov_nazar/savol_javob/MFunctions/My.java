package com.normurodov_nazar.savol_javob.MFunctions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MyD.User;

import java.util.ArrayList;

public class My {
    public static String number,name,surname,fullName;

    public static long unitsForAd,imageSize,id,seen, units,numberOfMyPublishedQuestions,numberOfMyAnswers,numberOfCorrectAnswers,numberOfIncorrectAnswers,questionLimit,unitsForPerDay;

    public static FirebaseAuth auth;

    public static boolean timedOut = false,verificationCompleted=false;
    public static int width;
    public static String folder;
    public static String token;
    public static String activeId = "";
    public static ArrayList<String> result = new ArrayList<>();
    public static User user;
    public static String theme;
    public static boolean noProblem = true;
    public static String petName;
    public static boolean actionCompleted = false;
    public static boolean updateSuccess = false;

    public static void setDataFromDoc(DocumentSnapshot doc){
        Long SN = doc.getLong(Keys.seen),iU = doc.getLong(Keys.imageSize),mQO = doc.getLong(Keys.units),nOMPQ = doc.getLong(Keys.numberOfMyPublishedQuestions),
                nOMA = doc.getLong(Keys.numberOfMyAnswers),nOCA = doc.getLong(Keys.numberOfCorrectAnswers),nOIA = doc.getLong(Keys.numberOfIncorrectAnswers);
        String T = doc.getString(Keys.token),n = doc.getString(Keys.number),iD = doc.getId(),nm = doc.getString(Keys.name),sm = doc.getString(Keys.surname);
        if(n!=null) number = n; else noProblem = true;
        if(nm!=null) name= nm; else noProblem = true;
        if(sm!=null) surname = sm;else noProblem = true;
        if(mQO!=null) units = mQO;else noProblem = true;
        if(nOMPQ!=null) numberOfMyPublishedQuestions = nOMPQ;else noProblem = true;
        if(nOMA!=null) numberOfMyAnswers = nOMA; else noProblem = true;
        if(nOCA!=null) numberOfCorrectAnswers = nOCA;else noProblem = true;
        if(nOIA!=null) numberOfIncorrectAnswers = nOIA;else noProblem = true;
        if(iU!=null) imageSize = iU;else noProblem = true;
        id = Long.parseLong(iD);
        if(SN!=null) seen = SN;else noProblem = true;
        if (T!=null) token = T; else noProblem = true;
        fullName = name+" "+surname;
    }

    public static void setDataFromUser(User user){
        My.user = user;
        number = user.getNumber();token = user.getToken();
        name = user.getName();surname = user.getSurname();imageSize = user.getImageSize();id = user.getId();seen = user.getSeen();
        units = user.getUnits();
        numberOfMyPublishedQuestions = user.getNumberOfMyPublishedQuestions();numberOfMyAnswers = user.getNumberOfMyAnswers();
        numberOfCorrectAnswers = user.getNumberOfCorrectAnswers();numberOfIncorrectAnswers = user.getNumberOfIncorrectAnswers();id = user.getId();
        fullName = surname+" "+name;
    }
}
