package com.smartisanos.sidebar.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.DragEvent;

public interface SidebarItem {
    CharSequence getDisplayName();
    Bitmap getAvatar();
    void setIndex(int index);
    int getIndex();
    void delete();
    boolean acceptDragEvent(Context context, DragEvent event);
    boolean handleDragEvent(Context context, DragEvent event);
    boolean openUI(Context context);
}
