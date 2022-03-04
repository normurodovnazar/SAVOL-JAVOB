package com.normurodov_nazar.adminformath.MyD;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface DocumentsListener {
    void onDocuments(List<DocumentSnapshot> docs);
}
