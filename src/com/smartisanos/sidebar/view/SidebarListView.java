package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.util.SidebarAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SidebarListView extends ListView {

    private DragEvent mStartDragEvent = null;

    public SidebarListView(Context context) {
        super(context, null);
    }

    public SidebarListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SidebarListView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SidebarListView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
//        if(event.getAction() == DragEvent.ACTION_DRAG_STARTED){
//            mStartDragEvent = DragEvent.obtain(event);
//            if(getAdapter() instanceof SidebarAdapter){
//                SidebarAdapter adapter = (SidebarAdapter)getAdapter();
//                adapter.setDragEvent(event);
//            }
//        }else if(event.getAction() == DragEvent.ACTION_DRAG_ENDED){
//            mStartDragEvent.recycle();
//            mStartDragEvent = null;
//            if(getAdapter() instanceof SidebarAdapter){
//                SidebarAdapter adapter = (SidebarAdapter)getAdapter();
//                adapter.setDragEvent(null);
//            }
//        }
        return super.dispatchDragEvent(event);
    }
}
