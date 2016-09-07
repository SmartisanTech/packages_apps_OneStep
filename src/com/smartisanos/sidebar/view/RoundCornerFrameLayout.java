package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Path.Direction;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RoundCornerFrameLayout extends FrameLayout {

    private float mRadius;
    private Path mClip;
    private Drawable mViewMask;

    public RoundCornerFrameLayout(Context context) {
        this(context, null);
    }

    public RoundCornerFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundCornerFrameLayout(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RoundCornerFrameLayout(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mRadius = context.getResources().getDimensionPixelSize(R.dimen.clip_radius);
        mViewMask = context.getDrawable(R.drawable.view_mask);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mClip = new Path();
        RectF rectRound = new RectF(this.mPaddingLeft, this.mPaddingTop, w - this.mPaddingRight, h - this.mPaddingBottom);
        mClip.addRoundRect(rectRound, mRadius, mRadius, Direction.CW);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int saveCount = canvas.save();
        canvas.clipPath(mClip);
        super.dispatchDraw(canvas);
        // draw mask
        int bottom = mScrollY + mBottom - mTop - mPaddingBottom;
        mViewMask.setBounds(mScrollX + mPaddingLeft, bottom - mViewMask.getMinimumHeight(),
                mScrollX + mRight - mLeft - mPaddingRight, bottom);
        mViewMask.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
