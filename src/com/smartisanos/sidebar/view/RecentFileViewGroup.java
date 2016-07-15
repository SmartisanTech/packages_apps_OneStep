package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.anim.AnimUtils;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

public class RecentFileViewGroup extends FrameLayout implements IEmpty {

    private ContentView mContentView;

    private EmptyView mEmptyView;
    private View mContainer;
    private ListView mRecentFileList;
    private View mClearFile;

    private boolean mIsEmpty = true;

    public RecentFileViewGroup(Context context) {
        this(context, null);
    }

    public RecentFileViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentFileViewGroup(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentFileViewGroup(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEmptyView = (EmptyView)findViewById(R.id.empty_view);
        mEmptyView.setImageView(R.drawable.file_blank);
        mEmptyView.setText(R.string.file_empty_text);
        mEmptyView.setHint(R.string.file_empty_hint);

        mContainer = findViewById(R.id.file_container);
        mClearFile = findViewById(R.id.clear);
        mRecentFileList = (ListView)findViewById(R.id.recentfile_listview);
        mRecentFileList.setAdapter(new RecentFileAdapter(mContext, this));

        mClearFile.setOnClickListener(new ClearListener(new Runnable() {
            @Override
            public void run() {
                mRecentFileList.setLayoutAnimation(AnimUtils.getClearLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                startAnimation(AnimUtils.getClearAnimationForContainer(RecentFileViewGroup.this, RecentFileManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mContentView.setCurrent(ContentType.NONE);
            }
        }));
    }

    public void setContentView(ContentView cv){
        mContentView = cv;
    }

    @Override
    public void setEmpty(boolean isEmpty) {
        if (mIsEmpty != isEmpty) {
            mIsEmpty = isEmpty;
            if (mIsEmpty) {
                mContainer.setVisibility(GONE);
                mEmptyView.setVisibility(VISIBLE);
            } else {
                mContainer.setVisibility(VISIBLE);
                mEmptyView.setVisibility(GONE);
            }
        }
    }

    public void show(boolean anim) {
        setVisibility(VISIBLE);
        if (anim) {
            if (!mIsEmpty) {
                mRecentFileList.setLayoutAnimation(AnimUtils.getEnterLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
            }
            startAnimation(AnimUtils.getEnterAnimationForContainer());
        }
    }

    public void dismiss(boolean anim) {
        if (anim) {
            if (mIsEmpty) {
                mRecentFileList.setLayoutAnimation(AnimUtils.getExitLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
            }
            startAnimation(AnimUtils.getExitAnimationForContainer(this));
        } else {
            setVisibility(View.INVISIBLE);
        }
    }
}
