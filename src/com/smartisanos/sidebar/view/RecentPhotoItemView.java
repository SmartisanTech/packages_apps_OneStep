package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

public class RecentPhotoItemView extends FrameLayout {

    private TextView mText;

    public RecentPhotoItemView(Context context) {
        this(context, null);
    }

    public RecentPhotoItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentPhotoItemView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentPhotoItemView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mText = (TextView) findViewById(R.id.text);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mText != null) {
            mText.setText(R.string.open_gallery);
        }
    }
}
