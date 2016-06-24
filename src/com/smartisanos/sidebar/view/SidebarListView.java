package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;

import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SidebarListView extends ListView {
    private static final LOG log = LOG.getInstance(SidebarListView.class);

    private Context mContext;
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
        mFootView = LayoutInflater.from(context).inflate(R.layout.sidebar_view_divider, null);
        mContext = context;
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

    private int mPrePosition = -1;
    public void setPrePosition(int position) {
        mPrePosition = position;
        if (mPrePosition == -1) {
            viewArr = null;
        }
    }

    private View[] viewArr = null;
    private AnimTimeLine moveAnimTimeLine;

    public void pointToNewPositionWithAnim(int position) {
        if (position < 0) {
            return;
        }
        if (mPrePosition == position) {
            return;
        }
        mPrePosition = position;
        int count = getCount();
        if (viewArr == null || viewArr.length != count) {
            viewArr = new View[count];
        }
        int index = -1;
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() == View.INVISIBLE) {
                viewArr[position] = view;
            } else {
                index = index + 1;
                if (index == position) {
                    index = index + 1;
                }
                viewArr[index] = view;
            }
        }
        int[] listViewLoc = new int[2];
        getLocationOnScreen(listViewLoc);
        moveAnimTimeLine = new AnimTimeLine();
        for (int i = 0; i < viewArr.length; i++) {
            View view = viewArr[i];
            int top = view.getTop();
            int height = view.getHeight();
            int viewIndex = top / height;
            int fromY = (int) view.getY();
            int toY = (height * i);
            if (fromY != toY) {
                log.error("view move from ["+viewIndex+"] to ["+i+"] ("+fromY+") -> ("+toY+")");
                Vector3f from = new Vector3f(0, fromY);
                Vector3f to = new Vector3f(0, toY);
                Anim anim = new Anim(view, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
                moveAnimTimeLine.addAnim(anim);
            }
        }
        moveAnimTimeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                log.error("move anim start");
            }

            @Override
            public void onComplete() {
                log.error("move anim end");
            }
        });
        moveAnimTimeLine.start();
    }
}
