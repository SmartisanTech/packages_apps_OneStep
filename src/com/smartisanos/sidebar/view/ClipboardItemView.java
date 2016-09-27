package com.smartisanos.sidebar.view;

import java.io.File;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.FileInfo;
import com.smartisanos.sidebar.util.Utils;

import android.content.Context;
import android.content.CopyHistoryItem;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import smartisanos.util.SidebarUtils;

public class ClipboardItemView extends LinearLayout {
    private TextView mDateText;
    private View mItemGroup;
    private TextView mText;
    private TextView mMoreLabel;

    public ClipboardItemView(Context context) {
        this(context, null);
    }

    public ClipboardItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipboardItemView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ClipboardItemView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //set layout
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.copy_history_item, this, true);
        // find view
        mDateText = (TextView) findViewById(R.id.date_content);
        mItemGroup = findViewById(R.id.text_item);
        mText = (TextView) findViewById(R.id.text);
        mMoreLabel = (TextView) findViewById(R.id.more_label);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mMoreLabel.setText(R.string.load_more);
    }

    public void reset() {
        mMoreLabel.setVisibility(View.GONE);
        mDateText.setVisibility(View.GONE);
        mItemGroup.setVisibility(View.GONE);
        setOnClickListener(null);
        setOnLongClickListener(null);
    }

    public void showItem(final CopyHistoryItem item) {
        mText.setText(item.mContent);
        mItemGroup.setVisibility(View.VISIBLE);
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Utils.copyText(mContext, item.mContent, false);
              Utils.resumeSidebar(mContext);
              Toast.makeText(mContext, R.string.text_copied, Toast.LENGTH_SHORT).show();
            }
        });

        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SidebarUtils.dragText(v, mContext, item.mContent);
                return true;
            }
        });
    }

    public void showDate(int resId) {
        mDateText.setText(resId);
        mDateText.setVisibility(View.VISIBLE);
    }

    public void showMoreTag(View.OnClickListener listener) {
        mMoreLabel.setVisibility(View.VISIBLE);
        setOnClickListener(listener);
    }
}
