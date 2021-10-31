package com.normurodov_nazar.savol_javob.MyD;

import com.google.firebase.firestore.DocumentSnapshot;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;

public class Item {
    final String name;

    public Item(String name) {
        this.name = name;
    }


    public static Item fromDoc(DocumentSnapshot doc){
        String name = (String) doc.getId();
        return new Item(name);
    }

    public String getName() {
        return name;
    }
}
