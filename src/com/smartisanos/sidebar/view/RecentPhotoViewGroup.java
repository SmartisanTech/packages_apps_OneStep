package com.smartisanos.sidebar.view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.AnimUtils;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import java.util.ArrayList;
import java.util.List;

public class RecentPhotoViewGroup extends FrameLayout implements IEmpty, ContentView.ISubView {
    private static final LOG log = LOG.getInstance(RecentPhotoViewGroup.class);

    private ContentView mContentView;
    private View mContainer;
    private TextView mTitle;
    private View mClear;
    private GridView mGridView;
    private RecentPhotoAdapter mAdapter;

    private EmptyView mEmptyView;
    private boolean mIsEmpty = true;

    public RecentPhotoViewGroup(Context context) {
        this(context, null);
    }

    public RecentPhotoViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public RecentPhotoViewGroup(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentPhotoViewGroup(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
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
        mGridView = (GridView)findViewById(R.id.recentphoto_gridview);
        mGridView.setAdapter(mAdapter = new RecentPhotoAdapter(mContext, this));
        mClear.setOnClickListener(mClearListener);
    }

    private ClearListener mClearListener = new ClearListener(new Runnable() {
        @Override
        public void run() {
            mGridView.setLayoutAnimation(AnimUtils.getClearLayoutAnimationForListView());
            mGridView.startLayoutAnimation();
            startAnimation(AnimUtils.getClearAnimationForContainer(RecentPhotoViewGroup.this, RecentPhotoManager.getInstance(mContext)));
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
        setVisibility(View.VISIBLE);
        if (anim) {
            if (!mIsEmpty) {
                imageAnim(true);
            }
            startAnimation(AnimUtils.getEnterAnimationForContainer());
        }
    }

    public void dismiss(boolean anim) {
        mClearListener.dismiss();
        if (anim) {
            if (!mIsEmpty) {
                imageAnim(false);
            }
            startAnimation(AnimUtils.getExitAnimationForContainer(this));
        } else {
            setVisibility(View.GONE);
        }
    }

    private AnimTimeLine mImageAnimTimeLine = null;

    private void imageAnim(final boolean isShow) {
        int count = mGridView.getChildCount();
        Vector3f loc00 = new Vector3f();
        Vector3f alphaFrom = new Vector3f();
        Vector3f alphaTo = new Vector3f();
        final int time = 200;
        int easeInOut = Anim.CUBIC_OUT;
        final List<Anim> anims = new ArrayList<Anim>();
        for (int i = 0; i < count; i++) {
            final View view = mGridView.getChildAt(i);
            if (view == null) {
                continue;
            }
            int x = view.getLeft();
            int y = view.getTop();
            if (i == 0) {
                loc00.x = x;
                loc00.y = y;
                continue;
            }
            Vector3f from = new Vector3f();
            Vector3f to = new Vector3f();
            int moveDelay = 0;
            int alphaDelay = 0;
            if (isShow) {
                view.setTranslationX(loc00.x);
                view.setTranslationX(loc00.y);
                view.setAlpha(0);
                from.x = loc00.x;
                from.y = loc00.y;
                to.x = x;
                to.y = y;
                alphaFrom.z = 0;
                alphaTo.z = 1;
                moveDelay = i * 5;
                alphaDelay = moveDelay + 20;
            } else {
                from.x = x;
                from.y = y;
                to.x = loc00.x;
                to.y = loc00.y;
                alphaFrom.z = 1;
                alphaTo.z = 0;
            }
            log.error("imageAnim show ["+isShow+"] from "+ from + ", to " + to);
            Anim moveAnim = new Anim(view, Anim.TRANSLATE, time, easeInOut, from, to);
            Anim alphaAnim = new Anim(view, Anim.TRANSPARENT, time, easeInOut, alphaFrom, alphaTo);
            moveAnim.setDelay(moveDelay);
            alphaAnim.setDelay(alphaDelay);
            anims.add(moveAnim);
            anims.add(alphaAnim);
        }
        if (mImageAnimTimeLine != null) {
            mImageAnimTimeLine.cancel();
        }
        mImageAnimTimeLine = new AnimTimeLine();
        for (Anim anim : anims) {
            mImageAnimTimeLine.addAnim(anim);
        }
        mImageAnimTimeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(int type) {
                if (type == Anim.ANIM_FINISH_TYPE_CANCELED) {
                    for (Anim anim : anims) {
                        if (anim == null) {
                            continue;
                        }
                        if (anim.getAnimType() == Anim.TRANSLATE) {
                            Vector3f loc = null;
                            if (isShow) {
                                loc = anim.getTo();
                            } else {
                                loc = anim.getFrom();
                            }
                            View view = anim.getView();
                            if (view != null) {
                                view.setX(loc.x);
                                view.setY(loc.y);
                            }
                        }
                    }
                }
            }
        });
        mImageAnimTimeLine.start();
    }

    private void updateUI(){
        mTitle.setText(R.string.title_photo);
        mAdapter.updateUI();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        updateUI();
    }
}
