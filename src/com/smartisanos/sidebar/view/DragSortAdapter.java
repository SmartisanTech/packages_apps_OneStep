package com.smartisanos.sidebar.view;

import android.widget.BaseAdapter;

public abstract class DragSortAdapter extends BaseAdapter {
    public abstract void moveItemPostion(Object object, int index);
}
