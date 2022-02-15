package com.normurodov_nazar.savol_javob.MyD;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;

public class ImageUploadingDialog extends Dialog {
    private final String filePath;
    private final Context context;
    private final SuccessListener successListener;
    private final ItemClickListener clickListener;
    private final ErrorListener errorListener;
    ProgressBar bar;
    TextView progress,percent;
    ImageView imageView;
    Button b;
    UploadTask uploadTask;
    private final StorageReference storage;

    public ImageUploadingDialog(@NonNull Context context,String filePath,String uploadAs,ImageMode mode,SuccessListener successListener,ItemClickListener clickListener,ErrorListener errorListener) {
        super(context);
        this.errorListener = errorListener;
        this.context = context;
        this.filePath = filePath;
        String ref;
        switch (mode){
            case chat:
                ref = Keys.chats;
                break;
            case profile:
                ref = Keys.users;
                break;
            case question:
                ref = Keys.allQuestions;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mode);
        }
        storage = FirebaseStorage.getInstance().getReference().child(ref).child(uploadAs);
        this.successListener = successListener;
        this.clickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_image_dialog);
        uploadTask = storage.putFile(Uri.fromFile(new File(filePath)));
        progress = findViewById(R.id.progress);
        percent = findViewById(R.id.foiz);
        bar = findViewById(R.id.progressImageUpload);
        imageView = findViewById(R.id.imageView);imageView.setImageURI(Uri.fromFile(new File(filePath)));
        b = findViewById(R.id.b);b.setOnClickListener(v -> {
            uploadTask.cancel();
            clickListener.onItemClick(0,null);
            dismiss();
        });
        percent.setText("0 %");
        uploadTask.addOnFailureListener(this::onError)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        successListener.onSuccess(null);
                        dismiss();
                    }else Hey.showUnknownError(context);
        })
        .addOnProgressListener(snapshot -> {
            int i = (int) (snapshot.getBytesTransferred()*100/snapshot.getTotalByteCount());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) bar.setProgress(100-i,true);
            else bar.setProgress(100-i);
            progress.setText(Hey.getProgress(snapshot));percent.setText(Hey.getPercentage(snapshot));
        });
    }

    private void onError(Exception e) {
        if(e.getMessage() != null) {
            Hey.showAlertDialog(context, e.getMessage()).setOnDismissListener(dialogInterface -> errorListener.onError(null));
        }
        dismiss();
    }
}
