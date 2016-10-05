package com.smartisanos.sidebar.view;

import android.view.DragEvent;
import android.widget.BaseAdapter;

public abstract class SidebarAdapter extends BaseAdapter {
    public abstract void moveItemPostion(Object object, int index);

    public abstract void onDragStart(DragEvent event);

    public abstract void onDragEnd();

    /**
     * when data change, update date set in this method. SidebarListView
     * will invoke this method when its dataset-change-animation finished.
     */
    public abstract void updateData();
}
