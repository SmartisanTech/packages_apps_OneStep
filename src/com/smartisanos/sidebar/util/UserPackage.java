package com.smartisanos.sidebar.util;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.os.UserHandle;

public class UserPackage {
    private static final LOG log = LOG.getInstance(UserPackage.class);

    public static final int USER_DOPPELGANGER = UserHandle.USER_DOPPELGANGER;
    public static final int USER_OWNER = UserHandle.USER_OWNER;

    private static Context mContext;

    public static void registerCallback(Context context) {
        log.error("registerCallback");
        mContext = context;
        LauncherApps service = getService(context);
        if (service != null) {
            service.registerCallback(mCallback);
        }
    }

    public static void unregisterCallback(Context context) {
        log.error("unregisterCallback");
        LauncherApps service = getService(context);
        if (service != null) {
            mContext = null;
            service.unregisterCallback(mCallback);
        }
    }

    private static LauncherApps getService(Context context) {
        return ((LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE));
    }

    private static final ServiceCallback mCallback = new ServiceCallback();

    private static class ServiceCallback extends LauncherApps.Callback {

        public void onPackageRemoved(String packageName, UserHandle user) {
            int userId = user.getIdentifier();
            log.error("onPackageRemoved ["+packageName+"], userId ["+userId+"]");
            if (userId == USER_DOPPELGANGER) {
                if (packageName != null) {
                    ContactManager.getInstance(mContext).removeDoppelgangerShortcut(packageName);
                }
            }
        }

        public void onPackageAdded(String packageName, UserHandle user) {
//            int userId = user.getIdentifier();
//            log.error("onPackageAdded ["+packageName+"], userId ["+userId+"]");
//            if (userId == USER_DOPPELGANGER) {
//            }
        }

        public void onPackageChanged(String packageName, UserHandle user) {
//            int userId = user.getIdentifier();
//            log.error("onPackageChanged ["+packageName+"], userId ["+userId+"]");
//            if (userId == USER_DOPPELGANGER) {
//            }
        }

        public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
        }

        public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
        }
    }
}