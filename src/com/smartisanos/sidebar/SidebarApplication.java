package com.smartisanos.sidebar;

import com.smartisanos.sidebar.util.MailContactsHelper;

import android.app.Application;
import android.os.StrictMode;

public class SidebarApplication extends Application {

    private static final boolean ENABLE_STRICT_MODE = true;

    @Override
    public void onCreate() {
        super.onCreate();
        setStrictMode();
        // this is necessary ! init it to make its inner data be filled
        // so we can use it correctly later
        MailContactsHelper.getInstance(this);
    }

    private void setStrictMode() {
        if (!ENABLE_STRICT_MODE) {
            return;
        }
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectCustomSlowCalls()
                .build();
        StrictMode.setThreadPolicy(threadPolicy);

        StrictMode.VmPolicy vmPolicy = new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build();
        StrictMode.setVmPolicy(vmPolicy);
    }
}
