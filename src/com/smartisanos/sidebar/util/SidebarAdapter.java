package com.smartisanos.sidebar.util;

import android.view.DragEvent;
import android.widget.BaseAdapter;

public abstract class SidebarAdapter extends BaseAdapter {
    private DragEvent mDragEvent;

    public void setDragEvent(DragEvent event) {
        mDragEvent = event;
    }

    public DragEvent getDragEvent() {
        return mDragEvent;
    }
}
