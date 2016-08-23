package com.smartisanos.sidebar.util;

import android.content.Context;
import android.view.DragEvent;

public interface SidebarItem {
    CharSequence getDisplayName();
    void delete();
    boolean acceptDragEvent(Context context, DragEvent event);
    boolean handleDragEvent(Context context, DragEvent event);
    boolean openUI(Context context);
}
