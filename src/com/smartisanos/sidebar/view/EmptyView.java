package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EmptyView extends LinearLayout {

    private ImageView mImageView;
    private TextView mText, mHint;
    private Button mButton;

    private int mTextResId, mHintResId;
    private int mButtonTextResId, mButtonBackgroundResId;

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
        mButton = (Button) findViewById(R.id.empty_button);
    }

    public void setImageView(int resId) {
        mImageView.setImageResource(resId);
    }

    public void setText(int resId) {
        mTextResId = resId;
        mText.setText(mTextResId);
    }

    public void setHint(int resId) {
        mHintResId = resId;
        mHint.setText(mHintResId);
    }

    public void setButton(int textResId, int backgroundResId, OnClickListener mOnClickListener) {
        mButtonTextResId = textResId;
        mButtonBackgroundResId = backgroundResId;
        mButton.setVisibility(VISIBLE);
        mButton.setText(mButtonTextResId);
        mButton.setBackgroundResource(mButtonBackgroundResId);
        mButton.setOnClickListener(mOnClickListener);
    }

    private void updateUI() {
        if (mTextResId != 0) {
            mText.setText(mTextResId);
        }

        if (mHintResId != 0) {
            mHint.setText(mHintResId);
        }

        if (mButtonTextResId != 0) {
            mButton.setText(mButtonTextResId);
        }
        if (mButtonBackgroundResId != 0) {
            mButton.setBackgroundResource(mButtonBackgroundResId);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUI();
    }
}
