package com.smartisanos.sidebar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.LOG;

public class AppResumeReceiver extends BroadcastReceiver {
    private static final LOG log = LOG.getInstance(AppResumeReceiver.class);

    private static final String APP_RESUME_ACTION = "android.intent.action.ACTIVITY_RESUMED";
    private static final String EXTRA_ACTIVITY_NAME = "android.intent.extra.activiti_name";

    private static final String LAUNCHER = "com.smartisanos.launcher/com.smartisanos.launcher.Launcher";
    private static final String LAUNCHER_COMPONENT = "com.smartisanos.launcher.Launcher";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null || !action.equals(APP_RESUME_ACTION)) {
            return;
        }
        String componentName = intent.getStringExtra(EXTRA_ACTIVITY_NAME);
        if (componentName == null) {
            return;
        }
        int mode = SidebarController.BG_MODE_LIGHT;
        if (componentName.endsWith(LAUNCHER_COMPONENT)) {
            mode = SidebarController.BG_MODE_DARK;
        }
        SidebarController.getInstance(context).updateBgMode(mode);
    }
}