package com.normurodov_nazar.savol_javob.MFunctions;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SnapshotMetadata;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.R;

import static com.normurodov_nazar.savol_javob.R.color;
import static com.normurodov_nazar.savol_javob.R.string;

public class Hey {

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

    public static void showUnknownErrorWithToast(Context context){
        Toast.makeText(context, context.getString(string.error_unknown)+context.getString(string.unknown), Toast.LENGTH_SHORT).show();
    }

    public static void setButtonAsLoading(@NonNull Context context, @NonNull Button button) {
        My.loading = true;
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f), fadeIn = new AlphaAnimation(0f, 1f);
        button.setText(context.getText(string.wait));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setBackgroundResource(R.drawable.button_bg_pressed);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setTextColor(context.getColor(color.white));
        }
        fadeOut.setDuration(800);
        fadeIn.setDuration(800);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }


            @Override
            public void onAnimationEnd(Animation animation) {
                if (My.loading) button.startAnimation(fadeIn);
            }
        });
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (My.loading) button.startAnimation(fadeOut);
            }

        });
        button.startAnimation(fadeOut);
    }

    public static void setButtonAsDefault(Context context, @NonNull Button button, @NonNull String title) {
        button.setBackgroundResource(R.drawable.sss);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setTextColor(context.getColor(color.black));
        }
        button.setText(title);
        button.clearAnimation();
        My.loading = false;
    }

    public static void animateHorizontally(View view, float x, long startAfter) {
        view.setAlpha(0f);
        view.setTranslationX(x);
        view.animate().alpha(1f).translationX(0).setDuration(1000).setStartDelay(startAfter).start();
    }

    public static void animateVertically(View view, float y, long startAfter) {
        view.setAlpha(0f);
        view.setTranslationY(y);
        view.animate().alpha(1f).translationY(0).setDuration(1000).setStartDelay(startAfter).start();
    }

    public static void animateFadeOut(View view, long startAfter) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(1500).setStartDelay(startAfter).start();
    }

    public static Task<DocumentSnapshot> newUserOrNot(String uId) {
        return FirebaseFirestore.getInstance().collection(Keys.users).document(uId).get();
    }

    public static Task<DocumentSnapshot> amIOnline(){
        return FirebaseFirestore.getInstance().collection(Keys.appNumber).document(Keys.appNumber).get();
    }

    public static byte getAppNumber(){
        return (byte) 254;
    }

    public static boolean isLoggedIn(SharedPreferences preferences){
        boolean a = preferences.getBoolean(Keys.logged,false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean c = user==null;
        if(!c) My.setFirebaseUser(user);
        return a;
    }
}