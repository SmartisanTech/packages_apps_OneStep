package com.smartisanos.sidebar.view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.view.ContentView.ContentType;

public class RecentPhotoViewGroup extends RoundCornerFrameLayout implements IEmpty, ContentView.ISubView {
    private static final LOG log = LOG.getInstance(RecentPhotoViewGroup.class);

    private ContentView mContentView;
    private View mContainer;
    private TextView mTitle;
    private View mClear;
    private RecentPhotoAdapter mAdapter;
    private ListView mListView;

    private EmptyView mEmptyView;
    private boolean mIsEmpty = true;

    public RecentPhotoViewGroup(Context context) {
        this(context, null);
    }

    public RecentPhotoViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public RecentPhotoViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentPhotoViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEmptyView = (EmptyView)findViewById(R.id.empty_view);
        mEmptyView.setImageView(R.drawable.photo_blank);
        mEmptyView.setText(R.string.photo_empty_text);
        mEmptyView.setHint(R.string.photo_empty_hint);

        mEmptyView.setButton(R.string.open_gallery, R.drawable.photo_blank_button, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setPackage("com.android.gallery3d");
                    intent.putExtra("package_name", "com.smartisanos.sidebar");
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    Utils.dismissAllDialog(mContext);
                }catch (ActivityNotFoundException e) {
                    // NA
                }
            }
        });

        mContainer = findViewById(R.id.photo_container);
        mTitle = (TextView)findViewById(R.id.title);
        mClear = findViewById(R.id.clear);
        mListView = (ListView) findViewById(R.id.content_list);
        mAdapter = new RecentPhotoAdapter(mContext, this);
        mListView.setAdapter(mAdapter);
        mClear.setOnClickListener(mClearListener);
    }

    private ClearListener mClearListener = new ClearListener(new Runnable() {
        @Override
        public void run() {
            int width = mListView.getWidth();
            AnimTimeLine timeLine = new AnimTimeLine();
            Anim moveAnim = new Anim(mListView, Anim.MOVE, 100, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(width, 0));
            Anim alphaAnim = new Anim(RecentPhotoViewGroup.this, Anim.TRANSPARENT, 200, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            timeLine.addAnim(moveAnim);
            timeLine.addAnim(alphaAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete(int type) {
                    mListView.setTranslationX(0);
                    RecentPhotoViewGroup.this.setAlpha(1);
                    RecentPhotoViewGroup.this.setVisibility(View.GONE);
                    RecentPhotoManager.getInstance(mContext).clear();
                    mAdapter.clearCache();
                }
            });
            timeLine.start();
            SidebarController.getInstance(mContext).resumeTopView();
            mContentView.setCurrent(ContentType.NONE);
        }
    }, R.string.title_confirm_delete_history_photo);

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
        mListView.setSelectionAfterHeaderView();
        mListView.requestLayout();
        setVisibility(View.VISIBLE);
        if (anim) {
            int time = 200;
            AnimTimeLine timeLine = new AnimTimeLine();
            if (!mIsEmpty) {
//                timeLine.addTimeLine(mGridView.photoAnim(true));
            }
            setPivotX(0);
            setPivotY(0);
            Anim scaleAnim = new Anim(this, Anim.SCALE, time, Anim.CUBIC_OUT, new Vector3f(0, 0.6f), new Vector3f(0, 1));
            timeLine.addAnim(scaleAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_RECENT_PHOTO_LIST_ANIM, true);
                }

                @Override
                public void onComplete(int type) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_RECENT_PHOTO_LIST_ANIM, false);
                    setScaleY(1);
                }
            });
            timeLine.start();
        }
    }

    public void dismiss(boolean anim) {
        mAdapter.setPhotoExpandedFalse();
        mAdapter.notifyDataSetChanged();
        mClearListener.dismiss();
        if (anim) {
            int time = 200;
            AnimTimeLine timeLine = new AnimTimeLine();
            if (!mIsEmpty) {
//                timeLine.addTimeLine(mGridView.photoAnim(false));
            }
            setPivotX(0);
            setPivotY(0);
            Anim scaleAnim = new Anim(this, Anim.SCALE, time, Anim.CUBIC_OUT, new Vector3f(0, 1), new Vector3f(0, 0.6f));
            timeLine.addAnim(scaleAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_RECENT_PHOTO_LIST_ANIM, true);
                }

                @Override
                public void onComplete(int type) {
                    setVisibility(View.GONE);
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_RECENT_PHOTO_LIST_ANIM, false);
                    setScaleY(1);
                }
            });
            timeLine.start();
        } else {
            setVisibility(View.GONE);
        }
    }

    private void updateUI(){
        mTitle.setText(R.string.title_photo);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUI();
        mClearListener.onConfigurationChanged(newConfig);
    }
}
