package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.util.ScrollController;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.widget.ScrollView;

public class DragScrollView extends ScrollView {

    private ScrollController mScrollController;

    public DragScrollView(Context context) {
        this(context, null);
    }

    public DragScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScrollController = new ScrollController(this);
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        int action = event.getAction();
        switch (action) {
        case DragEvent.ACTION_DRAG_LOCATION:
            float y = event.getY();
            if (y < 30) {
                mScrollController.scrollDown();
                return true;
            } else if (y > this.getHeight() - 200) {
                mScrollController.scrollUp();
                return true;
            }
            break;
        }
        mScrollController.stopScroll();
        return super.dispatchDragEvent(event);
    }
}
