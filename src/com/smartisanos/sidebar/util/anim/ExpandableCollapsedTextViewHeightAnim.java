package com.smartisanos.sidebar.util.anim;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.smartisanos.sidebar.util.LOG;

public class ExpandableCollapsedTextViewHeightAnim extends Animation {
    private static final LOG log = LOG.getInstance(ExpandableCollapsedTextViewHeightAnim.class);

    private TextView mTextView;
    private int mFrom;
    private int mTo;

    public ExpandableCollapsedTextViewHeightAnim() {
    }

    public void init(TextView textView, int from, int to, int time) {
        mTextView = textView;
        mFrom = from;
        mTo = to;
        setDuration(time);
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
        mTextView.setMaxHeight(newHeight);
        mTextView.getLayoutParams().height = newHeight;
        mTextView.requestLayout();
        if (interpolatedTime == 1) {
            cancel();
        }
    }

    public void start() {
        mTextView.startAnimation(this);
    }
}