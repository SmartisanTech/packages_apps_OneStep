package com.smartisanos.sidebar.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.json.JSONException;
import org.json.JSONObject;

import smartisanos.app.tracker.Agent;
import smartisanos.app.tracker.TrackerConstant;

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

    private static final String ACTION_DRAG = "com.smartisanos.sidebar.intent.action.event_drag";

    private static Agent mTrackerAgent;

    private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_DRAG.equals(action)) {
                String dragSource = intent.getStringExtra("drag_source");
                String dragContent = intent.getStringExtra("drag_content");
                String sourcePackage = intent.getStringExtra("source_package");
                onEvent(EVENT_DRAG, 0, "drag_source", dragSource, "drag_content", dragContent, "source_package", sourcePackage);
            }
        }
    };

    public static void init(Application application) {
        try {
            Agent.getInstance().init(application);
            mTrackerAgent = Agent.getInstance();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_DRAG);
            application.registerReceiver(mReceiver, intentFilter);
        } catch (Exception e) {
            // NA
        }
    }

    public static void onClick(String tag) {
        mTrackerAgent.onClick(tag, null);
    }

    public static void onClick(String tag, String... data) {
        onEvent(tag, TrackerConstant.ACTION_SINGLE_CLICK, data);
    }

    public static void reportStatus(String tag, String... data) {
        onEvent(tag, TrackerConstant.ACTION_STATUS_DATA, data);
    }

    public static void dragSuccess(int dragTarget, String targetPackage) {
        onEvent(EVENT_SUCCESS_DRAG, 0, "drag_target", dragTarget + "", "target_package", targetPackage);
    }

    public static void onEvent(String tag, String subTag, int action, String data) {
        mTrackerAgent.onEvent(tag, subTag, action, data);
    }

    public static void onEvent(String tag, int action, String... data) {
        if (data.length > 0 && (data.length & 0x01) == 0) {
            JSONObject jsonObject = new JSONObject();
            for (int i = 1; i < data.length; i += 2) {
                String key = data[i - 1];
                String value = data[i];
                try {
                    jsonObject.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mTrackerAgent.onEvent(tag, null, action, jsonObject.toString());
        }
    }

    public static void flush() {
        mTrackerAgent.flush();
    }
}