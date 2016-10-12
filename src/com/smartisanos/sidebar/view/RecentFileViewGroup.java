package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.Tracker;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class RecentFileViewGroup extends RoundCornerFrameLayout implements IEmpty, ContentView.ISubView {
    private static final LOG log = LOG.getInstance(RecentFileViewGroup.class);

    private ContentView mContentView;

    private EmptyView mEmptyView;
    private View mContainer;
    private ListView mRecentFileList;
    private TextView mTitle;
    private View mClearFile;
    private RecentFileAdapter mRecentFileAdapter;

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
        mRecentFileAdapter = new RecentFileAdapter(mContext, this);
        mRecentFileList.setAdapter(mRecentFileAdapter);

        mClearFile.setOnClickListener(mClearListener);

        updateUI();
    }

    private ClearListener mClearListener = new ClearListener(new Runnable() {
        @Override
        public void run() {
            AnimTimeLine timeLine = new AnimTimeLine();
            int width = mRecentFileList.getWidth();
            Anim moveAnim = new Anim(mRecentFileList, Anim.MOVE, 100, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(width, 0));
            Anim alphaAnim = new Anim(RecentFileViewGroup.this, Anim.TRANSPARENT, 200, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            timeLine.addAnim(moveAnim);
            timeLine.addAnim(alphaAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete(int type) {
                    mRecentFileList.setTranslationX(0);
                    RecentFileViewGroup.this.setAlpha(1);
                    RecentFileViewGroup.this.setVisibility(View.GONE);
                    RecentFileManager.getInstance(mContext).clear();
                    Tracker.onClick(Tracker.EVENT_MAKESURE_CLEAN, "1");
                }
            });
            timeLine.start();
            SidebarController.getInstance(mContext).resumeTopView();
            mContentView.setCurrent(ContentType.NONE);
        }
    }, R.string.title_confirm_delete_history_file);

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
        RecentFileManager.getInstance(mContext).startSearchFile();
        mRecentFileList.setSelectionAfterHeaderView();
        mRecentFileList.requestLayout();
        post(new Runnable() {
            @Override
            public void run() {
                RecentFileManager.getInstance(mContext).refresh();
            }
        });

        setVisibility(VISIBLE);
        if (anim) {
            int time = 180;
            AnimTimeLine timeLine = new AnimTimeLine();
            final View view;
            if (mIsEmpty) {
                view = mEmptyView;
            } else {
                view = mRecentFileList;
            }
            int height = view.getHeight();
            view.setPivotY(0);
            Anim moveAnim = new Anim(view, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(0, -height / 2), new Vector3f());
            moveAnim.setListener(new AnimListener() {
                @Override
                public void onStart() {
                }
                @Override
                public void onComplete(int type) {
                    view.setTranslationY(0);
                }
            });
            timeLine.addAnim(moveAnim);
            setPivotY(0);
            Anim scaleAnim = new Anim(this, Anim.SCALE, time, Anim.CUBIC_OUT, new Vector3f(0, 0.6f), new Vector3f(0, 1));
            timeLine.addAnim(scaleAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_FILE_LIST_ANIM, true);
                }

                @Override
                public void onComplete(int type) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_FILE_LIST_ANIM, false);
                    setScaleY(1);
                }
            });
            timeLine.start();
        }
        Tracker.onClick(Tracker.EVENT_TOPBAR, "1");
    }

    public void dismiss(boolean anim) {
        mClearListener.dismiss();
        if (anim) {
            AnimTimeLine timeLine = new AnimTimeLine();
            final View view;
            if (mIsEmpty) {
                view = mEmptyView;
            } else {
                view = mContainer;
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
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_FILE_LIST_ANIM, true);
                }

                @Override
                public void onComplete(int type) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_FILE_LIST_ANIM, false);
                    view.setScaleY(1);
                    view.setAlpha(1);
                    setVisibility(View.GONE);
                    mRecentFileAdapter.shrink();
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
        super.onConfigurationChanged(newConfig);
        updateUI();
        mClearListener.onConfigurationChanged(newConfig);
    }
}
