package com.normurodov_nazar.adminformath.MyD;

import com.google.firebase.firestore.DocumentSnapshot;

public interface Exists {
    void exists(DocumentSnapshot doc);
    void notExists();
}
