package com.normurodov_nazar.savol_javob;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.dynamicanimation.animation.SpringAnimation;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.normurodov_nazar.savol_javob.Activities.Home;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;

import java.util.Objects;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("token",s);
        SharedPreferences preferences = getSharedPreferences(Keys.sharedPreferences,MODE_PRIVATE);
        preferences.edit().putString(Keys.token,s).apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("notification","notification called");
        showNotification(Objects.requireNonNull(remoteMessage.getNotification()).getTitle(),remoteMessage.getNotification().getBody());
    }

    void showNotification(String title,String message){
        Intent i = new Intent(this, Home.class);
        String channel_id = "QA_notification_channel";
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent p = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channel_id)
                .setSmallIcon(R.drawable.tab1_icon)
                .setAutoCancel(false)
                .setVibrate(new long[]{600,600,600})
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.tab1_icon)
                .setContentIntent(p);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_id,"notification",NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0,builder.build());


    }
}
