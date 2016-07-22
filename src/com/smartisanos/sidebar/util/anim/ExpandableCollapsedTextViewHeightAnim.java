package com.smartisanos.sidebar.util.anim;

import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.smartisanos.sidebar.util.LOG;

public class ExpandableCollapsedTextViewHeightAnim extends Animation {
    private static final LOG log = LOG.getInstance(ExpandableCollapsedTextViewHeightAnim.class);

    private ScrollView mTextScrollView;
    private int mFrom;
    private int mTo;

    public ExpandableCollapsedTextViewHeightAnim() {
    }

    public void init(ScrollView scrollView, int from, int to, int time) {
        mTextScrollView = scrollView;
        mFrom = from;
        mTo = to;
        setDuration(time);
        setInterpolator(new AnimInterpolator.Interpolator(Anim.CUBIC_OUT));
        log.error("ExpandableCollapsedTextViewHeightAnim from ["+from+"], to ["+to+"], time ["+time+"]");
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight = (int)((mTo - mFrom) * interpolatedTime + mFrom);
//            log.error("interpolatedTime ["+interpolatedTime+"], from ["+mFrom+"], to ["+mTo+"], new ["+newHeight+"]");
        mTextScrollView.getLayoutParams().height = newHeight;
        mTextScrollView.requestLayout();
        if (interpolatedTime == 1) {
            cancel();
        }
    }

    public void start() {
        mTextScrollView.startAnimation(this);
    }
}