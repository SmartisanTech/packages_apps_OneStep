package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.Tracker;
import com.smartisanos.sidebar.util.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class SwitchAppView extends LinearLayout {

    private View mIconView;

    public SwitchAppView(Context context) {
        this(context, null);
    }

    public SwitchAppView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchAppView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchAppView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOnClickListener(mSwitchAppListener);
        setOnFocusChangeListener(mFocusChangeListener);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = findViewById(R.id.view_icon);
    }

    public View getIconView() {
        return mIconView;
    }

    private View.OnClickListener mSwitchAppListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.dismissAllDialog(mContext);
            Utils.launchPreviousApp(mContext);
            Tracker.onClick(Tracker.EVENT_CLICK_CHANGE);
        }
    };

    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener(){
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                v.performClick();
            }
        }
    };
}
