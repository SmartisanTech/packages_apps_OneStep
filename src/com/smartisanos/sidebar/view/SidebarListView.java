package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.SidebarAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SidebarListView extends ListView {

    private DragEvent mStartDragEvent = null;
    private boolean mNeedFootView = false;
    private View mFootView;
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
        mFootView = LayoutInflater.from(mContext).inflate(R.layout.sidebar_view_divider, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    public void setNeedFootView(boolean needFootView) {
        if (needFootView != mNeedFootView) {
            mNeedFootView = needFootView;
            requestLayout();
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        if(mFootView == null){
            // this means the construcor is going on !
            return;
        }
        if (mNeedFootView && (getAdapter() != null && !getAdapter().isEmpty())) {
            if (getFooterViewsCount() == 0) {
                addFooterView(mFootView);
            }
        } else {
            if (getFooterViewsCount() > 0) {
                removeFooterView(mFootView);
            }
        }
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
