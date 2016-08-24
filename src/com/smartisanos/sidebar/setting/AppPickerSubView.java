package com.smartisanos.sidebar.setting;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.AppItem;
import com.smartisanos.sidebar.util.AppManager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AppPickerSubView extends RelativeLayout implements View.OnClickListener {

    private AppItem mAppItem;
    private boolean mSelected = false;
    private ImageView mIcon, mSelectedView;
    private TextView mAppName;
    public AppPickerSubView(Context context) {
        this(context, null);
    }

    public AppPickerSubView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppPickerSubView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AppPickerSubView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIcon = (ImageView) findViewById(R.id.icon);
        mSelectedView = (ImageView) findViewById(R.id.selected);
        mAppName = (TextView) findViewById(R.id.app_name);
        setOnClickListener(this);
    }

    public void setAppItem(AppItem ai, boolean selected) {
        mAppItem = ai;
        mSelected = selected;
        updateUI();
    }

    public void clear() {
        mAppItem = null;
        mSelected = false;
        updateUI();
    }

    private void updateUI() {
        if (mIcon == null || mSelectedView == null) {
            // inflate not finished
            return;
        }
        if (mAppItem != null) {
            setVisibility(View.VISIBLE);
            mIcon.setImageBitmap(mAppItem.getAvatar());
            mAppName.setText(mAppItem.getDisplayName());
            mSelectedView.setVisibility(mSelected ? View.VISIBLE : View.INVISIBLE);
        } else {
            setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (mAppItem != null) {
            if (mSelected) {
                AppManager.getInstance(mContext).removeAppItem(mAppItem);
                mSelected = false;
            } else {
                AppManager.getInstance(mContext).addAppItem(mAppItem);
                mSelected = true;
            }
            updateUI();
        }
    }
}
