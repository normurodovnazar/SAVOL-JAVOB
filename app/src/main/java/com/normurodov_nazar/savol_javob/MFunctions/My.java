package com.normurodov_nazar.savol_javob.MFunctions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class My {
    public static String number,name,surname,url,logged,uId;

    public static FirebaseAuth auth;

    public static boolean loading=false;

    public static boolean timedOut = false,verificationCompleted=false;

    public static FirebaseUser firebaseUser;

    public static void setFirebaseUser(FirebaseUser user){
        firebaseUser = user;
        uId = user.getUid();
    }
}
