package com.normurodov_nazar.savol_javob.MFunctions;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;
import com.normurodov_nazar.savol_javob.MyD.ImageUploadingDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.normurodov_nazar.savol_javob.R.color;
import static com.normurodov_nazar.savol_javob.R.string;

public class Hey {

    public static String getSeenTime(Context context,long time){
        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String full = calendar.get(Calendar.DAY_OF_MONTH)+"-"+getMonth(context,calendar)+(isCurrentYear(calendar) ? "" : ","+calendar.get(Calendar.YEAR)+"-"+context.getString(string.year));
        return getClock(calendar)+","+ (isYesterday(calendar) ? context.getString(string.yesterday) : isToday(calendar) ? context.getString(string.today) : full);
    }

    static boolean isYesterday(Calendar calendar){
        int year,month,day,cYear,cMonth,cDay;
        Calendar c = Calendar.getInstance();cYear = c.get(Calendar.YEAR);cMonth = c.get(Calendar.MONTH);cDay = c.get(Calendar.DAY_OF_MONTH);
        year = calendar.get(Calendar.YEAR);month = calendar.get(Calendar.MONTH);day = calendar.get(Calendar.DAY_OF_MONTH);
        return year==cYear && month==cMonth && day==cDay-1;
    }

    static boolean isToday(Calendar calendar){
        int year,month,day,cYear,cMonth,cDay;
        Calendar c = Calendar.getInstance();cYear = c.get(Calendar.YEAR);cMonth = c.get(Calendar.MONTH);cDay = c.get(Calendar.DAY_OF_MONTH);
        year = calendar.get(Calendar.YEAR);month = calendar.get(Calendar.MONTH);day = calendar.get(Calendar.DAY_OF_MONTH);
        return year==cYear && month==cMonth && day==cDay;
    }

    static boolean isCurrentYear(Calendar calendar){
        int year=calendar.get(Calendar.YEAR),cYear = Calendar.getInstance().get(Calendar.YEAR);
        return year==cYear;
    }

    static String getMonth(Context context,Calendar calendar){
        int i = calendar.get(Calendar.MONTH);
        String m="";
        switch (i){
            case Calendar.JANUARY:m = context.getString(string.january);break;
            case Calendar.FEBRUARY:m = context.getString(string.february);break;
            case Calendar.MARCH:m = context.getString(string.march);break;
            case Calendar.APRIL:m = context.getString(string.april);break;
            case Calendar.MAY:m = context.getString(string.may);break;
            case Calendar.JUNE:m = context.getString(string.june);break;
            case Calendar.JULY:m = context.getString(string.july);break;
            case Calendar.AUGUST:m = context.getString(string.august);break;
            case Calendar.SEPTEMBER:m = context.getString(string.september);break;
            case Calendar.OCTOBER:m = context.getString(string.october);break;
            case Calendar.NOVEMBER:m = context.getString(string.november);break;
            default: m = context.getString(string.december);break;
        }
        return m;
    }

    static String getClock(Calendar calendar){
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY),minute = calendar.get(Calendar.MINUTE);
        return hourOfDay+":"+minute;
    }

    public static String getFriendsIdFromChatId(String chatId){
        if(chatId.contains(My.id+"+")) return chatId.replace(My.id+"+",""); else return chatId.replace("+"+My.id,"");
    }

    public static String getChatIdFromIds(long a,long b){
        if(a>b) return a+"+"+ b; else return b +"+"+a;
    }

    public static void showToast(Context context,String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void print(String tag,String message){
        Log.e(tag,message);
    }

    public static MyDialog showAlertDialog(Context context, String message) {
        MyDialog dialog = new MyDialog(context, message);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static MyDialog showUnknownError(Context context) {
        return showAlertDialog(context, context.getString(string.error_unknown) + context.getString(string.unknown));
    }

    public static MyDialogWithTwoButtons showSelectorDialog(Context context,String yes,String no,String info){
        MyDialogWithTwoButtons dialog = new MyDialogWithTwoButtons(context,yes,no,info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static ImageUploadingDialog uploadImageForProfile(Context context,String filePath,String uploadAs){
        ImageUploadingDialog dialog = new ImageUploadingDialog(context,filePath,uploadAs,false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static ImageUploadingDialog uploadImageToChat(Context context,String filePath,String uploadAs){
        ImageUploadingDialog dialog = new ImageUploadingDialog(context,filePath,uploadAs,true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static int getPercentage(FileDownloadTask.TaskSnapshot snapshot){
        long l = 100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
        Hey.print("a",snapshot.getBytesTransferred()+"/"+snapshot.getTotalByteCount());
        return (int) l;
    }

    public static void showUnknownErrorWithToast(Context context){
        Toast.makeText(context, context.getString(string.error_unknown)+context.getString(string.unknown), Toast.LENGTH_SHORT).show();
    }

    public static void setButtonAsLoading(@NonNull Context context, @NonNull Button button,boolean loading) {
        loading = true;
        button.setText(context.getText(string.wait));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setBackgroundResource(R.drawable.button_bg_pressed);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setTextColor(context.getColor(color.white));
        }
        Animation animation = new AlphaAnimation(0,1);
        animation.setDuration(600);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        button.startAnimation(animation);
    }

    public static void setIconButtonAsLoading(@NonNull Context context, @NonNull View button,boolean loading){
        loading = true;
        Animation animation = new AlphaAnimation(0,1);
        animation.setDuration(600);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        button.startAnimation(animation);
    }

    public static void setIconButtonAsDefault(Context context, @NonNull View button,boolean loading){
        button.clearAnimation();
        loading = false;
    }

    public static void setButtonAsDefault(Context context, @NonNull Button button, @NonNull String title,boolean loading) {
        button.setBackgroundResource(R.drawable.sss);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setTextColor(context.getColor(color.black));
        }
        button.setText(title);
        button.clearAnimation();
        loading = false;
    }

    public static void animateHorizontally(@NonNull View view, float x, long startAfter) {
        view.setAlpha(0f);
        view.setTranslationX(x);
        view.animate().alpha(1f).translationX(0).setDuration(1000).setStartDelay(startAfter).start();
    }

    public static void animateVertically(@NonNull View view, float y, long startAfter) {
        view.setAlpha(0f);
        view.setTranslationY(y);
        view.animate().alpha(1f).translationY(0).setDuration(1000).setStartDelay(startAfter).start();
    }

    public static void animateFadeOut(@NonNull View view, long startAfter) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(1500).setStartDelay(startAfter).start();
    }

    @NonNull
    public static Task<DocumentSnapshot> amIOnline(){
        return FirebaseFirestore.getInstance().collection(Keys.appNumber).document(Keys.appNumber).get();
    }

    public static long getId(SharedPreferences preferences){
        return preferences.getLong(Keys.id,-1);
    }

    public static Boolean isLoggedIn(@NonNull SharedPreferences preferences){
        return preferences.getBoolean(Keys.logged,false);
    }

    public static void animateHorizontal(@NonNull View view, float toWhere, long delay){
        view.animate().translationX(toWhere).setDuration(500).setStartDelay(delay).start();
    }

    public static SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences(Keys.sharedPreferences,Context.MODE_PRIVATE);
    }

    public static int generateID(){
        Random r = new Random();
        return Math.abs(r.nextInt());
    }
}