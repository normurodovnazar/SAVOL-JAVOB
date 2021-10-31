package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface DocumentsListener {
    void onDocuments(List<DocumentSnapshot> docs);
}
