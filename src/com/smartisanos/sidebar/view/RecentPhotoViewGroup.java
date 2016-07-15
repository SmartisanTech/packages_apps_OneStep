package com.smartisanos.sidebar.view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.AnimUtils;
import com.smartisanos.sidebar.view.ContentView.ContentType;

public class RecentPhotoViewGroup extends FrameLayout implements IEmpty {

    private ContentView mContentView;
    private View mContainer, mClearPhoto;
    private GridView mGridView;

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
        mClearPhoto = findViewById(R.id.clear);
        mGridView = (GridView)findViewById(R.id.recentphoto_gridview);
        mGridView.setAdapter(new RecentPhotoAdapter(mContext, this));
        mClearPhoto.setOnClickListener(new ClearListener(new Runnable(){
            @Override
            public void run() {
                mGridView.setLayoutAnimation(AnimUtils.getClearLayoutAnimationForListView());
                mGridView.startLayoutAnimation();
                startAnimation(AnimUtils.getClearAnimationForContainer(RecentPhotoViewGroup.this, RecentPhotoManager.getInstance(mContext)));
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
        setVisibility(View.VISIBLE);
        if (anim) {
            if (!mIsEmpty) {
                mGridView.setLayoutAnimation(AnimUtils.getEnterLayoutAnimationForListView());
                mGridView.startLayoutAnimation();
            }
            startAnimation(AnimUtils.getEnterAnimationForContainer());
        }
    }

    public void dismiss(boolean anim) {
        if (anim) {
            if (!mIsEmpty) {
                mGridView.setLayoutAnimation(AnimUtils.getExitLayoutAnimationForListView());
                mGridView.startLayoutAnimation();
            }
            startAnimation(AnimUtils.getExitAnimationForContainer(this));
        } else {
            setVisibility(View.INVISIBLE);
        }
    }
}
