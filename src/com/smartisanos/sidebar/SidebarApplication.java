package com.smartisanos.sidebar;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.os.StrictMode;

import com.smartisanos.sidebar.util.Constants;
import com.smartisanos.sidebar.util.CalendarIcon;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.MailContactsHelper;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.ThreadVerify;
import com.smartisanos.sidebar.util.Tracker;
import com.smartisanos.sidebar.util.UserPackage;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;

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
        UserPackage.registerCallback(this);
        AnimStatusManager.getInstance().reset();
        ThreadVerify.PROCESS_ID = Process.myTid();
        setStrictMode();
        // this is necessary ! init it to make its inner data be filled
        // so we can use it correctly later
        MailContactsHelper.getInstance(this);

        IntentFilter dateFilter = new IntentFilter();
        dateFilter.addAction(ACTION_UPDATE_CALENDAR_DATE);
        dateFilter.addAction(Intent.ACTION_DATE_CHANGED);
        dateFilter.addAction(Intent.ACTION_TIME_CHANGED);
        dateFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mDateTimeReceiver, dateFilter);
        Tracker.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Tracker.flush();
        UserPackage.unregisterCallback(this);
        myself = null;
        RecentFileManager.getInstance(getApplicationContext()).stopFileObserver();
        unregisterReceiver(mDateTimeReceiver);
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

    public static final String ACTION_UPDATE_CALENDAR_DATE = "smartisan.intent.action.update_calendar_date";

    private final BroadcastReceiver mDateTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_CALENDAR_DATE.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_DATE_CHANGED.equals(action)) {
                SidebarController.getInstance(context).refreshCalendarView();
                CalendarIcon.releaseOldIcon();
            }
        }
    };
}
