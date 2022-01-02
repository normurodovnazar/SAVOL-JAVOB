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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    ProgressBar bar;
    TextView progress,percent;
    ImageView imageView;
    Button b;
    UploadTask uploadTask;
    private final StorageReference storage;
    String downloadUrl;

    public ImageUploadingDialog(@NonNull Context context,String filePath,String uploadAs,boolean forProfile,SuccessListener successListener,ItemClickListener clickListener) {
        super(context);
        this.context = context;
        this.filePath = filePath;
        storage = FirebaseStorage.getInstance().getReference().child(forProfile ? Keys.chats : Keys.users).child(uploadAs);
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
                        finished();
                    }else Hey.showUnknownError(context);
        })
        .addOnProgressListener(snapshot -> {
            Hey.print("progress changed",Hey.getPercentage(snapshot));
            int i = (int) (snapshot.getBytesTransferred()*100/snapshot.getTotalByteCount());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) bar.setProgress(100-i,true);
            else bar.setProgress(100-i);
            progress.setText(Hey.getProgress(snapshot));percent.setText(Hey.getPercentage(snapshot));
        });
    }

    private void finished() {
        Hey.print("a","finished");
        storage.getDownloadUrl().addOnCompleteListener(task -> {
            if(task.isSuccessful()) if(task.getResult()!=null) {
                downloadUrl = task.getResult().toString();
                successListener.onSuccess(null);
                dismiss();
            } else unknownE(); else unknownE();
            });
    }

    void unknownE(){
        Hey.showUnknownError(context).setOnDismissListener(dialog -> {
            dismiss();
            ImageUploadingDialog.this.dismiss();
        });
    }

    private void onError(Exception e) {
        Hey.print("error",e.getMessage());
        if(e.getMessage() != null)
            switch (e.getMessage()){
            default:
                Hey.showAlertDialog(context,e.getMessage());
        }
        dismiss();
    }

    public String getDownloadUrl(){
        return downloadUrl;
    }
}
