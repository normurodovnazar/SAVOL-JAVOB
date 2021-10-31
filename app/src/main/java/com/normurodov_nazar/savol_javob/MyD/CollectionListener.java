package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public interface CollectionListener {
    void result(ArrayList<Message> messages);
}
