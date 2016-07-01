package com.smartisanos.sidebar.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.RemoteException;
import android.view.View;

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

    public static void setAlwaysCanAcceptDrag(View view, boolean can){
        // NA
        try {
            Method setAlwaysCanAcceptDrag = view.getClass().getMethod("setAlwaysCanAcceptDrag", boolean.class);
            try {
                setAlwaysCanAcceptDrag.invoke(view, can);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
