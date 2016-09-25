package com.smartisanos.sidebar.view;

import java.io.File;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.FileInfo;
import com.smartisanos.sidebar.util.Utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import smartisanos.util.SidebarUtils;

public class RecentFileItemView extends LinearLayout {
    private TextView mDateText;
    private View mFileItemGroup;
    private ImageView mIcon;
    private TextView mFileName;
    private TextView mMoreLabel;

    public RecentFileItemView(Context context) {
        this(context, null);
    }

    public RecentFileItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentFileItemView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentFileItemView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //set layout
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.recent_file_item, this, true);
        // find view
        mDateText = (TextView) findViewById(R.id.date_content);
        mFileItemGroup = findViewById(R.id.recent_file_item);
        mFileName = (TextView) findViewById(R.id.file_name);
        mIcon = (ImageView) findViewById(R.id.file_icon);
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
        mFileItemGroup.setVisibility(View.GONE);
        setOnClickListener(null);
        setOnLongClickListener(null);
    }

    public void showItem(final FileInfo info) {
        mFileName.setText(new File(info.filePath).getName());
        mIcon.setImageResource(info.getIconId());
        mFileItemGroup.setVisibility(View.VISIBLE);
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openFile(mContext, info);
            }
        });
        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SidebarUtils.dragFile(v, mContext, new File(info.filePath), info.mimeType);
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
