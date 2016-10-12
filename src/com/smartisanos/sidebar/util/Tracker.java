package com.smartisanos.sidebar.util;

import android.app.Application;

import smartisanos.app.tracker.Agent;

public class Tracker {

    public static final String EVENT_SWITCH         = "EVENT_SWITCH";
    public static final String EVENT_SET            = "EVENT_SET";
//    public static final String EVENT_DELETE         = "EVENT_DELETE";
    public static final String EVENT_COPY           = "EVENT_COPY";
    public static final String EVENT_MAKESURE_CLEAN = "EVENT_MAKESURE_CLEAN";
    public static final String EVENT_CLEAN          = "EVENT_CLEAN";
    public static final String EVENT_OPEN_DOC       = "EVENT_OPEN_DOC";
    public static final String EVENT_OPEN_PIC       = "EVENT_OPEN_PIC";
    public static final String EVENT_TOPBAR         = "EVENT_TOPBAR";
    public static final String EVENT_CLICK_APP      = "EVENT_CLICK_APP";
    public static final String EVENT_CLICK_CHANGE   = "EVENT_CLICK_CHANGE";
    public static final String EVENT_CLICK_CONTACTS = "EVENT_CLICK_CONTACTS";
    public static final String STATUS_APPNAME       = "STATUS_APPNAME";
    //Todo
    public static final String EVENT_SUCCESS_DRAG   = "EVENT_SUCCESS_DRAG";
    public static final String EVENT_DRAG           = "EVENT_DRAG";
    public static final String EVENT_EXIT           = "EVENT_EXIT";
    public static final String EVENT_SIGN           = "EVENT_SIGN";
    public static final String EVENT_ONLAUNCH       = "EVENT_ONLAUNCH";

    private static Agent mTrackerAgent;
    public static void init(Application application) {
        try {
            Agent.getInstance().init(application);
        } catch (Exception e) {}
        mTrackerAgent = Agent.getInstance();
    }

    public static void onClick(String tag) {
        if (mTrackerAgent != null) {
            try {
                mTrackerAgent.onClick(tag, null);
            } catch (Exception e) {

            }
        }
    }

    public static void onClick(String tag, String subTag) {
        if (mTrackerAgent != null) {
            try {
                mTrackerAgent.onClick(tag, subTag);
            } catch (Exception e) {

            }
        }
    }

    public static void onStatusDataChanged(String tag, String subTag, String data) {
        try {
            if (mTrackerAgent != null) {
                mTrackerAgent.onStatusDataChanged(tag, subTag, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onEvent(String tag, String subTag, int action, String data) {
        try {
            if (mTrackerAgent != null) {
                mTrackerAgent.onEvent(tag, subTag, action, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void flush() {
        if (mTrackerAgent != null) {
            mTrackerAgent.flush();
        }
    }

    public static void onAttach(String tag, String subTag, String data) {
        mTrackerAgent.onAttach(tag, subTag, data);
    }
}