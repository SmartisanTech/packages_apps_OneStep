package com.smartisanos.sidebar;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;

public class SidebarService extends Service {
    static final String TAG = "SidebarService";
    public static final boolean DEBUG = (SystemProperties.getInt(
            "ro.debuggable", 0) == 1);

    @Override
    public void onCreate() {
        Log.d("TAG", "onCreate()......");
        SidebarController.getInstance(getApplicationContext()).init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO
    }

    /**
     * Nobody binds to us.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        // TODO
    }
}
