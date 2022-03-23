package com.normurodov_nazar.savol_javob;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.normurodov_nazar.savol_javob.Activities.MainActivity;
import com.normurodov_nazar.savol_javob.Activities.QuestionChat;
import com.normurodov_nazar.savol_javob.Activities.SingleChat;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.MFunctions.My;

import java.util.Map;

public class NotificationService extends FirebaseMessagingService {
  boolean sound,vibrate,privateChat;
  String type;
  Intent i;

  @Override
  public void onNewToken(@NonNull String s) {
    super.onNewToken(s);
    Log.e("token",s);
    SharedPreferences preferences = Hey.getPreferences(this);
    preferences.edit().putString(Keys.token,s).apply();
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    SharedPreferences preferences = Hey.getPreferences(this);
    sound = preferences.getBoolean(Keys.sound,true);
    vibrate = preferences.getBoolean(Keys.vibrate,true);
    privateChat = preferences.getBoolean(Keys.privateChat,true);

    String title,message;
    RemoteMessage.Notification notification = remoteMessage.getNotification();
    if (notification!=null) {
      title = notification.getTitle();
      message = notification.getBody();
    } else {
      title = " ";
      message = " ";
    }
    Map<String,String> data = remoteMessage.getData();
    type = data.get(Keys.type)==null ? "" : data.get(Keys.type);

    String sender = data.get(Keys.sender)==null ? "" : data.get(Keys.sender);
    Hey.print("notificationData",data.toString());
    Hey.print("active","a="+My.activeId);
    if (My.activeId==null){
      i = new Intent(this, MainActivity.class);
      i.putExtra(Keys.type,type);
    }else {
      Hey.print("Active",My.activeId);
      Hey.print("Notif",data.get(Keys.id));
      if (!My.activeId.equals(data.get(Keys.id)==null ? "a":data.get(Keys.id))){
        switch (type){
          case Keys.privateChat:
            i = new Intent(this, SingleChat.class);
            break;
          case Keys.publicQuestions:
          case Keys.needQuestions:
            i = new Intent(this, QuestionChat.class);
            break;
        }
      }
    }
    if (i!=null){
      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      switch (type){
        case Keys.privateChat:
          String uId = data.get(Keys.id)==null ? "-1" : data.get(Keys.id);
          i.putExtra(My.activeId==null ? Keys.id : Keys.chatId,My.activeId==null ?  uId : Hey.getChatIdFromIds(My.id,Long.parseLong(uId)));
          if (privateChat) showNotification(title,message);
          break;
        case Keys.publicQuestions:
        case Keys.needQuestions:
          String idQP = data.get(Keys.id);
          String th = data.get(Keys.theme);
          i.putExtra(Keys.id,idQP).putExtra(Keys.theme,th);
          assert sender != null;
          if (!sender.equals(String.valueOf(Hey.getId(Hey.getPreferences(this))))) showNotification(title,message);
          break;
      }
    }
  }

  void showNotification(String title,String message){
    String channel_id = "QA_notification_channel";
    PendingIntent p = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_ONE_SHOT);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channel_id)
            .setSmallIcon(R.drawable.user_icon)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(p);
    if (sound) {
      Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      if (uri!=null) {
        builder.setSound(uri);
      }
    }
    if (vibrate) {
      builder.setVibrate(new long[]{300});
    }
    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(channel_id,"notification",NotificationManager.IMPORTANCE_HIGH);
      manager.createNotificationChannel(channel);
    }
    manager.notify(0,builder.build());
  }
}

