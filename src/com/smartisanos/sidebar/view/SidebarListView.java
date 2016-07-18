package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;

public class SidebarListView extends ListView {
    private static final LOG log = LOG.getInstance(SidebarListView.class);

    private boolean mNeedFootView = false;
    private View mFootView;
    private boolean mCanAccpeeDrag = true;

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

    public void setCanAcceptDrag(boolean can){
        mCanAccpeeDrag = can;
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        if(!mCanAccpeeDrag){
            return false;
        }
        return super.dispatchDragEvent(event);
    }

    public void setFake(SidebarListView fake){
        mFake = fake;
    }

    private DragEventAdapter mAdapter;
    private SidebarListView mFake;
    private DragEvent mDragEvent;

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if(adapter instanceof DragEventAdapter){
            mAdapter = (DragEventAdapter) adapter;
        }else{
            mAdapter = null;
        }
    }

    private ScrollView getScrollViewParent(){
        View now = this;
        while(now.getParent() != null){
            now = (View) now.getParent();
            if(now instanceof ScrollView){
                return (ScrollView)now;
            }
        }
        return null;
    }

    private int getTopUtilScrollView(View now){
        int ret = 0;
        while(now != null && !(now instanceof ScrollView)){
            ret += now.getTop();
            now = (View) now.getParent();
        }
        return ret;
    }

    public List<View> getChildViews() {
        List<View> views = new ArrayList<View>();
        int viewCount = getChildCount();
        for (int i = 0; i < viewCount; i++) {
            View child = getChildAt(i);
            views.add(child);
        }
        return views;
    }

    public List<View> getVisibleChild() {
        List<View> ret = new ArrayList<View>();
        int parentScrollY = getScrollViewParent().getScrollY();
        int parentHeight = getScrollViewParent().getHeight();
        int parentTop = getTopUtilScrollView(SidebarListView.this);
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            int myTop = parentTop + child.getTop();
            int relate = myTop - parentScrollY;
            if (relate + child.getHeight() <= 0 || relate > parentHeight) {
                continue;
            }
            ret.add(child);
        }
        return ret;
    }

    private static final int ANIM_DURA = 150;

    private void showAnimWhenIn(final AnimatorListener listener) {
        getRootView().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        final long animDelay = 15;
                        final List<View> childs = getChildViews();
                        for (int i = 0; i < childs.size(); ++i) {
                            final View child = childs.get(i);
                            child.setAlpha(0);
                            child.setTranslationY(-20f);
                            ViewPropertyAnimator anim = child
                                    .animate()
                                    .alpha(1)
                                    .translationY(0)
                                    .setDuration(ANIM_DURA)
                                    .setInterpolator(
                                            new DecelerateInterpolator(1.5f));
                            if (i == childs.size() - 1) {
                                anim.setListener(listener);
                            }
                            anim.setStartDelay(animDelay * i + 200);
                            anim.start();
                        }
                    }
                });
    }

    public void onDragStart(DragEvent event) {
        if(mDragEvent != null){
            mDragEvent.recycle();
            mDragEvent = null;
        }
        mDragEvent = DragEvent.obtain(event);
        if (mAdapter != null) {
            mAdapter.onDragStart(mDragEvent);
            if(mFake == null){
                // no anim!;
                return ;
            }
            showAnimWhenIn(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mDragEvent != null) {
                        mFake.onDragStart(mDragEvent);
                    }
                }
            });

            mFake.setVisibility(View.VISIBLE);
            post(new Runnable() {
                @Override
                public void run() {
                    mFake.dismiss(mDragEvent);
                }
            });
        }
    }

    public void onDragEnd() {
        if (mDragEvent != null) {
            mDragEvent.recycle();
            mDragEvent = null;
        }
        if (mAdapter != null) {
            mAdapter.onDragEnd();
            if(mFake == null){
                // no anim
                return ;
            }

            showAnimWhenIn(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFake.onDragEnd();
                }
            });

            mFake.setVisibility(View.VISIBLE);
            post(new Runnable() {
                @Override
                public void run() {
                    mFake.dismiss(null);
                }
            });
        }
    }

    public void dismiss(final DragEvent event) {
        final long delayStep = 15;
        final List<View> childs = getChildViews();
        AnimTimeLine timeLine = new AnimTimeLine();
        int count = childs.size();
        for (int i = 0; i < count; i++) {
            View child = childs.get(i);
            child.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            child.setDrawingCacheEnabled(false);
            child.setAlpha(1);
            child.setTranslationY(0);

            long moveDelay = delayStep * i;
            long alphaDelay = delayStep * (count - i);

            int fromY = 0;
            int toY = -20;
            Anim moveAnim = new Anim(child, Anim.MOVE, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(0, fromY), new Vector3f(0, toY));
            moveAnim.setDelay(moveDelay);
            Anim alphaAnim = new Anim(child, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            alphaAnim.setDelay(alphaDelay);

            timeLine.addAnim(moveAnim);
            timeLine.addAnim(alphaAnim);
        }
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(int type) {
                setVisibility(View.GONE);
                for (View child : childs) {
                    child.setLayerType(View.LAYER_TYPE_NONE, null);
                    child.setDrawingCacheEnabled(true);
                    child.setAlpha(1);
                    child.setTranslationY(0);
                }
            }
        });
        timeLine.start();
    }

    private int mPrePosition = -1;
    public void setPrePosition(int position) {
        mPrePosition = position;
    }

    private AnimTimeLine moveAnimTimeLine;

    public void pointToNewPositionWithAnim(int position) {
        if (position < 0) {
            return;
        }
        int count = getCount() - getFooterViewsCount();
        if(position >= count){
            return ;
        }
        if (mPrePosition == position) {
            return;
        }
        mPrePosition = position;
        View[] viewArr = new View[count];
        int index = 0;
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (view.getVisibility() == View.INVISIBLE) {
                viewArr[position] = view;
            } else {
                if (index == position) {
                    index ++ ;
                }
                viewArr[index ++] = view;
            }
        }
        int[] listViewLoc = new int[2];
        getLocationOnScreen(listViewLoc);
        moveAnimTimeLine = new AnimTimeLine();
        int toY = 0;
        for (int i = 0; i < viewArr.length; i++) {
            View view = viewArr[i];
            int fromY = (int) view.getY();
            if (fromY != toY) {
                Vector3f from = new Vector3f(0, fromY);
                Vector3f to = new Vector3f(0, toY);
                Anim anim = new Anim(view, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
                moveAnimTimeLine.addAnim(anim);
            }
            toY += view.getHeight();
        }
        moveAnimTimeLine.start();
    }
}
