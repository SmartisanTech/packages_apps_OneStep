package com.smartisanos.sidebar.view;

import android.view.DragEvent;

public abstract class DragEventAdapter extends DragSortAdapter {
    public abstract void onDragStart(DragEvent event);
    public abstract void onDragEnd();
}
