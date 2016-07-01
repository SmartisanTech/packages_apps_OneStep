package com.smartisanos.sidebar.view;

import android.view.DragEvent;
import android.widget.BaseAdapter;

public abstract class DragEventAdapter extends BaseAdapter {
    public abstract void onDragStart(DragEvent event);
    public abstract void onDragEnd();
}
