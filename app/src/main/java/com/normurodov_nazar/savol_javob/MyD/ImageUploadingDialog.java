package com.normurodov_nazar.savol_javob.MyD;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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
    TextView progress,percent;
    ImageView imageView;
    Button b;
    UploadTask uploadTask;
    private final StorageReference storage;

    public ImageUploadingDialog(@NonNull Context context,String filePath,String uploadAs,boolean toChats) {
        super(context);
        this.context = context;
        this.filePath = filePath;
        storage = FirebaseStorage.getInstance().getReference().child(toChats ? Keys.chats : Keys.users).child(uploadAs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_image_dialog);
        uploadTask = storage.putFile(Uri.fromFile(new File(filePath)));
        progress = findViewById(R.id.progress);
        percent = findViewById(R.id.foiz);
        imageView = findViewById(R.id.imageView);imageView.setImageURI(Uri.fromFile(new File(filePath)));
        b = findViewById(R.id.b);b.setOnClickListener(v -> {
            uploadTask.cancel();
            dismiss();
        });
        percent.setText("0 %");
        uploadTask.addOnFailureListener(this::onError)
                .addOnCompleteListener(task -> {
                    Hey.print("complete", String.valueOf(task.isSuccessful()));
                    if(task.isSuccessful()){
                        finished();
                    }else Hey.showUnknownError(context);
        })
        .addOnProgressListener(snapshot -> {
            Hey.print("progress changed",getPercentage(snapshot));
            progress.setText(getProgress(snapshot));percent.setText(getPercentage(snapshot));
        });
    }

    private String getProgress(UploadTask.TaskSnapshot snapshot){
        return getMb(snapshot.getBytesTransferred())+" Mb/"+getMb(snapshot.getTotalByteCount())+" Mb";
    }

    private String getMb(long bytes){
        float f = bytes/1024f/1024f;
        f = (int)(f*100f)/100f;
        return Float.toString(f);
    }

    private String getPercentage(UploadTask.TaskSnapshot snapshot){
        float f = (float) snapshot.getBytesTransferred()/snapshot.getTotalByteCount()*100;f = (int)(f*100)/100f;
        return f +" %";
    }

    private void finished() {
        Hey.print("a","finished");
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

    @Override
    protected void onStart() {
        super.onStart();

    }
}
