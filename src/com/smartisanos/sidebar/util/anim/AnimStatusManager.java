package com.smartisanos.sidebar.util.anim;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.LOG;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Pair;

public class AnimStatusManager {
    private static final LOG log = LOG.getInstance(AnimStatusManager.class);

    public static final int ON_TOP_VIEW_CLICK         = 0x1;
    public static final int ON_SIDE_VIEW_ADD_CLICK    = 0x1 << 1;
    public static final int ON_RECENT_PHOTO_LIST_ANIM = 0x1 << 2;
    public static final int ON_FILE_LIST_ANIM         = 0x1 << 3;
    public static final int ON_CLIPBOARD_LIST_ANIM    = 0x1 << 4;
    public static final int ON_ADD_ITEM_VIEW_ANIM     = 0x1 << 5;
    public static final int ON_ADD_ITEM_ANIM          = 0x1 << 6;
    public static final int ON_TOP_VIEW_RESUME        = 0x1 << 7;
    public static final int ON_TOP_VIEW_ENTER         = 0x1 << 8;
    public static final int ON_TOP_VIEW_EXIT          = 0x1 << 9;
    public static final int ON_SIDE_VIEW_ENTER        = 0x1 << 10;
    public static final int ON_SIDE_VIEW_EXIT         = 0x1 << 11;
    public static final int ON_ADD_RIG_ITEM_REMOVE    = 0x1 << 12;

    public static final int [] STATUS_ARR = new int[] {
            ON_TOP_VIEW_CLICK,
            ON_SIDE_VIEW_ADD_CLICK,
            ON_RECENT_PHOTO_LIST_ANIM,
            ON_FILE_LIST_ANIM,
            ON_CLIPBOARD_LIST_ANIM,
            ON_ADD_ITEM_VIEW_ANIM,
            ON_ADD_ITEM_ANIM,
            ON_TOP_VIEW_RESUME,
            ON_TOP_VIEW_ENTER,
            ON_TOP_VIEW_EXIT,
            ON_SIDE_VIEW_ENTER,
            ON_SIDE_VIEW_EXIT,
            ON_ADD_RIG_ITEM_REMOVE
    };

    public static final Map<Integer, String> statusNameMap = new HashMap<Integer, String>();
    static {
//        Class clazz = AnimStatusManager.class;
//        Field[] fields = clazz.getFields();
//        AnimStatusManager obj = new AnimStatusManager();
//        for (Field f : fields) {
//            try {
//                String name = f.getName();
//                String typeName = f.getType().getName();
//                if (name != null
//                        && typeName.equals("int")
//                        && name.startsWith("ON_")) {
//                    int value = f.getInt(obj);
//                    log.info("AnimStatusManager key [" + name + "], value [" + value + "]");
//                    statusNameMap.put(value, name);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        statusNameMap.put(ON_TOP_VIEW_CLICK,         "ON_TOP_VIEW_CLICK");
        statusNameMap.put(ON_SIDE_VIEW_ADD_CLICK,    "ON_SIDE_VIEW_ADD_CLICK");
        statusNameMap.put(ON_RECENT_PHOTO_LIST_ANIM, "ON_RECENT_PHOTO_LIST_ANIM");
        statusNameMap.put(ON_FILE_LIST_ANIM,         "ON_FILE_LIST_ANIM");
        statusNameMap.put(ON_CLIPBOARD_LIST_ANIM,    "ON_CLIPBOARD_LIST_ANIM");
        statusNameMap.put(ON_ADD_ITEM_VIEW_ANIM,     "ON_ADD_ITEM_VIEW_ANIM");
        statusNameMap.put(ON_ADD_ITEM_ANIM,          "ON_ADD_ITEM_ANIM");
        statusNameMap.put(ON_TOP_VIEW_RESUME,        "ON_TOP_VIEW_RESUME");
        statusNameMap.put(ON_TOP_VIEW_ENTER,         "ON_TOP_VIEW_ENTER");
        statusNameMap.put(ON_TOP_VIEW_EXIT,          "ON_TOP_VIEW_EXIT");
        statusNameMap.put(ON_SIDE_VIEW_ENTER,        "ON_SIDE_VIEW_ENTER");
        statusNameMap.put(ON_SIDE_VIEW_EXIT,         "ON_SIDE_VIEW_EXIT");
        statusNameMap.put(ON_ADD_RIG_ITEM_REMOVE,    "ON_ADD_RIG_ITEM_REMOVE");
    }

    public static AnimStatusManager mManager;
    private volatile int mStatus = 0;
    private List<Pair<AnimFlagStatusChangedListener, Integer>> mFlagListeners = new ArrayList<Pair<AnimFlagStatusChangedListener, Integer>>();

    public static AnimStatusManager getInstance() {
        if (mManager == null) {
            mManager = new AnimStatusManager();
        }
        return mManager;
    }

    public void dumpStatus() {
        if (!LOG.ENABLE_DEBUG) {
            return;
        }
        for (int i = 0; i < STATUS_ARR.length; i++) {
            int status = STATUS_ARR[i];
            if (getStatus(status)) {
                String statusName = statusNameMap.get(status);
                log.error("status error, "+statusName+" is true");
            }
        }
    }

    public void reset() {
        mStatus = 0;
    }

    public void addAnimFlagStatusChangedListener(int flag, AnimFlagStatusChangedListener listener) {
        if(flag == 0 || listener == null) {
            return ;
        }
        mFlagListeners.add(new Pair<AnimFlagStatusChangedListener, Integer>(listener, flag));
    }

    public void removeAnimFlagStatusChangedListener(AnimFlagStatusChangedListener listener) {
        for (int i = 0; i < mFlagListeners.size(); ++i) {
            if (mFlagListeners.get(i).first == listener) {
                mFlagListeners.remove(i);
            }
        }
    }

    public void setStatus(int status, boolean value) {
        if (getStatus(status) == value) {
            return;
        }
        String statusName = statusNameMap.get(status);
        if (LOG.ENABLE_DEBUG) log.info("setStatus status ["+statusName+"], value ["+value+"]");
        int oldValue = mStatus;
        if (value) {
            mStatus |= status;
        } else {
            mStatus &= ~status;
        }

        for (int i = 0; i < mFlagListeners.size(); ++i) {
            int careFlag = mFlagListeners.get(i).second;
            if ((oldValue & careFlag) != (mStatus & careFlag)) {
                mFlagListeners.get(i).first.onChanged();
            }
        }
    }

    public int getStatus() {
        return mStatus;
    }

    public boolean getStatus(int status) {
        return (mStatus & status) == status;
    }

    public static final int SHOW_CONTENT_FLAG = ON_TOP_VIEW_CLICK
            | ON_SIDE_VIEW_ADD_CLICK
            | ON_RECENT_PHOTO_LIST_ANIM
            | ON_FILE_LIST_ANIM
            | ON_CLIPBOARD_LIST_ANIM
            | ON_ADD_ITEM_ANIM
            | ON_ADD_ITEM_VIEW_ANIM
            | ON_TOP_VIEW_RESUME
            | ON_TOP_VIEW_ENTER
            | ON_TOP_VIEW_EXIT
            | ON_SIDE_VIEW_ENTER
            | ON_SIDE_VIEW_EXIT
            | ON_ADD_RIG_ITEM_REMOVE;

    public boolean canShowContentView() {
        return (mStatus & SHOW_CONTENT_FLAG) == 0;
    }

    public static final int ENTER_ANIM_FLAG = ON_TOP_VIEW_ENTER
            | ON_SIDE_VIEW_ENTER;
    public static final int EXIT_ANIM_FLAG = ON_TOP_VIEW_EXIT
            | ON_SIDE_VIEW_EXIT;

    public boolean isEnterAnimOngoing() {
        return (mStatus & ENTER_ANIM_FLAG) != 0;
    }

    public boolean isExitAnimOngoing() {
        return (mStatus & EXIT_ANIM_FLAG) != 0;
    }

    public boolean canAddResoleInfoItem() {
        return (mStatus & ON_ADD_RIG_ITEM_REMOVE) == 0;
    }

    public interface AnimFlagStatusChangedListener {
        void onChanged();
    }
}