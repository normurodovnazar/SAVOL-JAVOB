package com.normurodov_nazar.savol_javob.MyD;

import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.util.HashMap;
import java.util.Map;

public class ImageMessage {
    final String message, time, sender, imageUrl;

    public ImageMessage(String message, String time, String sender, String imageUrl) {
        this.message = message;
        this.time = time;
        this.sender = sender;
        this.imageUrl = imageUrl;
    }

    Map<String, String> toMap() {
        Map<String, String> temp = new HashMap<>();
        temp.put(Keys.message, message);
        temp.put(Keys.time, time);
        temp.put(Keys.sender, sender);
        temp.put(Keys.imageUrl, imageUrl);
        temp.put(Keys.type, Keys.imageMessage);
        return temp;
    }
}
