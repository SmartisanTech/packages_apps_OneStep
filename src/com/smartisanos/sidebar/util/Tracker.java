package com.smartisanos.sidebar.util;

import android.app.Application;

public class Tracker {
    public static final String EVENT_ONLAUNCH = "EVENT_ONLAUNCH";
    public static final String EVENT_SWITCH = "EVENT_SWITCH";
    public static final String EVENT_SET = "EVENT_SET";
    public static final String EVENT_COPY = "EVENT_COPY";
    public static final String EVENT_MAKESURE_CLEAN = "EVENT_MAKESURE_CLEAN";
    public static final String EVENT_CLEAN = "EVENT_CLEAN";
    public static final String EVENT_OPEN_DOC = "EVENT_OPEN_DOC";
    public static final String EVENT_OPEN_PIC = "EVENT_OPEN_PIC";
    public static final String EVENT_TOPBAR = "EVENT_TOPBAR";
    public static final String EVENT_CLICK_APP = "EVENT_CLICK_APP";
    public static final String EVENT_CLICK_CHANGE = "EVENT_CLICK_CHANGE";
    public static final String EVENT_CLICK_CONTACTS = "EVENT_CLICK_CONTACTS";
    public static final String STATUS_APPNAME = "STATUS_APPNAME";
    public static final String EVENT_SUCCESS_DRAG = "EVENT_SUCCESS_DRAG";
    //TODO
    public static final String EVENT_DRAG = "EVENT_DRAG";
    public static final String EVENT_EXIT = "EVENT_EXIT";
    public static final String EVENT_SIGN = "EVENT_SIGN";

    public static void init(Application application) {
    }

    public static void onClick(String tag) {
    }

    public static void onClick(String tag, String... data) {
    }

    public static void reportStatus(String tag, String... data) {
    }

    public static void dragSuccess(int dragTarget, String targetPackage) {
    }

    public static void onEvent(String tag, String subTag, int action, String data) {
    }

    public static void onEvent(String tag, int action, String... data) {

    }

    public static void flush() {
    }
}