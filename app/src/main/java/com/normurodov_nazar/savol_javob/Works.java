package com.normurodov_nazar.savol_javob;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Works extends Service {

    public Works() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}