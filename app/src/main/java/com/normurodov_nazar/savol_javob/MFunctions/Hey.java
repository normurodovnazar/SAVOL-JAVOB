package com.normurodov_nazar.savol_javob.MFunctions;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.normurodov_nazar.savol_javob.R.color;
import static com.normurodov_nazar.savol_javob.R.string;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.normurodov_nazar.savol_javob.MyD.CollectionListener;
import com.normurodov_nazar.savol_javob.MyD.DocumentSnapshotListener;
import com.normurodov_nazar.savol_javob.MyD.DocumentsListener;
import com.normurodov_nazar.savol_javob.MyD.EditMessageDialog;
import com.normurodov_nazar.savol_javob.MyD.EditMode;
import com.normurodov_nazar.savol_javob.MyD.ErrorListener;
import com.normurodov_nazar.savol_javob.MyD.Exists;
import com.normurodov_nazar.savol_javob.MyD.ImageDownloadingDialog;
import com.normurodov_nazar.savol_javob.MyD.ImageMode;
import com.normurodov_nazar.savol_javob.MyD.ImageUploadingDialog;
import com.normurodov_nazar.savol_javob.MyD.ItemClickListener;
import com.normurodov_nazar.savol_javob.MyD.LoadingDialog;
import com.normurodov_nazar.savol_javob.MyD.Message;
import com.normurodov_nazar.savol_javob.MyD.MyDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;
import com.normurodov_nazar.savol_javob.MyD.MySingleton;
import com.normurodov_nazar.savol_javob.MyD.ProgressListener;
import com.normurodov_nazar.savol_javob.MyD.Question;
import com.normurodov_nazar.savol_javob.MyD.StatusListener;
import com.normurodov_nazar.savol_javob.MyD.SuccessListener;
import com.normurodov_nazar.savol_javob.MyD.UriListener;
import com.normurodov_nazar.savol_javob.MyD.User;
import com.normurodov_nazar.savol_javob.R;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Hey {

    public static long getCurrentTime() {
        return Timestamp.now().toDate().getTime();
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = new ClipData(new ClipDescription("a", new String[0]), new ClipData.Item(text));
        clipboardManager.setPrimaryClip(clipData);
        showToast(context, context.getString(string.copied));
    }

    public static ArrayList<Integer> getDifferenceBetweenMessageChanges(ArrayList<Message> old, ArrayList<Message> newM) {
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < old.size(); i++) {
            String messageType = old.get(i).getType();
            if ((messageType.equals(Keys.textMessage) || messageType.equals(Keys.question) || messageType.equals(Keys.answer)) && !old.get(i).getMessage().equals(newM.get(i).getMessage())) {
                positions.add(i);
            }
        }
        return positions;
    }

    public static ArrayList<Integer> getDifferenceOfReadUnreadMessages(ArrayList<Message> old, ArrayList<Message> newM) {
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < old.size(); i++) {
            if (!old.get(i).isRead() && newM.get(i).isRead()) {
                positions.add(i);
            }
        }
        return positions;
    }

    public static void getAllUnreadMessagesForMeAndRead(ArrayList<Message> messages, CollectionReference chats) {
        for (Message m : messages)
            if (m.getSender() != My.id && !m.isRead()) {
                Map<String, Object> data = new HashMap<>();
                data.put(Keys.read, true);
                chats.document(m.getId()).update(data);
            }
    }

    public static ArrayList<Message> getDeletedMessages(ArrayList<Message> oldMessages, ArrayList<Message> messages) {
        ArrayList<Message> temp = new ArrayList<>();
        ArrayList<String> oldIds = new ArrayList<>(), ids = new ArrayList<>();
        for (Message x : oldMessages) oldIds.add(x.getId());
        for (Message x : messages) ids.add(x.getId());
        for (String id : ids) oldIds.remove(id);
        for (Message x : oldMessages) if (oldIds.contains(x.getId())) temp.add(x);
        return temp;
    }

    public static void downloadFile(Context context, String folder, String fileName, File destinationFile, ProgressListener progressListener, SuccessListener successListener, ErrorListener errorListener) {
        FirebaseStorage.getInstance().getReference().child(folder).child(fileName).getFile(destinationFile).addOnFailureListener(e -> Hey.showAlertDialog(context, context.getString(string.error_download_file) + ":" + e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage()))).addOnProgressListener(snapshot -> progressListener.onProgressChanged(snapshot.getBytesTransferred(), snapshot.getTotalByteCount())).addOnSuccessListener(taskSnapshot -> successListener.onSuccess(null));
    }

    public static void addToChats(Context context, long id1, long id2) {
        CollectionReference c1 = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(id1)).collection(Keys.chats),
                c2 = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(id2)).collection(Keys.chats);
        addDocumentToCollectionIfNotExists(context, c1, id2);
        addDocumentToCollectionIfNotExists(context, c2, id1);
    }

    public static void removeFromChats(Context context, long id, SuccessListener successListener) {
        CollectionReference c = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.chats);
        c.document(String.valueOf(id)).delete().addOnFailureListener(e -> Hey.showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage())).addOnSuccessListener(unused -> successListener.onSuccess(null));
    }

    static void addDocumentToCollectionIfNotExists(Context context, CollectionReference c, long id) {
        getCollection(context, c, docs -> {
            boolean contains = false;
            for (DocumentSnapshot doc : docs) {
                if (Long.parseLong(doc.getId()) == id) contains = true;
            }
            if (!contains)
                addDocumentToCollection(context, c, String.valueOf(id), new HashMap<>(), doc -> {
                }, errorMessage -> {
                });
        }, errorMessage -> {

        });
    }

    public static void addDocumentToCollection(Context context, CollectionReference collectionReference, String documentId, Map<String, Object> data, SuccessListener successListener, ErrorListener errorListener) {
        collectionReference.document(documentId).set(data).addOnSuccessListener(unused -> successListener.onSuccess(null)).addOnFailureListener(e -> showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage())));
    }

    public static void editMessage(Context context, Map<String, Object> data, DocumentReference document, EditMode editMode, SuccessListener successListener) {
        EditMessageDialog dialog = new EditMessageDialog(context, data, document, editMode, successListener);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static ListenerRegistration addDocumentListener(Context context, DocumentReference doc, DocumentSnapshotListener listener, ErrorListener errorListener) {
        return doc.addSnapshotListener((value, error) -> {
            if (value != null) {
                listener.documentSnapshot(value);
            } else if (error != null)
                Hey.showAlertDialog(context, context.getString(string.error) + ":" + error.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(error.getLocalizedMessage()));
            else showUnknownError(context).setOnDismissListener(dialog -> {
                    assert false;
                    errorListener.onError(error.getLocalizedMessage());
                });
        });
    }

    public static void getDocument(Context context, DocumentReference doc, SuccessListener documentListener, ErrorListener errorListener) {
        if (doc!=null) doc.get().addOnSuccessListener(documentListener::onSuccess).addOnFailureListener(e -> {
            Hey.showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage());
            errorListener.onError(e.getLocalizedMessage());
        });
    }

    /**
     * called listener every updates in this chat
     */
    public static ListenerRegistration addMessagesListener(Context context, CollectionReference collectionReference, long startAt, CollectionListener collectionListener, ErrorListener errorListener) {
        return collectionReference.orderBy(Keys.time, Query.Direction.ASCENDING).startAt(startAt).addSnapshotListener((value, error) -> {
            if (value != null) {
                List<DocumentSnapshot> list = value.getDocuments();
                ArrayList<Message> messages = new ArrayList<>();
                for (DocumentSnapshot d : list) {
                    messages.add(Message.fromDoc(d));
                }
                collectionListener.result(messages);
            } else if (error != null)
                Hey.showAlertDialog(context, context.getString(string.error) + ":" + error.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(error.getLocalizedMessage()));
            else showUnknownError(context).setOnDismissListener(dialog -> {
                    assert false;
                    errorListener.onError(error.getLocalizedMessage());
                });
        });

    }


    public static void searchUsersFromServer(Context context, String text, boolean byName, DocumentsListener documentsListener, ErrorListener errorListener) {
        FirebaseFirestore.getInstance().collection(Keys.users).whereEqualTo(byName ? Keys.name : Keys.surname, text).get()
                .addOnSuccessListener(queryDocumentSnapshots -> documentsListener.onDocuments(queryDocumentSnapshots.getDocuments())).addOnFailureListener(e -> Hey.showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage())));
    }

    public static void getCollection(Context context, CollectionReference collectionReference, DocumentsListener documentsListener, ErrorListener errorListener) {
        collectionReference.get().addOnCompleteListener(task -> task.addOnSuccessListener(queryDocumentSnapshots -> documentsListener.onDocuments(queryDocumentSnapshots.getDocuments())).addOnFailureListener(e -> {
            Hey.showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage()));
            errorListener.onError(e.getLocalizedMessage());
        })).addOnFailureListener(e -> {
            Hey.showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage()));
            errorListener.onError(e.getLocalizedMessage());
        });
    }

    public static ListenerRegistration setCollectionListener(Context context, CollectionReference collectionReference, DocumentsListener documentsListener, ErrorListener errorListener) {
        return collectionReference.addSnapshotListener((value, error) -> {
            if (value != null) {
                documentsListener.onDocuments(value.getDocuments());
            } else {
                Hey.showAlertDialog(context, context.getString(string.error) + ":" + (error == null ? context.getString(string.unknown) : error.getLocalizedMessage())).setOnDismissListener(dialog -> {
                    assert error != null;
                    errorListener.onError(error.getLocalizedMessage());
                });
            }
        });
    }

    public static String getTimeText(Context context, long time) {
        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String full = calendar.get(Calendar.DAY_OF_MONTH) + "-" + getMonth(context, calendar) + (isCurrentYear(calendar) ? "" : "," + calendar.get(Calendar.YEAR) + "-" + context.getString(string.year));
        return getClock(calendar) + "," + (isYesterday(calendar) ? context.getString(string.yesterday) : isToday(calendar) ? context.getString(string.today) : full);
    }

    static boolean isYesterday(Calendar calendar) {
        int year, month, day, cYear, cMonth, cDay;
        Calendar c = Calendar.getInstance();
        cYear = c.get(Calendar.YEAR);
        cMonth = c.get(Calendar.MONTH);
        cDay = c.get(Calendar.DAY_OF_MONTH);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        return year == cYear && month == cMonth && day == cDay - 1;
    }

    static boolean isToday(Calendar calendar) {
        int year, month, day, cYear, cMonth, cDay;
        Calendar c = Calendar.getInstance();
        cYear = c.get(Calendar.YEAR);
        cMonth = c.get(Calendar.MONTH);
        cDay = c.get(Calendar.DAY_OF_MONTH);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        return year == cYear && month == cMonth && day == cDay;
    }

    static boolean isCurrentYear(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR), cYear = Calendar.getInstance().get(Calendar.YEAR);
        return year == cYear;
    }

    static String getMonth(Context context, Calendar calendar) {
        int i = calendar.get(Calendar.MONTH);
        String m;
        switch (i) {
            case Calendar.JANUARY:
                m = context.getString(string.january);
                break;
            case Calendar.FEBRUARY:
                m = context.getString(string.february);
                break;
            case Calendar.MARCH:
                m = context.getString(string.march);
                break;
            case Calendar.APRIL:
                m = context.getString(string.april);
                break;
            case Calendar.MAY:
                m = context.getString(string.may);
                break;
            case Calendar.JUNE:
                m = context.getString(string.june);
                break;
            case Calendar.JULY:
                m = context.getString(string.july);
                break;
            case Calendar.AUGUST:
                m = context.getString(string.august);
                break;
            case Calendar.SEPTEMBER:
                m = context.getString(string.september);
                break;
            case Calendar.OCTOBER:
                m = context.getString(string.october);
                break;
            case Calendar.NOVEMBER:
                m = context.getString(string.november);
                break;
            default:
                m = context.getString(string.december);
                break;
        }
        return m;
    }

    static String getClock(Calendar calendar) {
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY), minute = calendar.get(Calendar.MINUTE);
        return (String.valueOf(hourOfDay).length() == 1 ? "0" + hourOfDay : hourOfDay) + ":" + (String.valueOf(minute).length() == 1 ? "0" + minute : minute);
    }

    public static String getFriendsIdFromChatId(String chatId) {
        if (chatId.contains(My.id + "+")) return chatId.replace(My.id + "+", "");
        else return chatId.replace("+" + My.id, "");
    }

    public static String getChatIdFromIds(long a, long b) {
        if (a > b) return a + "+" + b;
        else return b + "+" + a;
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void print(String tag, String message) {
        Log.e(tag, message);
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

    public static MyDialogWithTwoButtons showDeleteDialog(Context context, String info, Message message, boolean forDeleteMessage) {
        MyDialogWithTwoButtons dialog = new MyDialogWithTwoButtons(context, info, message, forDeleteMessage);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static LoadingDialog showLoadingDialog(Context context) {
        LoadingDialog loadingDialog = new LoadingDialog(context);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(true);
        loadingDialog.show();
        loadingDialog.setOnDismissListener(dialogInterface -> {
            if (loadingDialog.isFromUser()) {
                MyDialogWithTwoButtons x = Hey.showDeleteDialog(context, context.getString(string.cancelLoading), null, false);
                x.setOnDismissListener(dialogInterface1 -> {
                    if (!x.getResult()) loadingDialog.show();
                });
            }
        });
        return loadingDialog;
    }

    public static LoadingDialog showLoadingDialog(Context context, ItemClickListener userCancel) {
        LoadingDialog loadingDialog = new LoadingDialog(context);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(true);
        loadingDialog.show();
        loadingDialog.setOnDismissListener(dialogInterface -> {
            if (loadingDialog.isFromUser()) {
                MyDialogWithTwoButtons x = Hey.showDeleteDialog(context, context.getString(string.cancelLoading), null, false);
                x.setOnDismissListener(dialogInterface1 -> {
                    if (!x.getResult()) loadingDialog.show();
                    else userCancel.onItemClick(0, "");
                });
            }
        });
        return loadingDialog;
    }


    public static void showDeleteImageDialog(Context context, Message message, SuccessListener successListener, ErrorListener errorListener) {
        LoadingDialog dialog = showLoadingDialog(context);
        FirebaseStorage.getInstance().getReference().child(Keys.chats).child(message.getId()).delete().addOnFailureListener(e -> {
            Hey.showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage());
            errorListener.onError(e.getLocalizedMessage());
            dialog.closeDialog();
        }).addOnCompleteListener(task -> {
            successListener.onSuccess(null);
            dialog.closeDialog();
        });
    }

    public static void showDownloadDialog(Context context, Message message, SuccessListener successListener, ErrorListener errorListener) {
        ImageDownloadingDialog dialog = new ImageDownloadingDialog(context, message, errorListener, successListener);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void uploadImageForProfile(Context context, String filePath, String uploadAs, SuccessListener listener, ItemClickListener cancelListener, ErrorListener errorListener) {
        ImageUploadingDialog dialog = new ImageUploadingDialog(context, filePath, uploadAs, ImageMode.profile, listener, cancelListener, errorListener);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void uploadImageToChat(Context context, String filePath, String uploadAs, SuccessListener listener, ItemClickListener cancelListener, ErrorListener errorListener) {
        ImageUploadingDialog dialog = new ImageUploadingDialog(context, filePath, uploadAs, ImageMode.chat, listener, cancelListener, errorListener);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static int getPercentage(long progress, long total) {
        long l = 100 * progress / total;
        return (int) l;
    }

    public static void setButtonAsLoading(@NonNull Context context, @NonNull Button button) {
        button.setText(context.getText(string.wait));
        button.setBackgroundResource(R.drawable.button_bg_pressed);
        //button.setTextColor(context.getResources().getColor(color.white));
        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(600);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        button.startAnimation(animation);
    }

    public static void setButtonAsDefault(Context context, @NonNull Button button, @NonNull String title) {
        button.setBackgroundResource(R.drawable.button_background);
        //button.setTextColor(context.getResources().getColor(color.black));
        button.setText(title);
        button.clearAnimation();
    }

    public static void setIconButtonAsLoading(@NonNull View button) {
        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(600);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        button.startAnimation(animation);
    }

    public static void setIconButtonAsDefault(@NonNull View button) {
        button.clearAnimation();
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

    public static void myNumberExists(Exists exists) {
        FirebaseFirestore.getInstance().collection(Keys.users).whereEqualTo(Keys.number, My.number).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() == 0) {
                exists.notExists();
            } else exists.exists(queryDocumentSnapshots.getDocuments().get(0));
        });
    }

    public static void amIOnline(StatusListener statusListener, ErrorListener errorListener, Context context) {
        FirebaseFirestore.getInstance().collection(Keys.appNumbers).document(Keys.appNumbers).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.getMetadata().isFromCache()) statusListener.offline();
            else statusListener.online();
        }).addOnFailureListener(e -> {
            if (e.getLocalizedMessage() != null) {
                if (!e.getLocalizedMessage().equals("Failed to get document because the client is offline."))
                    showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage()));
                else statusListener.offline();
            }
        });
    }

    public static void isDocumentExists(Context context, DocumentReference doc, Exists exists, ErrorListener errorListener) {
        doc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) exists.exists(documentSnapshot);
            else exists.notExists();
        }).addOnFailureListener(e -> showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(e.getLocalizedMessage())));
    }

    public static long getId(SharedPreferences preferences) {
        return preferences.getLong(Keys.id, -1);
    }

    public static Boolean isLoggedIn(@NonNull SharedPreferences preferences) {
        return preferences.getBoolean(Keys.logged, false);
    }

    public static void animateHorizontal(@NonNull View view, float toWhere, long delay) {
        view.animate().translationX(toWhere).setDuration(500).setStartDelay(delay).start();
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Keys.sharedPreferences, Context.MODE_PRIVATE);
    }

    public static int generateID() {
        Random r = new Random();
        int i = Math.abs(r.nextInt());
        if (i == 0) return generateID();
        else return i;
    }

    public static void deleteDocument(Context context, DocumentReference doc, SuccessListener successListener) {
        doc.delete().addOnFailureListener(e -> showAlertDialog(context, context.getString(string.error_deleting) + e.getLocalizedMessage()))
                .addOnSuccessListener(unused -> successListener.onSuccess(null));
    }

    /**
     * @param chats           collection to add message
     * @param message         message to send
     * @param successListener called when message sent
     * @param errorListener   called when error occurs
     */
    public static void sendMessage(Context context, CollectionReference chats, Message message, SuccessListener successListener, ErrorListener errorListener) {
        amIOnline(new StatusListener() {
            @Override
            public void online() {
                chats.document(message.getId()).set(message.toMap()).addOnSuccessListener(unused -> successListener.onSuccess(null)).addOnFailureListener(e -> {
                    showToast(context, context.getString(string.error) + ":" + e.getLocalizedMessage());
                    errorListener.onError(e.getLocalizedMessage());
                });
            }

            @Override
            public void offline() {
                showToast(context, context.getString(string.error_connection));
                errorListener.onError("");
            }
        }, errorListener, context);


    }

    public static void pickImage(ActivityResultLauncher<Intent> p) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        p.launch(i);
    }

    public static void cropImage(Context context, Activity activity, Uri uri, File targetFile, boolean forProfile, ErrorListener errorListener) {
        UCrop.Options options = new UCrop.Options();
        int blackColor = context.getResources().getColor(color.onlyBlack);
        int whiteColor = context.getResources().getColor(color.onlyWhite);
        options.setRootViewBackgroundColor(blackColor);
        options.setStatusBarColor(blackColor);
        options.setLogoColor(blackColor);
        options.setActiveControlsWidgetColor(whiteColor);
        options.setToolbarWidgetColor(blackColor);
        options.setCropFrameColor(blackColor);
        options.setCropGridColor(blackColor);
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        if (!targetFile.exists()) {
            Uri target = Uri.fromFile(targetFile);
            if (forProfile) UCrop.of(uri, target).withAspectRatio(1, 1)
                    .withOptions(options)
                    .start(activity);
            else UCrop.of(uri, target).withOptions(options).start(activity);
        } else {
            if (targetFile.delete())
                cropImage(context, activity, uri, targetFile, forProfile, errorListener);
            else errorListener.onError("");
        }
    }

    public static File getLocalFile(Message message) {
        return new File(My.folder + message.getId() + ".png");
    }

    public static String getProgress(UploadTask.TaskSnapshot snapshot) {
        return getMb(snapshot.getBytesTransferred()) + "/" + getMb(snapshot.getTotalByteCount());
    }

    public static String getProgress(FileDownloadTask.TaskSnapshot snapshot) {
        return getMb(snapshot.getBytesTransferred()) + "/" + getMb(snapshot.getTotalByteCount());
    }

    public static String getMb(long bytes) {
        float f = bytes / 1024f / 1024f;
        f = (int) (f * 100f) / 100f;
        return f + " Mb";
    }

    public static String getPercentage(UploadTask.TaskSnapshot snapshot) {
        float f = (float) snapshot.getBytesTransferred() / snapshot.getTotalByteCount() * 100;
        f = (int) (f * 100) / 100f;
        return f + " %";
    }

    public static String getPercentage(FileDownloadTask.TaskSnapshot snapshot) {
        float f = (float) snapshot.getBytesTransferred() / snapshot.getTotalByteCount() * 100;
        f = (int) (f * 100) / 100f;
        return f + " %";
    }

    public static ArrayList<Message> getDifferenceOfMessages(ArrayList<Message> messages, ArrayList<Message> oldMessages) {
        ArrayList<Message> temp = new ArrayList<>();
        for (int i = oldMessages.size(); i < messages.size(); i++) temp.add(messages.get(i));
        return temp;
    }

    public static ArrayList<Question> getDifferenceOfQuestions(ArrayList<Question> newQ, ArrayList<Question> oldQ) {
        ArrayList<Question> temp = new ArrayList<>();
        for (int i = oldQ.size(); i < newQ.size(); i++) temp.add(newQ.get(i));
        return temp;
    }

    public static boolean withUpper(String text) {
        return text.charAt(0) == text.toUpperCase().charAt(0);
    }

    public static PopupMenu showPopupMenu(Context context, View item, ArrayList<String> items, ItemClickListener itemClickListener, boolean showNow) {
        PopupMenu menu = new PopupMenu(context, item);
        for (String s : items) {
            menu.getMenu().add(Menu.NONE, Menu.NONE, items.indexOf(s), s);
        }
        menu.setOnMenuItemClickListener(item1 -> {
            itemClickListener.onItemClick(item1.getOrder(), null);
            return true;
        });
        if (showNow) menu.show();
        return menu;
    }

    /**
     * what we need to do with this image?
     *
     * @param user the user which need download his image
     * @param a    what need to do if file exists and ready to show
     * @param b    what need to do if file doesn't exists or file is corrupted
     *             returns
     */
    public static void workWithProfileImage(User user, SuccessListener a, ErrorListener b) {
        File file = new File(user.getLocalFileName());
        if (file.exists()) {
            if (file.length() == user.getImageSize()) {
                a.onSuccess(null);
            } else {
                file.delete();
                b.onError(null);
            }
        } else {
            b.onError(null);
        }
    }

    public static void workWithImageMessage(Message message, SuccessListener a, ErrorListener b) {
        File file = Hey.getLocalFile(message);
        if (file.exists() && file.length() == message.getImageSize()) a.onSuccess(null);
        else {
            if (file.exists()) {
                file.delete();
            }
            b.onError("");
        }
    }

    public static void getUserFromUserId(Context context, String id, SuccessListener listener, ErrorListener errorListener) {
        Hey.getDocument(context, FirebaseFirestore.getInstance().collection(Keys.users).document(id), doc -> {
            DocumentSnapshot documentSnapshot = (DocumentSnapshot) doc;
            listener.onSuccess(User.fromDoc(documentSnapshot));
        }, errorListener);
    }

    public static int getIndexInArray(Message message, ArrayList<Message> messages) {
        for (Message x : messages) {
            if (x.getId().equals(message.getId())) return messages.indexOf(x);
        }
        return -1;
    }

    public static void updateDocument(Context context, DocumentReference doc, Map<String, Object> data, SuccessListener successListener, ErrorListener errorListener) {
        doc.set(data, SetOptions.merge()).addOnSuccessListener(unused -> successListener.onSuccess(null)).addOnFailureListener(e -> Hey.showAlertDialog(context, context.getString(string.error) + ":" + e.getLocalizedMessage()).setOnDismissListener(dialog -> errorListener.onError(null)));
    }

    public static void sendNotification(Context context, String title, String message, String tokenOrTopic, Map<String, String> d, SuccessListener successListener, ErrorListener errorListener) {
        JSONObject notification = new JSONObject(), body = new JSONObject(), data = new JSONObject();
        try {
            String t = d.get(Keys.type);
            String type = t == null ? "" : t;
            body.put("title", title);
            body.put("body", message);
            notification.put("to", type.equals(Keys.privateChat) ? tokenOrTopic : Keys.topics + tokenOrTopic);
            notification.put("notification", body);
            switch (type) {
                case Keys.privateChat:
                    data.put(Keys.id, d.get(Keys.id));
                    break;
                case Keys.publicQuestions:
                case Keys.needQuestions:
                    data.put(Keys.id, d.get(Keys.id));
                    data.put(Keys.theme, d.get(Keys.theme));
                    data.put(Keys.sender, d.get(Keys.sender));
                    break;
            }
            data.put(Keys.type, type);
            notification.put("data", data);
            Hey.print("notification", notification.toString());
        } catch (JSONException e) {
            errorListener.onError(e.getLocalizedMessage());
            showToast(context, context.getString(string.error) + ":" + e.getLocalizedMessage());
        }
        sendToFirebase(notification, context, successListener, errorListener);
    }

    static void sendToFirebase(JSONObject notification, Context context, SuccessListener successListener, ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Keys.fcmApi, notification, response -> successListener.onSuccess(null), error -> {
            errorListener.onError(error.getLocalizedMessage());
            showToast(context, error.getLocalizedMessage());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", My.petName);
                params.put("Content-Type", Keys.contentType);
                return params;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static String reverseString(String serverKey) {
        String[] a = serverKey.split("");
        StringBuilder leftToRight = new StringBuilder();
        for (int i = a.length - 1; i >= 0; i--) {
            leftToRight.append(a[i]);
        }
        return leftToRight.toString();
    }

    public static void updateActivity() {
        if (!My.updated) {
            My.updated = true;
            Hey.print("a", "Activity updated");
            FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).update(Collections.singletonMap(Keys.seen, Hey.getCurrentTime())).
                    addOnSuccessListener(unused -> {
                    });
        } else {
            Hey.print("a", "Activity update called but not required");
        }
    }

    public static boolean isMessageAddedToTop(ArrayList<Message> oldMessages, ArrayList<Message> messages) {
        return oldMessages.get(0).getTime() > messages.get(0).getTime();
    }

    public static ArrayList<Message> getMessagesOnTop(ArrayList<Message> oldMessages, ArrayList<Message> messages) {
        ArrayList<Message> temp = new ArrayList<>();
        long limitId = oldMessages.get(0).getTime();
        for (Message m : messages) {
            if (m.getTime() < limitId) temp.add(m);
            else break;
        }
        return temp;
    }

    public static boolean isMessagesAddedToBottom(ArrayList<Message> oldMessages, ArrayList<Message> messages) {
        return oldMessages.get(oldMessages.size() - 1).getTime() < messages.get(messages.size() - 1).getTime();
    }

    public static void showErrorMessage(Activity activity, String errorMessage, boolean finish) {
        showAlertDialog(activity, activity.getString(string.error) + ":" + errorMessage).setOnDismissListener(dialogInterface -> {
            if (finish) activity.finish();
        });
    }

    public static PopupMenu chooseImage(Context context, View view, ActivityResultLauncher<Intent> memoryLauncher, ActivityResultLauncher<Intent> captureLauncher, UriListener listener) {
        return showPopupMenu(context, view, new ArrayList<>(Arrays.asList(context.getString(string.camera), context.getString(string.memory))), (position, name) -> {
            if (position == 0) {
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = null;
                try {
                    photo = createTempFile(context);
                } catch (IOException e) {
                    Hey.showAlertDialog(context, e.getLocalizedMessage());
                }
                if (photo != null) {
                    Uri photoUri = FileProvider.getUriForFile(context, "com.normurodov_nazar.savol_javob.fileProvider", photo);
                    listener.onUri(photoUri);
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                }
                captureLauncher.launch(pictureIntent);
            } else pickImage(memoryLauncher);
        }, true);
    }

    public static File createTempFile(Context context) throws IOException {
        File storage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(String.valueOf(Hey.getCurrentTime()), ".jpg", storage);
    }

    public static String getTopicFromTheme(String theme) {
        return theme.replaceAll(" ", "").replaceAll("ʻ", "").replaceAll("'", "").replaceAll("\\.", "");
    }

    public static void applyTheme(Context context) {
        print("h", "apply Theme called");
        switch (Hey.getPreferences(context).getString(Keys.dayNight, Keys.system)) {
            case Keys.day:
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Keys.night:
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                    print("a", "mode wasn't night and applying night");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else print("a", "mode was night do nothing");
                break;
            case Keys.system:
                if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static void setBigImage(SubsamplingScaleImageView bigImage, File f) {
        bigImage.setImage(ImageSource.uri(Uri.fromFile(f)));
        bigImage.setBackgroundColor(Color.BLACK);
        bigImage.setMaxScale(15);
        bigImage.setMinScale(0.1f);
    }

    public static void publishQuestion(Context context, Question question, int days, String filePath, boolean forActivate, SuccessListener successListener, ErrorListener errorListener) {
        DocumentReference myDoc = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id));
        CollectionReference publicQuestions = FirebaseFirestore.getInstance().collection(Keys.publicQuestions + context.getString(string.lang)),
                allQuestions = FirebaseFirestore.getInstance().collection(Keys.allQuestions),
                myQuestions = FirebaseFirestore.getInstance().collection(Keys.users).document(String.valueOf(My.id)).collection(Keys.allQuestions),
        chats = FirebaseFirestore.getInstance().collection(Keys.chats).document(question.getQuestionId()).collection(Keys.chats);
        Hey.amIOnline(new StatusListener() {
            @Override
            public void online() {
                if (My.units >= days * My.unitsForPerDay) {
                    MyDialogWithTwoButtons x = Hey.showDeleteDialog(context, context.getString(string.confirmUnitDecrease)
                            .replaceAll("xxx", String.valueOf(days))
                            .replaceAll("yyy", String.valueOf(My.unitsForPerDay))
                            .replaceAll("zzz", String.valueOf(days * My.unitsForPerDay)), null, false);
                    x.setOnDismissListener(z -> {
                        if (x.getResult()) {
                            Hey.getCollection(context, publicQuestions, docs -> {
                                if (My.questionLimit > docs.size()) {
                                    if (!forActivate)
                                        Hey.uploadImageToChat(context, filePath, question.getQuestionId(), doc -> {
                                            Hey.print("newQuestion", "imageUploaded");
                                            Hey.addDocumentToCollection(context, allQuestions, question.getQuestionId(), question.toMap(), doc1 -> {
                                                Hey.print("newQuestion", "allQuestions");
                                                Hey.addDocumentToCollection(context, myQuestions, question.getQuestionId(), question.toMap(), doc2 -> {
                                                    Hey.print("newQuestion", "myQuestions");
                                                    Hey.addDocumentToCollection(context, publicQuestions, question.getQuestionId(), question.toMap(), doc3 -> {
                                                        Hey.print("newQuestion", Keys.publicQuestions);
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put(Keys.time, question.getTime());
                                                        data.put(Keys.type, Keys.question);
                                                        data.put(Keys.sender, question.getSender());
                                                        data.put(Keys.message, question.getMessage());
                                                        data.put(Keys.read, false);
                                                        data.put(Keys.imageSize,(new File(filePath)).length());
                                                        Hey.addDocumentToCollection(context, chats, question.getQuestionId(), data, doc42 -> {
                                                            Hey.print("newQuestion","added to chat");
                                                            Map<String, String> notification = new HashMap<>();
                                                            notification.put(Keys.id, question.getQuestionId());
                                                            notification.put(Keys.theme, question.getTheme());
                                                            notification.put(Keys.type, Keys.publicQuestions);
                                                            notification.put(Keys.sender, String.valueOf(My.id));
                                                            String theme = question.getTheme().replace(Keys.incorrect, "").replace(Keys.correct, "");
                                                            Hey.sendNotification(context, My.fullName, context.getString(R.string.newQuestionMessage).replace("aaa", theme), Hey.getTopicFromTheme(theme), notification, doc4 -> {
                                                            }, errorMessage -> { });
                                                            FirebaseMessaging.getInstance().subscribeToTopic(Keys.topics + question.getQuestionId());
                                                            Map<String,Object> x1 = new HashMap<>();
                                                            x1.put(Keys.numberOfMyPublishedQuestions,My.numberOfMyPublishedQuestions+1);
                                                            x1.put(Keys.units,My.units-My.unitsForPerDay* days);
                                                            Hey.updateDocument(context, myDoc, x1, doc43 -> Hey.print("newQuestion",My.unitsForPerDay*days +" gone"), errorMessage -> {

                                                            });
                                                            successListener.onSuccess("");
                                                        }, errorMessage -> {

                                                        });
                                                    }, errorMessage -> {

                                                    });
                                                }, errorMessage -> {

                                                });
                                            }, errorMessage -> {

                                            });
                                        }, (position, name) -> {
                                        }, errorMessage -> {
                                        });
                                    else
                                        Hey.addDocumentToCollection(context, publicQuestions, question.getQuestionId(), question.toMap(), doc12 -> {
                                            Map<String, String> notification = new HashMap<>();
                                            notification.put(Keys.id, question.getQuestionId());
                                            notification.put(Keys.theme, question.getTheme());
                                            notification.put(Keys.type, Keys.publicQuestions);
                                            notification.put(Keys.sender, String.valueOf(My.id));
                                            String theme = question.getTheme().replace(Keys.incorrect, "").replace(Keys.correct, "");
                                            Hey.sendNotification(context, My.fullName, context.getString(R.string.newQuestionMessage).replace("aaa", theme), Hey.getTopicFromTheme(theme), notification, doc4 -> {
                                            }, errorMessage -> {
                                            });
                                            Map<String,Object> x1 = new HashMap<>();
                                            x1.put(Keys.units,My.units-My.unitsForPerDay* days);
                                            Hey.updateDocument(context, myDoc, x1, doc43 -> Hey.print("newQuestion",My.unitsForPerDay*days +" gone"), errorMessage -> {

                                            });
                                            FirebaseMessaging.getInstance().subscribeToTopic(Keys.topics + question.getQuestionId());
                                            successListener.onSuccess(null);
                                        }, errorMessage -> {
                                        });
                                } else {
                                    errorListener.onError("");
                                    Hey.showAlertDialog(context, context.getString(R.string.questionLimitError).replace("xxx", String.valueOf(My.questionLimit)));
                                }
                            }, errorMessage -> {

                            });
                        } else errorListener.onError("");
                    });
                } else {
                    errorListener.onError("");
                    Hey.showAlertDialog(context, context.getString(R.string.notEnoughUnitsForDay)
                            .replaceAll("xxx", String.valueOf(days))
                            .replaceAll("yyy", String.valueOf(My.units))
                            .replaceAll("zzz", String.valueOf(My.unitsForPerDay))
                            .replaceAll("ttt",String.valueOf(My.unitsForPerDay*days)));
                }
            }

            @Override
            public void offline() {
                errorListener.onError("");
                Hey.showToast(context, context.getString(string.error_connection));
            }
        }, errorMessage -> errorListener.onError(""), context);
    }

    public static void compressImage(Context context,File file) {
        Hey.print("fileSizeWas",Hey.getMb(file.length()));
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(file));
            } catch (IOException e) {
                Hey.print("compressImage","IOException");
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                Hey.print("compressImage","IOException");
            }
        }
        Hey.compressBitmap(file,bitmap);
    }

    public static void compressBitmap(File file, Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap != null) {
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream)) {
                byte[] bytes = byteArrayOutputStream.toByteArray();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    Hey.print("FileOutputStream", "FileNotFoundException");
                } catch (SecurityException e) {
                    Hey.print("FileOutputStream", "SecurityException");
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.write(bytes);
                        fileOutputStream.close();
                        Hey.print("afterComrpess",getMb(file.length()));
                    } catch (IOException e) {
                        Hey.print("FileOutputStream", "IOException");
                    }
                }
            }
        }
    }

    public static boolean amIBlocked() {
        return My.blockTime>getCurrentTime();
    }

    public static void showYouBlockedDialog(Context context){
        Hey.showAlertDialog(context,context.getString(string.youBlocked).replaceAll("xxx",Hey.getTimeText(context,My.blockTime)));
    }

    public static void gotoPrivacy(Context context,TextView privacy) {
        privacy.setOnClickListener(v->{
            context.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(context.getString(R.string.privacy))));
        });
    }
}