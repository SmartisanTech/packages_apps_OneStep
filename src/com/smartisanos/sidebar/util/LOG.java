package com.smartisanos.sidebar.util;

import android.util.Log;
import android.os.SystemProperties;

import java.util.ArrayList;

public class LOG {
    public static final String TAG = "Sidebar";
    private Class logOwner = null;
    private String className = null;
    private static boolean ENABLE_DETAIL_INFO = false;
    private boolean ENABLE_DETAIL_INFO_BY_CLASS = false;
    private static final boolean ENABLE_TRACE = false;
    public static boolean DISABLE_INFO_LOG = true;
    public static boolean ENABLE_DEBUG = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    public static final boolean IS_USER = !(SystemProperties.getInt("ro.debuggable", 0) == 1);

    public static void openLog() {
        ENABLE_DEBUG = true;
    }

    private boolean close = false;
    public void close() {
        close = true;
    }

    public static void i(String info) {
        _info(sgetLogString(info));
    }

    public static void e(String info) {
        _error(sgetLogString(info));
    }

    public static void i(String tag, String info) {
        if (DISABLE_INFO_LOG) {
            return;
        }
        _info(sgetLogString(tag + " : " + info));
    }

    public static void e(String tag, String info) {
        _error(sgetLogString(tag + " : " + info));
    }

    private static String sgetLogString(String info) {
        if(ENABLE_DETAIL_INFO) {
            return getDetail(info);
        } else {
            return info;
        }
    }

    private LOG(Class owner) {
        if(owner == null) {
            throw new IllegalArgumentException("LOG must be init by class object");
        }
        logOwner = owner;
        className = getClassName(logOwner);
    }

    private String getClassName(Class c) {
        if(c == null) {
            return null;
        }
        String cName = c.getName();
        return cName.substring(cName.lastIndexOf(".") + 1);
    }

    public static LOG getInstance(Class owner) {
        return new LOG(owner);
    }

    /**
     * if enableDetailGlobal true, all LOG out info will print detail log, set false to close it
     * @param flag
     */
    public static void enableDetailGlobal(boolean flag) {
        ENABLE_DETAIL_INFO = flag;
    }

    /**
     * if enableDetailByClass true, this LOG object will print detail log, set false to close it.
     * @param flag
     */
    public void enableDetailByClass(boolean flag) {
        ENABLE_DETAIL_INFO_BY_CLASS = flag;
    }

    public static void trace() {
        trace("");
    }

    /**
     * trace method, if you want know who call this method.
     */
    public static void trace(String traceName) {
        if(!ENABLE_TRACE) {return;}
        Exception ex = new Exception();
        StackTraceElement[] traceArr = ex.getStackTrace();
        _error("########## trace begin  ########## " + traceName);
        for (StackTraceElement ste : traceArr) {
            _error(ste.toString());
        }
        _error("########## trace finish ##########");
    }

    private static ArrayList getLogTrace() {
        ArrayList list = null;
        StackTraceElement[] traceArr = (new Exception()).getStackTrace();
        if(traceArr != null && traceArr.length > 4) {
            String className = traceArr[4].getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            String methodName = traceArr[4].getMethodName();
            int lineNum = traceArr[4].getLineNumber();
            list = new ArrayList();
            list.add(className);
            list.add(methodName);
            list.add(lineNum);
        }
        traceArr = null;
        return list;
    }

    private static String getDetail(String info) {
        StringBuffer buf = new StringBuffer();
        ArrayList details = getLogTrace();
        if(details != null && details.size() == 3) {
            buf.append(details.get(0));
            buf.append("->");
            buf.append(details.get(1));
            buf.append(" : ");
            buf.append(details.get(2));
            buf.append(" ");
        }
        buf.append(info);
        return buf.toString();
    }

    public static String getLogDetail() {
        StringBuffer buf = new StringBuffer();
        ArrayList details = getLogTrace();
        if(details != null && details.size() == 3) {
            buf.append(details.get(0));
            buf.append("->");
            buf.append(details.get(1));
            buf.append(" : ");
            buf.append(details.get(2));
        }
        return buf.toString();
    }

    private String getLogString(String info) {
        if(ENABLE_DETAIL_INFO || ENABLE_DETAIL_INFO_BY_CLASS) {
            return getDetail(info);
        } else {
            if(className != null) {
                return className + " : " + info;
            }
            return info;
        }
    }

    public void info(String info) {
        if (DISABLE_INFO_LOG) {
            return;
        }
        if (close) { return; }
        _info(getLogString(info));
    }

    public void error(String info) {
        _error(getLogString(info));
    }

    public void info(String tag, String info) {
        if (DISABLE_INFO_LOG) {
            return;
        }
        if (close) { return; }
        _info(getLogString(tag + " : " + info));
    }

    public void error(String tag, String info) {
        if (close) { return; }
        _error(getLogString(tag + " : " + info));
    }

    private static void _info(String info) {
        if (DISABLE_INFO_LOG) {
            return;
        }
        if(IS_USER) {
            Log.e(TAG, info);
        } else {
            Log.i(TAG, info);
        }
    }

    private static void _error(String info) {
        Log.e(TAG, info);
    }
}
