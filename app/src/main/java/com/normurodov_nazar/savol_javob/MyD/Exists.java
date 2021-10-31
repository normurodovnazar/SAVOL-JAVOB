package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.DocumentSnapshot;

public interface Exists {
    void exists(DocumentSnapshot doc);
    void notExists();
}
