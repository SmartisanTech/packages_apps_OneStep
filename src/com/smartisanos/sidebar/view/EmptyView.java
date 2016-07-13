package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EmptyView extends LinearLayout {

    private ImageView mImageView;
    private TextView mText, mHint;

    public EmptyView(Context context) {
        super(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.empty_image_view);
        mText = (TextView) findViewById(R.id.empty_text);
        mHint = (TextView) findViewById(R.id.empty_hint);
    }

    public void setImageView(int resId) {
        if (mImageView != null) {
            mImageView.setImageResource(resId);
        }
    }

    public void setText(int resId) {
        if (mText != null) {
            mText.setText(resId);
        }
    }

    public void setHint(int resId) {
        if (mHint != null) {
            mHint.setText(resId);
        }
    }
}
