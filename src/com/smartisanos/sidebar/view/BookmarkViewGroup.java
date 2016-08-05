package com.smartisanos.sidebar.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.BookmarkManager;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BookmarkViewGroup extends LinearLayout implements IEmpty, ContentView.ISubView  {
    private static final LOG log = LOG.getInstance(BookmarkViewGroup.class);

    private Context mContext;

    public BookmarkViewGroup(Context context) {
        this(context, null);
    }

    public BookmarkViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BookmarkViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BookmarkViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    private ContentView mContentView;

    private EmptyView mEmptyView;
    private View mContainer;
    private ListView mRecentBookmarkList;
    private TextView mTitle;
    private View mClearList;
    private BookmarkAdapter mBookmarkAdapter;

    private boolean mIsEmpty = true;

    public void setContentView(ContentView cv){
        mContentView = cv;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mEmptyView = (EmptyView)findViewById(R.id.empty_view);
        mEmptyView.setImageView(R.drawable.bookmark_blank);
        mEmptyView.setText(R.string.bookmark_empty_text);
        mEmptyView.setHint(R.string.bookmark_empty_hint);

        mContainer = findViewById(R.id.bookmark_container);
        mTitle = (TextView) findViewById(R.id.title);
        mClearList = findViewById(R.id.clear);
        mRecentBookmarkList = (ListView)findViewById(R.id.recentbookmark_listview);
        mBookmarkAdapter = new BookmarkAdapter(mContext, this);
        mRecentBookmarkList.setAdapter(mBookmarkAdapter);
        mRecentBookmarkList.setOnItemClickListener(mBookmarkItemClicked);

        mClearList.setOnClickListener(mClearListener);
    }

    private ClearListener mClearListener = new ClearListener(new Runnable() {
        @Override
        public void run() {
            AnimTimeLine timeLine = new AnimTimeLine();
            int width = mRecentBookmarkList.getWidth();
            Anim moveAnim = new Anim(mRecentBookmarkList, Anim.MOVE, 100, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(width, 0));
            Anim alphaAnim = new Anim(BookmarkViewGroup.this, Anim.TRANSPARENT, 200, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            timeLine.addAnim(moveAnim);
            timeLine.addAnim(alphaAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete(int type) {
                    mRecentBookmarkList.setTranslationX(0);
                    BookmarkViewGroup.this.setAlpha(1);
                    BookmarkViewGroup.this.setVisibility(View.GONE);
                    BookmarkManager.getInstance(mContext).clear();
                }
            });
            timeLine.start();
            SidebarController.getInstance(mContext).resumeTopView();
            mContentView.setCurrent(ContentView.ContentType.NONE);
        }
    }, R.string.title_confirm_delete_bookmark);

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

    @Override
    public void show(boolean anim) {
        setVisibility(VISIBLE);
        if (anim) {
            int time = 200;
            AnimTimeLine timeLine = new AnimTimeLine();
            if (mIsEmpty) {
                int height = mEmptyView.getHeight();
                mEmptyView.setPivotY(0);
                Anim moveAnim = new Anim(mEmptyView, Anim.TRANSLATE, time, Anim.CUBIC_OUT, new Vector3f(0, -height), new Vector3f());
                moveAnim.setListener(new AnimListener() {
                    @Override
                    public void onStart() {
                    }
                    @Override
                    public void onComplete(int type) {
                        mEmptyView.setY(0);
                    }
                });
                timeLine.addAnim(moveAnim);
            } else {
                int height = mRecentBookmarkList.getHeight();
                mRecentBookmarkList.setPivotY(0);
                Anim moveAnim = new Anim(mRecentBookmarkList, Anim.TRANSLATE, time, Anim.CUBIC_OUT, new Vector3f(0, -height), new Vector3f());
                timeLine.addAnim(moveAnim);
            }
            setPivotY(0);
            Anim scaleAnim = new Anim(this, Anim.SCALE, time, Anim.CUBIC_OUT, new Vector3f(0, 0.6f), new Vector3f(0, 1));
            timeLine.addAnim(scaleAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_BOOKMARK_ANIM, true);
                }

                @Override
                public void onComplete(int type) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_BOOKMARK_ANIM, false);
                    mRecentBookmarkList.setY(0);
                    setScaleY(1);
                }
            });
            timeLine.start();
        }
    }

    @Override
    public void dismiss(boolean anim) {
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
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_BOOKMARK_ANIM, true);
                }

                @Override
                public void onComplete(int type) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_BOOKMARK_ANIM, false);
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

    public AdapterView.OnItemClickListener mBookmarkItemClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (position >= 0 && mBookmarkAdapter != null) {
                int count = mBookmarkAdapter.getCount();
                if (count > position) {
                    BookmarkManager.BookmarkItem item = mBookmarkAdapter.getItem(position);
                    if (item != null && item.content_uri != null) {
                        try {
                            Uri uri = Uri.parse(item.content_uri);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(uri);
                            mContext.startActivity(intent);

                            Utils.resumeSidebar(mContext);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    private void updateUI() {
        mTitle.setText(R.string.title_bookmark);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUI();
        mClearListener.onConfigurationChanged(newConfig);
    }
}