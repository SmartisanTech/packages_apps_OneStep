package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IClear;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimUtils;

import smartisanos.util.SidebarUtils;

public class ContentView extends RelativeLayout {
    private static final LOG log = LOG.getInstance(ContentView.class);

    public enum ContentType{
        NONE,
        PHOTO,
        FILE,
        CLIPBOARD,
        ADDTOSIDEBAR
    }

    private ViewStub mViewStubAddToSidebar;

    private ClipboardViewGroup mClipboardContainner;

    private View mPhotoContainer, mFileContainer;
    private RecentPhotoGridView mPhotos;
    private ListView mRecentFileList;

    private View mClearPhoto, mClearFile;
    private Context mViewContext;

    private ContentType mCurType = ContentType.NONE;

    // add content related
    private View mAddContainner;

    public ContentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ContentView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ContentType getCurrentContent(){
        return mCurType;
    }

    public void setCurrent(ContentType ct){
        mCurType = ct;
    }

    public void show(ContentType ct, boolean anim) {
        if (mCurType != ContentType.NONE) {
            return;
        }
        setVisibility(View.VISIBLE);
        SidebarController.getInstance(mContext).addContentView();
        mCurType = ct;
        this.animate().alpha(1.0f).setDuration(ANIMATION_DURA).start();
        switch (ct) {
        case PHOTO:
            if (anim) {
                mPhotos.setLayoutAnimation(AnimUtils.getEnterLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(AnimUtils.getEnterAnimationForContainer(mPhotoContainer));
            } else {
                mPhotoContainer.setVisibility(View.VISIBLE);
            }
            break;
        case FILE:
            if (anim) {
                mRecentFileList.setLayoutAnimation(AnimUtils.getEnterLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(AnimUtils.getEnterAnimationForContainer(mFileContainer));
            } else {
                mFileContainer.setVisibility(View.VISIBLE);
            }
            break;
        case CLIPBOARD:
            mClipboardContainner.show(anim);
            break;
        case ADDTOSIDEBAR:
            if(mAddContainner == null){
                initAddToSidebar();
            }
            if(anim){
                mAddContainner.startAnimation(AnimUtils.getEnterAnimationForContainer(mAddContainner));
            }else{
                mAddContainner.setVisibility(View.VISIBLE);
            }
            break;
        default:
            break;
        }
    }

    private static final int ANIMATION_DURA = 314;

    public void dismiss(ContentType ct, boolean anim) {
        if (mCurType != ct) {
            return;
        }
        mCurType = ContentType.NONE;
        if(anim){
            this.animate().alpha(0.0f).setDuration(ANIMATION_DURA).start();
        }else{
            this.setAlpha(0.0f);
        }
        switch (ct) {
        case PHOTO:
            if (anim) {
                mPhotos.setLayoutAnimation(AnimUtils.getExitLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(AnimUtils.getExitAnimationForContainer(mPhotoContainer));
            } else {
                mPhotoContainer.setVisibility(View.INVISIBLE);
            }
            break;
        case FILE:
            if (anim) {
                mRecentFileList.setLayoutAnimation(AnimUtils.getExitLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(AnimUtils.getExitAnimationForContainer(mFileContainer));
            } else {
                mFileContainer.setVisibility(View.INVISIBLE);
            }
            break;
        case CLIPBOARD:
            mClipboardContainner.dismiss(anim);
            break;
        case ADDTOSIDEBAR:
            if(mAddContainner == null){
                initAddToSidebar();
            }
            if (anim) {
                mAddContainner.startAnimation(AnimUtils.getExitAnimationForContainer(mAddContainner));
            } else {
                mAddContainner.setVisibility(View.INVISIBLE);
            }
            break;
        case NONE:
            break;
        default:
            break;
        }
    }

    private void initAddToSidebar(){
        if(mAddContainner == null){
            mViewStubAddToSidebar.inflate();
            mAddContainner = findViewById(R.id.addtosidebar_container);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mViewStubAddToSidebar = (ViewStub)findViewById(R.id.viewstub_addtosidebar);

        // view container
        mPhotoContainer = findViewById(R.id.photo_container);
        mFileContainer = findViewById(R.id.file_container);
        mClipboardContainner = (ClipboardViewGroup)findViewById(R.id.clipboard_container);
        mClipboardContainner.setContentView(this);
        mClearPhoto = mPhotoContainer.findViewById(R.id.clear);
        mClearFile = mFileContainer.findViewById(R.id.clear);

        // content
        mPhotos = (RecentPhotoGridView)findViewById(R.id.recentphoto_gridview);

        mRecentFileList = (ListView)findViewById(R.id.recentfile_listview);
        mRecentFileList.setAdapter(new RecentFileAdapter(this.mContext));

        mClearPhoto.setOnClickListener(new ClearListener(new Runnable(){
            @Override
            public void run() {
                mPhotos.setLayoutAnimation(AnimUtils.getClearLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(AnimUtils.getClearAnimationForContainer(mPhotoContainer, RecentPhotoManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mCurType = ContentType.NONE;
            }
        }));

        mClearFile.setOnClickListener(new ClearListener(new Runnable() {
            @Override
            public void run() {
                mRecentFileList.setLayoutAnimation(AnimUtils.getClearLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(AnimUtils.getClearAnimationForContainer(mFileContainer, RecentFileManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mCurType = ContentType.NONE;
            }
        }));
    }

    @Override
    protected void onChildVisibilityChanged(View child, int oldVisibility,
            int newVisibility) {
        super.onChildVisibilityChanged(child, oldVisibility, newVisibility);
        if(newVisibility != View.VISIBLE){
            int count = getChildCount();
            for(int i = 0;i < count; ++ i){
                if(getChildAt(i).getVisibility() == View.VISIBLE){
                    // do nothing
                    return ;
                }
            }
            setVisibility(View.INVISIBLE);
            SidebarController.getInstance(mContext).resumeTopView();
            SidebarController.getInstance(mContext).removeContentView();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            Utils.resumeSidebar(mContext);
            return true;
        default:
            break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_BACK:
            boolean isUp = event.getAction() == KeyEvent.ACTION_UP;
            if (isUp && getCurrentContent() != ContentType.NONE) {
                Utils.resumeSidebar(mContext);
            }
            break;
        default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }
}