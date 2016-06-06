package com.smartisanos.sidebar.util;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.RemoteException;

import com.smartisanos.sidebar.SidebarController;

public class Utils {
    static void sendCloseSystemWindows(Context context, String reason) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs(reason);
            } catch (RemoteException e) {
            }
        }
    }

    public static void resumeSidebar(Context context){
        SidebarController.getInstance(context).resumeTopView();
        SidebarController.getInstance(context).dismissContent();
    }

    public static void dismissAllDialog(Context context) {
        resumeSidebar(context);
        sendCloseSystemWindows(context, null);
    }
}
