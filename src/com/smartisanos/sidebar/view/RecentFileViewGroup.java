package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.AnimUtils;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class RecentFileViewGroup extends RoundCornerFrameLayout implements IEmpty, ContentView.ISubView {

    private ContentView mContentView;

    private EmptyView mEmptyView;
    private View mContainer;
    private ListView mRecentFileList;
    private TextView mTitle;
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
        mTitle = (TextView) findViewById(R.id.title);
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
        }, R.string.title_confirm_delete_history_file));
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
            AnimTimeLine timeLine = new AnimTimeLine();
            final View view;
            if (mIsEmpty) {
                view = mEmptyView;
            } else {
                view = mRecentFileList;
            }
            int time = 200;
            view.setPivotY(0);
            Anim alphaAnim = new Anim(view, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            Anim scaleAnim = new Anim(view, Anim.SCALE, time, Anim.CUBIC_OUT, new Vector3f(1, 1), new Vector3f(1, 0.6f));
            timeLine.addAnim(alphaAnim);
            timeLine.addAnim(scaleAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onComplete(int type) {
                    view.setScaleY(1);
                    view.setAlpha(1);
                    setVisibility(View.GONE);
                }
            });
            timeLine.start();
        } else {
            setVisibility(View.GONE);
        }
    }

    private void updateUI(){
        mTitle.setText(R.string.title_file);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        updateUI();
    }
}
