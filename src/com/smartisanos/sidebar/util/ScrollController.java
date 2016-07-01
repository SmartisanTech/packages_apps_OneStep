package com.smartisanos.sidebar.util;

import android.util.Log;
import android.widget.ScrollView;

public class ScrollController {

    // 50 times on second !
    private static final int DELAY = 20;
    private static final int NUMBS = 200;

    private static final int DIRECTION_DOWN = -1;
    private static final int DIRECTION_UP = 1;

    private ScrollView mView;

    private int mInitDel = 0;
    private int mScrollDirection = 0;

    public ScrollController(ScrollView view) {
        mView = view;
    }

    private void scroll(int direction) {
        if (mScrollDirection == direction) {
            return;
        }
        mView.removeCallbacks(mScrollRunnable);
        mScrollDirection = direction;
        mInitDel = mView.getHeight() / NUMBS;
        if (mInitDel <= 0) {
            mInitDel = 1;
        }
        mView.post(mScrollRunnable);
    }

    public void scrollUp() {
        scroll(DIRECTION_UP);
    }

    public void scrollDown() {
        scroll(DIRECTION_DOWN);
    }

    public void stopScroll() {
        mView.removeCallbacks(mScrollRunnable);
        mScrollDirection = 0;
    }

    private Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            mView.scrollBy(0, mInitDel * mScrollDirection);
            mView.postDelayed(mScrollRunnable, DELAY);
        }
    };
}
