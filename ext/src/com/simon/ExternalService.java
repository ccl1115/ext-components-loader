package com.simon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 */
public class ExternalService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "This toast comes from an external service", Toast.LENGTH_LONG).show();
        return Service.START_STICKY;
    }


}
