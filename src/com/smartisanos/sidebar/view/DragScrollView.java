package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;

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
        boolean intercept = mScrollController.onDragEvent(event);
        if (intercept) {
            return true;
        }
        return super.dispatchDragEvent(event);
    }

    private class ScrollController {

        // 50 times on second !
        private static final int DELAY = 20;
        private static final int NUMBS = 200;

        private static final int DIRECTION_DOWN = -1;
        private static final int DIRECTION_NONE = 0;
        private static final int DIRECTION_UP = 1;

        private ScrollView mView;
        private DragEvent mEvent;
        private float mRate = 1.0f;
        private int mInitDel = 0;
        private int mScrollDirection = 0;
        private int mTopArea;
        private int mBottomArea;
        public ScrollController(ScrollView view) {
            mView = view;
            mTopArea = getResources().getDimensionPixelSize(R.dimen.drag_scroll_view_top_area);
            mBottomArea = getResources().getDimensionPixelSize(R.dimen.drag_scroll_view_bottom_area);
        }

        private float getInterpolation(float rate) {
            return rate * rate;
        }

        private void setRate(float rate) {
            mRate = 1.0f + getInterpolation(rate) * 3;
        }

        public boolean onDragEvent(DragEvent event){
            int action = event.getAction();
            if(action ==  DragEvent.ACTION_DRAG_LOCATION){
                float y = event.getY();
                if(y < mTopArea && !isScrollUp()){
                    setEvent(event);
                    setRate((mTopArea - y)  * 1.0f / mTopArea);
                    scroll(DIRECTION_DOWN);
                    return true;
                }else if(y > mView.getHeight() - mBottomArea && !isScrollBottom()){
                    setEvent(event);
                    setRate(1 - (mView.getHeight() - y)  * 1.0f / mBottomArea);
                    scroll(DIRECTION_UP);
                    return true;
                }
            }
            setEvent(null);
            setRate(0.0f);
            scroll(DIRECTION_NONE);
            return false;
        }

        private void setEvent(DragEvent event) {
            if (mEvent != null) {
                mEvent.recycle();
                mEvent = null;
            }
            if (event != null) {
                mEvent = DragEvent.obtain(event);
            }
        }

        private boolean isScrollUp(){
            return mView.getScrollY() == 0;
        }

        private boolean isScrollBottom(){
            if(mView.getChildCount() <= 0){
                return true;
            }
            return mView.getChildAt(0).getMeasuredHeight() <= mView.getScrollY() + mView.getHeight();
        }

        private void scroll(int direction) {
            if (mScrollDirection == direction) {
                return;
            }
            mScrollDirection = direction;
            mView.removeCallbacks(mScrollRunnable);
            if (mScrollDirection != DIRECTION_NONE) {
                mInitDel = mView.getHeight() / NUMBS;
                if (mInitDel <= 0) {
                    mInitDel = 1;
                }
                mView.post(mScrollRunnable);
            }
        }

        private Runnable mScrollRunnable = new Runnable() {
            @Override
            public void run() {
                mView.scrollBy(0, (int) (mInitDel * mScrollDirection * mRate));
                mView.postDelayed(mScrollRunnable, DELAY);
                if (mEvent != null) {
                    DragScrollView.super.dispatchDragEvent(mEvent);
                }
            }
        };
    }

}
