package com.normurodov_nazar.savol_javob.MyD;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MFunctions.Keys;
import com.normurodov_nazar.savol_javob.R;

import java.io.File;

public class ImageDownloadingDialog extends Dialog {
    final Message message;
    final ErrorListener errorListener;
    final SuccessListener successListener;
    StorageReference storageReference;
    FileDownloadTask downloadTask;

    ProgressBar progress;
    TextView progressDownload, percentage;
    Button cancel;

    public ImageDownloadingDialog(@NonNull Context context, Message message, ErrorListener errorListener, SuccessListener successListener) {
        super(context);
        this.successListener = successListener;
        this.errorListener = errorListener;
        this.message = message;
        storageReference = FirebaseStorage.getInstance().getReference().child(Keys.chats).child(message.getId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_image_dialog);
        initVars();
        downloadTask.addOnFailureListener(e -> {
            errorListener.onError(e.getLocalizedMessage());
            Hey.showAlertDialog(getContext(), getContext().getString(R.string.error_download_file) + ":" + e.getLocalizedMessage())
                    .setOnDismissListener(dialog -> {
                        downloadTask.cancel();
                        this.dismiss();
                    });
        }).addOnSuccessListener(taskSnapshot -> {
            successListener.onSuccess(null);
            dismiss();
        }).addOnProgressListener(taskSnapshot -> {
            int i = (int) (taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                progress.setProgress(100 - i, true);
            else progress.setProgress(100 - i);
            progressDownload.setText(Hey.getProgress(taskSnapshot));
            percentage.setText(Hey.getPercentage(taskSnapshot));
        });
    }

    private void initVars() {
        cancel = findViewById(R.id.cancel_download);
        cancel.setOnClickListener(v -> {
            downloadTask.cancel();
            dismiss();
        });
        progress = findViewById(R.id.progressImageDownload);
        progressDownload = findViewById(R.id.progress_download);
        percentage = findViewById(R.id.percentage_download);
        downloadTask = storageReference.getFile(new File(Hey.getLocalFile(message)));
    }
}
