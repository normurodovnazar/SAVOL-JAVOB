package com.normurodov_nazar.savol_javob;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.normurodov_nazar.savol_javob.MFunctions.Hey;
import com.normurodov_nazar.savol_javob.MyD.ImageUploadingDialog;
import com.normurodov_nazar.savol_javob.MyD.MyDialogWithTwoButtons;

import java.io.File;

public class ForTest extends AppCompatActivity{
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_test);
        b = findViewById(R.id.aaaa);
    }

    @Override
    protected void onStart() {
        super.onStart();
        b.setOnClickListener(v -> {
            File originalFile = new File(getExternalFilesDir("images").toString()+File.separatorChar+"Me.png");
            ImageUploadingDialog dialog = Hey.uploadImageToChat(this,originalFile.getPath(),"Nazar.png");
            dialog.setOnDismissListener(d -> {
                String url = dialog.getDownloadUrl();
                if(url != null) Hey.print("aa",url); else Hey.print("aaa","Url is null");
            });
        });
    }
}