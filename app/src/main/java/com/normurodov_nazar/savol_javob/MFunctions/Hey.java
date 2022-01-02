package com.normurodov_nazar.savol_javob.MFunctions;

import static com.normurodov_nazar.savol_javob.R.color;
import static com.normurodov_nazar.savol_javob.R.string;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.normurodov_nazar.savol_javob.MyD.AddItemDialog;
import com.normurodov_nazar.savol_javob.MyD.CollectionListener;
import com.normurodov_nazar.savol_javob.MyD.DocumentSnapshotListener;
import com.normurodov_nazar.savol_javob.MyD.DocumentsListener;
import com.normurodov_nazar.savol_javob.MyD.EditMessageDialog;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.Exists;
import com.normurodov_nazar.savol_javob.MyD.ImageDownloadingDialog;
import com.normurodov_nazar.savol_javob.MyD.ImageUploadingDialog;
import com.normurodov_nazar.savol_javob.MyD.ItemClickListener;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.ProgressListener;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Hey {

    public static Message biggerListsLastElement(ArrayList<Message> m1,ArrayList<Message> m2){
        ArrayList<Message> bigger = m1.size()>m2.size() ? m1 : m2;
        return bigger.get(bigger.size()-1);
    }

    public static boolean isSimilar(ArrayList<Message> m1,ArrayList<Message> m2){
        boolean matches = true;
        int limit;
        if (m1.size()==0 || m2.size()==0) return false; else {
            limit = Math.min(m1.size(), m2.size());
            Hey.print("limit", String.valueOf(limit));
            for (int i=0;i<limit;i++){
                Hey.print("messages", String.valueOf(m1.get(i).getId()));
                Hey.print("oldMessages", String.valueOf(m2.get(i).getId()));
                if (!m1.get(i).getId().equals(m2.get(i).getId())) {
                    Hey.print("xxxxxxxxxxx", m1.get(i).getId() +" xxx "+ m1.get(i).getId());
                    matches = false;
                    break;
                }
            }
            Hey.print("matches", String.valueOf(matches));
            return matches;
        }
    }

    public static void addItem(Context context,CollectionReference reference,String user){
        AddItemDialog dialog = new AddItemDialog(context, reference, user);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void downloadFile(Context context,String folder, String fileName, File destinationFile, ProgressListener progressListener, SuccessListener successListener, ErrorListener errorListener){
        Hey.print("downloadingFileTo",destinationFile.getPath());
        FirebaseStorage.getInstance().getReference().child(folder).child(fileName).getFile(destinationFile).addOnFailureListener(e -> {
            Hey.showAlertDialog(context,context.getString(R.string.error_download_file)+":"+e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage()));
        }).addOnProgressListener(snapshot -> progressListener.onProgressChanged(snapshot.getBytesTransferred(),snapshot.getTotalByteCount())).addOnSuccessListener(taskSnapshot -> successListener.onSuccess(null));
    }

    public static void addDocumentListener(Context context, DocumentReference doc, DocumentSnapshotListener listener, ErrorListener errorListener){
        doc.addSnapshotListener((value, error) -> {
            if(value!=null){
                listener.documentSnapshot(value);
            }else if(error!=null) Hey.showAlertDialog(context,context.getString(string.error)+":"+ error.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(error.getLocalizedMessage()));
            else showUnknownError(context).setOnDismissListener(dialog -> {
                    assert false;
                    errorListener.onError(error.getLocalizedMessage());
                });
        });
    }

    public static void addToChats(Context context,long id,long otherId){
        CollectionReference collection = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(id)).collection(Keys.chats);
        collectionListener(context, collection, docs -> {
            boolean contains = false;
            for (DocumentSnapshot doc : docs) {
                if(Long.parseLong(doc.getId())==otherId) contains=true;
            }
            if(!contains) addDocumentToCollection(context, collection, String.valueOf(otherId), new HashMap<>(), doc -> {
                print("A",otherId+" added to "+id);
            }, errorMessage -> {

            });
        }, errorMessage -> {

        });
    }

    public static void addDocumentToCollection(Context context,CollectionReference collectionReference, String documentId, Map<String,Object> data,SuccessListener successListener,ErrorListener errorListener){
        collectionReference.document(documentId).set(data).addOnSuccessListener(unused -> {
            successListener.onSuccess(null);
        }).addOnFailureListener(e -> showAlertDialog(context,context.getString(string.error)+":"+e.getLocalizedMessage()).setOnDismissListener(dialog -> {errorListener.onError(e.getLocalizedMessage());}));
    }

    public static EditMessageDialog editMessage(Context context,Message message,CollectionReference chats,SuccessListener successListener){
        EditMessageDialog dialog = new EditMessageDialog(context,message,chats,successListener);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static void getDocument(Context context, DocumentReference doc, SuccessListener documentListener, ErrorListener errorListener){
        doc.addSnapshotListener((value, error) -> {
            if(value!=null){
                documentListener.onSuccess(value);
            }else {
                if (error != null) Hey.showAlertDialog(context,context.getString(string.error)+":"+error.getLocalizedMessage()); else showUnknownError(context);
                errorListener.onError(error != null ? error.getLocalizedMessage() : context.getString(string.unknown));
            }
        });
    }

    /**
     * called listener every updates in this chat
     */
    public static ListenerRegistration addMessagesListener(Context context, CollectionReference collectionReference, CollectionListener collectionListener, ErrorListener errorListener){
        return collectionReference.orderBy(Keys.time, Query.Direction.ASCENDING).addSnapshotListener((value, error) -> {
            if(value!=null){
                List<DocumentSnapshot> list = value.getDocuments();
                ArrayList<Message> messages = new ArrayList<>();
                for(DocumentSnapshot d : list){
                    messages.add(Message.fromDoc(d));
                }
                collectionListener.result(messages);
            }else if(error!=null) Hey.showAlertDialog(context,context.getString(string.error)+":"+ error.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(error.getLocalizedMessage()));
            else showUnknownError(context).setOnDismissListener(dialog -> {
                    assert false;
                    errorListener.onError(error.getLocalizedMessage());
                });
        });
    }

    public static void collectionListener(Context context, CollectionReference collectionReference, DocumentsListener documentsListener, ErrorListener errorListener){
        collectionReference.addSnapshotListener((value, error) -> {
            if(value!=null){
                documentsListener.onDocuments(value.getDocuments());
            }else if(error!=null) Hey.showAlertDialog(context,context.getString(string.error)+":"+ error.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(error.getLocalizedMessage()));
            else showUnknownError(context).setOnDismissListener(dialog -> {
                    assert false;
                    errorListener.onError(error.getLocalizedMessage());
                });
        });
    }

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
        String m;
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
        return (String.valueOf(hourOfDay).length()==1 ? "0"+hourOfDay : hourOfDay) +":"+(String.valueOf(minute).length()==1 ? "0"+minute : minute);
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

    public static MyDialogWithTwoButtons showDeleteDialog(Context context, String info, Message message){
        MyDialogWithTwoButtons dialog = new MyDialogWithTwoButtons(context,info,message);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static LoadingDialog showLoadingDialog(Context context){
        LoadingDialog loadingDialog = new LoadingDialog(context);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false);
        loadingDialog.show();
        return  loadingDialog;
    }

    public static void showDeleteImageDialog(Context context,Message message,ErrorListener errorListener,SuccessListener successListener) {
        LoadingDialog dialog = showLoadingDialog(context);
        FirebaseStorage.getInstance().getReference().child(Keys.chats).child(message.getId()).delete().addOnFailureListener(e -> {
            Hey.showAlertDialog(context,context.getString(string.error)+":"+e.getLocalizedMessage());
            errorListener.onError(e.getLocalizedMessage());
            dialog.dismiss();
        }).addOnCompleteListener(task -> {
            successListener.onSuccess(null);
            dialog.dismiss();
        });
    }

    public static ImageDownloadingDialog showDownloadDialog(Context context,Message message,SuccessListener successListener,ErrorListener errorListener) {
        ImageDownloadingDialog dialog = new ImageDownloadingDialog(context,message,errorListener,successListener);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static ImageUploadingDialog uploadImageForProfile(Context context, String filePath, String uploadAs, SuccessListener listener, ItemClickListener cancelListener){
        ImageUploadingDialog dialog = new ImageUploadingDialog(context,filePath,uploadAs,false,listener,cancelListener);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static ImageUploadingDialog uploadImageToChat(Context context,String filePath,String uploadAs,SuccessListener listener, ItemClickListener cancelListener){
        ImageUploadingDialog dialog = new ImageUploadingDialog(context,filePath,uploadAs,true,listener,cancelListener);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static int getPercentage(long progress,long total){
        long l = 100*progress/total;
        Hey.print("a",progress+"/"+total);
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

    public static void setIconButtonAsLoading(@NonNull View button){
            Animation animation = new AlphaAnimation(0, 1);
            animation.setDuration(600);
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.REVERSE);
            button.startAnimation(animation);
    }

    public static void setIconButtonAsDefault(@NonNull View button){
        button.clearAnimation();
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
    public static void myNumberExists(Exists exists){
        FirebaseFirestore.getInstance().collection(Keys.users).whereEqualTo(Keys.number,My.number).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size()==0){
                exists.notExists();
            }else exists.exists(queryDocumentSnapshots.getDocuments().get(0));
        });
    }
    public static void amIOnline(StatusListener statusListener,ErrorListener errorListener, Context context){
        FirebaseFirestore.getInstance().collection(Keys.appNumber).document(Keys.appNumber).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.getMetadata().isFromCache()) statusListener.offline(); else statusListener.online();
        }).addOnFailureListener(e -> {
            if(e.getLocalizedMessage()!=null){
                if(!e.getLocalizedMessage().equals("Failed to get document because the client is offline.")) showAlertDialog(context,context.getString(string.error)+":"+e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage()));
                else statusListener.offline();
            }
        });
    }

    public static void isDocumentExists(Context context,DocumentReference doc,Exists exists,ErrorListener errorListener){
        doc.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) exists.exists(documentSnapshot); else exists.notExists();
        }).addOnFailureListener(e -> showAlertDialog(context,context.getString(string.error)+":"+e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage())));
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

    public static void deleteDocument(Context context, DocumentReference doc, SuccessListener successListener) {
        print("a","delete started");
        doc.delete().addOnFailureListener(e -> showAlertDialog(context,context.getString(string.error_deleting)+e.getLocalizedMessage()))
        .addOnSuccessListener(unused -> {
            print("a","Document deleted");
            successListener.onSuccess(null);
        });
    }

    /**
     *
     * @param context
     * @param chats collection to add message
     * @param message message to send
     * @param successListener called when message sent
     * @param errorListener called when error occurs
     */
    public static void sendMessage(Context context, CollectionReference chats,Message message,SuccessListener successListener,ErrorListener errorListener) {
        print("a","send message");
        amIOnline(new StatusListener() {
            @Override
            public void online() {
                print("a","i online");
                chats.document(message.getId()).set(message.toMap()).addOnSuccessListener(unused -> successListener.onSuccess(null)).addOnFailureListener(e -> {
                            showToast(context,context.getString(string.error)+":"+e.getLocalizedMessage());
                            errorListener.onError(e.getLocalizedMessage());
                        });
            }

            @Override
            public void offline() {
                Toast.makeText(context, context.getString(string.error_connection), Toast.LENGTH_SHORT).show();
                errorListener.onError("");
            }
        }, errorListener,context);


    }

    public static void pickImage(ActivityResultLauncher<Intent> p) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        p.launch(i);
    }

    public static void cropImage(Context context, Activity activity, Uri uri,File targetFile,boolean forProfile,ErrorListener errorListener) {
        UCrop.Options options = new UCrop.Options();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int blackColor = context.getColor(R.color.black);
            int whiteColor = context.getColor(R.color.white);
            options.setRootViewBackgroundColor(blackColor);
            options.setStatusBarColor(blackColor);
            options.setLogoColor(blackColor);
            options.setActiveControlsWidgetColor(whiteColor);
            options.setToolbarWidgetColor(blackColor);
            options.setCropFrameColor(blackColor);
            options.setCropGridColor(blackColor);
        }
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);

        if(!targetFile.exists()){
            Uri target = Uri.fromFile(targetFile);
            if(forProfile) UCrop.of(uri,target).withAspectRatio(1,1)
                    .withOptions(options)
                    .start(activity); else UCrop.of(uri,target).withOptions(options).start(activity);
        }else{
            if(targetFile.delete()) cropImage(context,activity,uri,targetFile,forProfile,errorListener); else errorListener.onError("");
        }
    }

    public static String getLocalFile(Message message) {
        return My.folder+message.getId()+".png";
    }

    public static String getProgress(UploadTask.TaskSnapshot snapshot){
        return getMb(snapshot.getBytesTransferred())+" Mb/"+getMb(snapshot.getTotalByteCount())+" Mb";
    }

    public static String getProgress(FileDownloadTask.TaskSnapshot snapshot){
        return getMb(snapshot.getBytesTransferred())+" Mb/"+getMb(snapshot.getTotalByteCount())+" Mb";
    }

    public static String getMb(long bytes){
        float f = bytes/1024f/1024f;
        f = (int)(f*100f)/100f;
        return Float.toString(f);
    }

    public static String getPercentage(UploadTask.TaskSnapshot snapshot){
        float f = (float) snapshot.getBytesTransferred()/snapshot.getTotalByteCount()*100;
        f = (int)(f*100)/100f;
        return f +" %";
    }

    public static String getPercentage(FileDownloadTask.TaskSnapshot snapshot){
        float f = (float) snapshot.getBytesTransferred()/snapshot.getTotalByteCount()*100;
        f = (int)(f*100)/100f;
        return f +" %";
    }
}