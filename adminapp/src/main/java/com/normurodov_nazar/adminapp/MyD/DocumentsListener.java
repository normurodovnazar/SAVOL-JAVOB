package com.normurodov_nazar.adminapp.MyD;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public interface DocumentsListener {
    void onDocuments(List<DocumentSnapshot> docs);
}
