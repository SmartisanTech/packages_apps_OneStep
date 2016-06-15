package com.smartisanos.sidebar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.IClear;
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.Utils;

public class ContentView extends RelativeLayout {

    public enum ContentType{
        NONE,
        PHOTO,
        FILE,
        CLIPBOARD,
        ADDTOSIDEBAR
    }

    private ViewStub mViewStubAddToSidebar;

    private View mPhotoContainer, mFileContainer, mClipboardContainner;
    private RecentPhotoGridView mPhotos;
    private ListView mRecentFileList;
    private ListView mClipList;

    private View mClearPhoto, mClearFile, mClearClipboard;
    private ClipboardAdapter mClipboardAdapter;

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
                mPhotos.setLayoutAnimation(getEnterLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(getEnterAnimationForContainer(mPhotoContainer));
            } else {
                mPhotoContainer.setVisibility(View.VISIBLE);
            }
            break;
        case FILE:
            if (anim) {
                mRecentFileList.setLayoutAnimation(getEnterLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(getEnterAnimationForContainer(mFileContainer));
            } else {
                mFileContainer.setVisibility(View.VISIBLE);
            }
            break;
        case CLIPBOARD:
            if (anim) {
                mClipList.setLayoutAnimation(getEnterLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
                mClipboardContainner.startAnimation(getEnterAnimationForContainer(mClipboardContainner));
            } else {
                mClipboardContainner.setVisibility(View.VISIBLE);
            }
            break;
        case ADDTOSIDEBAR:
            if(mAddContainner == null){
                initAddToSidebar();
            }
            mAddContainner.setVisibility(View.VISIBLE);
            break;
        default:
            break;
        }
    }

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
                mPhotos.setLayoutAnimation(getExitLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(getExitAnimationForContainer(mPhotoContainer));
            } else {
                mPhotoContainer.setVisibility(View.INVISIBLE);
            }
            break;
        case FILE:
            if (anim) {
                mRecentFileList.setLayoutAnimation(getExitLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(getExitAnimationForContainer(mFileContainer));
            } else {
                mFileContainer.setVisibility(View.INVISIBLE);
            }
            break;
        case CLIPBOARD:
            if (anim) {
                mClipList.setLayoutAnimation(getExitLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
                mClipboardContainner.startAnimation(getExitAnimationForContainer(mClipboardContainner));
            } else {
                mClipboardContainner.setVisibility(View.INVISIBLE);
            }
            break;
        case ADDTOSIDEBAR:
            if(mAddContainner == null){
                initAddToSidebar();
            }
            mAddContainner.setVisibility(View.INVISIBLE);
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
        mClipboardContainner = findViewById(R.id.clipboard_container);
        mClearPhoto = mPhotoContainer.findViewById(R.id.clear);
        mClearFile = mFileContainer.findViewById(R.id.clear);
        mClearClipboard = mClipboardContainner.findViewById(R.id.clear);

        // content
        mPhotos = (RecentPhotoGridView)findViewById(R.id.recentphoto_gridview);

        mRecentFileList = (ListView)findViewById(R.id.recentfile_listview);
        mRecentFileList.setAdapter(new RecentFileAdapter(this.mContext));

        mClipList = (ListView)findViewById(R.id.clipboard_listview);
        mClipboardAdapter = new ClipboardAdapter(this.mContext);
        mClipList.setAdapter(mClipboardAdapter);

        mClearPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotos.setLayoutAnimation(getClearLayoutAnimationForListView());
                mPhotos.startLayoutAnimation();
                mPhotoContainer.startAnimation(getClearAnimationForContainer(mPhotoContainer, RecentPhotoManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mCurType = ContentType.NONE;
            }
        });

        mClearFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecentFileList.setLayoutAnimation(getClearLayoutAnimationForListView());
                mRecentFileList.startLayoutAnimation();
                mFileContainer.startAnimation(getClearAnimationForContainer(mFileContainer, RecentFileManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mCurType = ContentType.NONE;
            }
        });

        mClearClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClipList.setLayoutAnimation(getClearLayoutAnimationForListView());
                mClipList.startLayoutAnimation();
                mClipboardContainner.startAnimation(getClearAnimationForContainer(mClipboardContainner, RecentClipManager.getInstance(mContext)));
                SidebarController.getInstance(mContext).resumeTopView();
                mCurType = ContentType.NONE;
            }
        });
    }

    private static final int ANIMATION_DURA = 314;
    private LayoutAnimationController getEnterLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(ANIMATION_DURA);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0);
        return controller;
    }

    private LayoutAnimationController getExitLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        anim.setDuration(ANIMATION_DURA);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0);
        return controller;
    }

    private LayoutAnimationController getClearLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(ANIMATION_DURA / 2);
        anim.setFillAfter(true);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0.125f);
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        return controller;
    }

    private Animation getClearAnimationForContainer(View container, IClear clear){
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(ANIMATION_DURA);
        anim.setAnimationListener(new DismissAnimationListener(container, clear));
        return anim;
    }

    private Animation getEnterAnimationForContainer(View container){
        Animation scaleAnim = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
        scaleAnim.setDuration(ANIMATION_DURA);
        scaleAnim.setAnimationListener(new ShowAnimationListener(container));
        return scaleAnim;
    }

    private Animation getExitAnimationForContainer(View container){
        Animation scaleAnim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f);
        scaleAnim.setDuration(ANIMATION_DURA);
        scaleAnim.setAnimationListener(new DismissAnimationListener(container));
        return scaleAnim;
    }

    private static final class ShowAnimationListener implements Animation.AnimationListener{

        private View view;
        public ShowAnimationListener(View view){
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            view.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //NA
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //NA
        }
    }

    private final class DismissAnimationListener implements Animation.AnimationListener{

        private View view;
        private IClear clear;

        public DismissAnimationListener(View view){
            this.view = view;
        }

        public DismissAnimationListener(View view, IClear clear){
            this.view = view;
            this.clear = clear;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            //NA
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            view.setVisibility(View.INVISIBLE);
            if(clear != null){
                clear.clear();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //NA
        }
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
