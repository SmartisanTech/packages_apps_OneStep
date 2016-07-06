package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.ScrollController;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.widget.ScrollView;

public class DragScrollView extends ScrollView {

    private ScrollController mScrollController;

    private int mTopArea = 0;
    private int mBottomArea = 0;

    public DragScrollView(Context context) {
        this(context, null);
    }

    public DragScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScrollController = new ScrollController(this);
        mTopArea = getResources().getDimensionPixelSize(R.dimen.drag_scroll_view_top_area);
        mBottomArea = getResources().getDimensionPixelSize(R.dimen.drag_scroll_view_bottom_area);
    }

    private boolean isScrollUp(){
        return getScrollY() == 0;
    }

    private boolean isScrollBottom(){
        if(getChildCount() <= 0){
            return true;
        }
        return getChildAt(0).getMeasuredHeight() <= getScrollY() + getHeight();
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        int action = event.getAction();
        switch (action) {
        case DragEvent.ACTION_DRAG_LOCATION:
            float y = event.getY();
            if (y < mTopArea && !isScrollUp()) {
                mScrollController.scrollDown();
                return true;
            } else if (y > this.getHeight() - mBottomArea && !isScrollBottom()) {
                mScrollController.scrollUp();
                return true;
            }
            break;
        }
        mScrollController.stopScroll();
        return super.dispatchDragEvent(event);
    }
}
