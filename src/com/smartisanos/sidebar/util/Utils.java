package com.smartisanos.sidebar.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.view.DragEvent;
import android.view.View;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;

public class Utils {
    private static final LOG log = LOG.getInstance(Utils.class);

    static void sendCloseSystemWindows(Context context, String reason) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs(reason);
            } catch (RemoteException e) {
            }
        }
    }

    public static void resumeSidebar(Context context){
        log.error("super method resumeSidebar !");
        SidebarController.getInstance(context).resumeTopView();
        SidebarController.getInstance(context).dismissContent(true);
        SidebarController.getInstance(context).getSidebarRootView().stopDrag();
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

    public static void copyText(Context context, CharSequence cs, boolean inHistory){
        ClipboardManager cm  = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        try {
            Method method = cm.getClass().getMethod("setPrimaryClip", ClipData.class, boolean.class);
            try {
                method.invoke(cm, ClipData.newPlainText(null, cs), false);
                return;
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
        cm.setPrimaryClip(ClipData.newPlainText(null, cs));
    }

    public static boolean isPackageInstalled(Context context, String packageName){
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (NameNotFoundException e) {
            // NA
        }
        return false;
    }

    private static final String LAUNCHER_NAME = "com.smartisanos.launcher.Launcher";

    public static boolean launcherIsTopActivity(Context context) {
        boolean isMatch = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            List<RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && tasks.size() > 0) {
                RunningTaskInfo info = tasks.get(0);
                if (info != null) {
                    isMatch = info.topActivity.getClassName().equals(LAUNCHER_NAME);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMatch;
    }

    public static boolean inArea(float rawX, float rawY, View view) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        int left = loc[0];
        int top = loc[1];
        int right = left + viewWidth;
        int bottom = top + viewHeight;
        if (left < rawX && rawX < right) {
            if (top < rawY && rawY < bottom) {
                return true;
            }
        }
        return false;
    }

    public static String debugDrag(DragEvent event) {
        StringBuffer buffer = new StringBuffer();
        int action = event.getAction();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED : {
                buffer.append("[ACTION_DRAG_STARTED]");
                break;
            }
            case DragEvent.ACTION_DRAG_ENTERED : {
                buffer.append("[ACTION_DRAG_ENTERED]");
                break;
            }
            case DragEvent.ACTION_DRAG_LOCATION : {
                buffer.append("[ACTION_DRAG_LOCATION]");
                break;
            }
            case DragEvent.ACTION_DRAG_EXITED : {
                buffer.append("[ACTION_DRAG_EXITED]");
                break;
            }
            case DragEvent.ACTION_DRAG_ENDED : {
                buffer.append("[ACTION_DRAG_ENDED]");
                break;
            }
            case DragEvent.ACTION_DROP : {
                buffer.append("[ACTION_DROP]");
                break;
            }
        }
        return buffer.toString();
    }

    public static boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                connected = networkInfo.isConnected();
            }
        } catch (Exception e) {}
        return connected;
    }

    public static boolean isWifiConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected();
    }

    public static String parseTitle(String content) {
        String title = null;
        try {
            //html code, may include <!-- -->
            List<int[]> invisibleContent = parseContent("(?s)/<[!]--.*-->/", content);
            List<int[]> titleContent = parseContent("<title>(.*)</title>", content);
            int[] region = null;
            int size = titleContent.size();
            for (int i = 0; i < size; i++) {
                int[] charIndex = titleContent.get(i);
                if (invisibleContent.size() == 0) {
                    //match first
                    region = charIndex;
                    break;
                }
                boolean inBlock = false;
                for (int[] block : invisibleContent) {
                    if (block[0] <= charIndex[0] && charIndex[0] <= block[1]) {
                        inBlock = true;
                        break;
                    }
                    if (block[0] <= charIndex[1] && charIndex[1] <= block[1]) {
                        inBlock = true;
                        break;
                    }
                }
                if (!inBlock) {
                    region = charIndex;
                }
            }
            if (region != null) {
                title = content.substring(region[0], region[1]);
                if (title != null) {
                    int length = title.length();
                    title = title.substring(7, length - 8);
                }
            }
        } catch (Exception e) {
            title = null;
            e.printStackTrace();
        }
        return title;
    }

    private static List<int[]> parseContent(String expression, String content) {
        List<int[]> list = new ArrayList<int[]>();
        try {
            Pattern pattern = Pattern.compile(expression, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                int[] match = new int[2];
                match[0] = matcher.start();
                match[1] = matcher.end();
                list.add(match);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String convertDateToLabel(Context context, long currentTime, long time) {
        long day = 24 * 60 * 60 * 1000;
        long delta = currentTime - time;
        if (delta < 0) {
            delta = 0;
        }
        int interval = (int) (delta / day);
        Resources resources = context.getResources();
        String label = null;
        if (interval <= 30) {
            if (interval == 0) {
                //today
                label = resources.getString(R.string.date_label_today);
            } else if (interval == 1) {
                //yesterday
                label = resources.getString(R.string.date_label_yesterday);
            } else if (interval <= 7) {
                //last 7 days
                label = resources.getString(R.string.date_label_last_week);
            } else {
                //last 30 days
                label = resources.getString(R.string.date_label_last_month);
            }
        } else {
            label = resources.getString(R.string.date_label_earlier);
            //show month & year
//            Calendar now = Calendar.getInstance();
//            now.setTimeInMillis(currentTime);
//            Calendar date = Calendar.getInstance();
//            date.setTimeInMillis(time);
//            if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
//                //same year, show month
//                int month = date.get(Calendar.MONTH);
//                label = resources.getString(Constants.MONTH_ARRAY[month]);
//            } else {
//                //show year
//                label = "" + date.get(Calendar.YEAR);
//            }
        }
        return label;
    }

    public static List<ResolveInfo> getAllAppsInfo(Context context) {
        return context.getPackageManager().queryIntentActivities(Intent.makeMainActivity(null), 0);
    }
}
