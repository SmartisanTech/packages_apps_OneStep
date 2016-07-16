package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimInterpolator;
import com.smartisanos.sidebar.util.anim.AnimUtils;
import com.smartisanos.sidebar.util.anim.Vector3f;

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

    private RecentPhotoViewGroup mRecentPhotoViewGroup;
    private RecentFileViewGroup mRecentFileViewGroup;
    private ClipboardViewGroup mClipboardViewGroup;

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
        mCurType = ct;
        setVisibility(View.VISIBLE);
        Anim alphaAnim = new Anim(this, Anim.TRANSPARENT, ANIMATION_DURA, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
        alphaAnim.start();
        switch (ct) {
        case PHOTO:
            mRecentPhotoViewGroup.show(anim);
            break;
        case FILE:
            mRecentFileViewGroup.show(anim);
            break;
        case CLIPBOARD:
            mClipboardViewGroup.show(anim);
            break;
        case ADDTOSIDEBAR:
            if(mAddContainner == null){
                initAddToSidebar();
            }
            mAddContainner.setVisibility(View.VISIBLE);
            if (anim) {
                boolean isLeft = SidebarController.getInstance(mViewContext).getSidebarMode() == SidebarMode.MODE_LEFT;
                ValueAnimator animator = new ValueAnimator();
                animator.setFloatValues(0f, 1f);
                animator.setDuration(300);
                if (isLeft) {
                    mAddContainner.setPivotX(0);
                } else {
                    int width = SidebarController.getInstance(mViewContext).getContentViewWidth();
                    mAddContainner.setPivotX(width);
                    log.error("mAddContainner show setPivotX ["+width+"]");
                }
                mAddContainner.setPivotY(0);
                animator.setInterpolator(new AnimInterpolator.Interpolator(Anim.CUBIC_OUT));
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float percent = (Float) valueAnimator.getAnimatedValue();
                        float scaleX = 0.6f + (0.4f * percent);
                        float scaleY = 0.6f + (0.4f * percent);
                        mAddContainner.setScaleX(scaleX);
                        mAddContainner.setScaleY(scaleY);
                        mAddContainner.setAlpha(percent);
                        mAddContainner.setX(0);
                        if (percent == 1.0f) {
                            mAddContainner.setScaleX(1);
                            mAddContainner.setScaleY(1);
                            mAddContainner.setAlpha(1);
                            valueAnimator.cancel();
                        }
                    }
                });
                animator.start();
            }
            break;
        default:
            break;
        }
    }

    private static final int ANIMATION_DURA = 300;

    public void dismiss(ContentType ct, boolean anim) {
        if (mCurType != ct) {
            return;
        }
        mCurType = ContentType.NONE;
        if(anim){
            Anim alphaAnim = new Anim(this, Anim.TRANSPARENT, ANIMATION_DURA, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            alphaAnim.start();
        }else{
            this.setAlpha(0.0f);
        }
        switch (ct) {
        case PHOTO:
            mRecentPhotoViewGroup.dismiss(anim);
            break;
        case FILE:
            mRecentFileViewGroup.dismiss(anim);
            break;
        case CLIPBOARD:
            mClipboardViewGroup.dismiss(anim);
            break;
        case ADDTOSIDEBAR:
            if(mAddContainner == null){
                initAddToSidebar();
            }
            if (anim) {
                boolean isLeft = SidebarController.getInstance(mViewContext).getSidebarMode() == SidebarMode.MODE_LEFT;
                if (SidebarController.getInstance(mViewContext).getSideView() != null) {
                    SidebarController.getInstance(mViewContext).getSideView().clickAddButtonAnim(isLeft, false, null);
                }
                mAddContainner.startAnimation(AnimUtils.getExitAnimationForContainer(mAddContainner));
                ValueAnimator animator = new ValueAnimator();
                animator.setFloatValues(0f, 1f);
                animator.setDuration(300);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mAddContainner.setVisibility(View.INVISIBLE);
                        mAddContainner.setAlpha(1);
                    }
                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }
                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
                if (isLeft) {
                    mAddContainner.setPivotX(0);
                } else {
                    int width = mAddContainner.getWidth();
                    mAddContainner.setPivotX(width);
                }

                mAddContainner.setPivotY(0);
                animator.setInterpolator(new AnimInterpolator.Interpolator(Anim.CUBIC_OUT));
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float percent = (Float) valueAnimator.getAnimatedValue();
                        float scaleX = 1 - (0.4f * percent);
                        float scaleY = 1 - (0.4f * percent);
                        mAddContainner.setScaleX(scaleX);
                        mAddContainner.setScaleY(scaleY);
                        mAddContainner.setAlpha(1 - percent);
                        mAddContainner.setX(0);
                        if (percent == 1.0f) {
                            mAddContainner.setScaleX(1);
                            mAddContainner.setScaleY(1);
                            valueAnimator.cancel();
                        }
                    }
                });
                animator.start();
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

        mRecentPhotoViewGroup = (RecentPhotoViewGroup)findViewById(R.id.recent_photo_view_group);
        mRecentPhotoViewGroup.setContentView(this);
        mRecentFileViewGroup = (RecentFileViewGroup)findViewById(R.id.recent_file_view_group);
        mRecentFileViewGroup.setContentView(this);
        mClipboardViewGroup = (ClipboardViewGroup)findViewById(R.id.clipboard_view_group);
        mClipboardViewGroup.setContentView(this);
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
            setVisibility(View.GONE);
            SidebarController.getInstance(mContext).resumeTopView();
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