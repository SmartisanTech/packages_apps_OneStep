package com.smartisanos.sidebar.util;

import android.os.Process;

public class ThreadVerify {
    private static final LOG log = LOG.getInstance(ThreadVerify.class);

    private static final boolean DISABLE_THREAD_VERIFY = false;

    public static int PROCESS_ID = 0;

    public static void verify(boolean needRunningInUiThread) {
        if (DISABLE_THREAD_VERIFY) {
            return;
        }
        if (PROCESS_ID == 0) {
            log.error("verifyThread return by PROCESS_ID is 0");
            return;
        }
        boolean failed = false;
        int tid = Process.myTid();

        if (needRunningInUiThread) {
            if (tid != PROCESS_ID) {
                failed = true;
            }
        } else {
            if (tid == PROCESS_ID) {
                failed = true;
            }
        }
        if (failed) {
            log.error("verifyThread failed, MAIN thread ["+PROCESS_ID+"] Running thread ["+tid+"] needRunningInUiThread ["+needRunningInUiThread+"]");
            throw new IllegalArgumentException("verifyThread failed");
        }
    }

    public static void dumpTreadId() {
        int tid = Process.myTid();
        log.error("current running thread id ["+tid+"], Main thread id ["+PROCESS_ID+"]");
    }
}