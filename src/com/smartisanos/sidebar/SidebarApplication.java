package com.smartisanos.sidebar;

import com.smartisanos.sidebar.util.Constants;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.MailContactsHelper;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.ThreadVerify;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;

import android.app.Application;
import android.os.Process;
import android.os.StrictMode;

public class SidebarApplication extends Application {
    private static final LOG log = LOG.getInstance(SidebarApplication.class);

    private static final boolean ENABLE_STRICT_MODE = true;

    private volatile static SidebarApplication myself;

    public static SidebarApplication getInstance() {
        return myself;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myself = this;
        Constants.init(this);
        AnimStatusManager.getInstance().reset();
        ThreadVerify.PROCESS_ID = Process.myTid();
        setStrictMode();
        // this is necessary ! init it to make its inner data be filled
        // so we can use it correctly later
        MailContactsHelper.getInstance(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        myself = null;
        RecentFileManager.getInstance(getApplicationContext()).stopFileObserver();
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
