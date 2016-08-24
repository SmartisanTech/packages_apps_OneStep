package com.smartisanos.sidebar.view;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.Vector3f;

public class ContentView extends RelativeLayout {
    private static final LOG log = LOG.getInstance(ContentView.class);

    public interface ISubView{
        void show(boolean anim);
        void dismiss(boolean anim);
    }

    public enum ContentType{
        NONE,
        PHOTO,
        FILE,
        CLIPBOARD,
    }

    private RecentPhotoViewGroup mRecentPhotoViewGroup;
    private RecentFileViewGroup mRecentFileViewGroup;
    private ClipboardViewGroup mClipboardViewGroup;

    private ContentType mCurType = ContentType.NONE;

    private Map<ContentType, ISubView> mMapTypeToView = new HashMap<ContentType, ISubView>();

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

    private static final int ANIMATION_DURA = 300;

    public void show(ContentType ct, boolean anim) {
        if (mCurType != ContentType.NONE || !mMapTypeToView.containsKey(ct)) {
            return;
        }
        mCurType = ct;
        mMapTypeToView.get(ct).show(anim);
        if (anim) {
            Anim alphaAnim = new Anim(this, Anim.TRANSPARENT, ANIMATION_DURA, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
            alphaAnim.start();
        } else {
            setAlpha(1.0f);
        }
    }

    public void dismiss(ContentType ct, boolean anim) {
        if (mCurType != ct || !mMapTypeToView.containsKey(ct)) {
            return;
        }
        mCurType = ContentType.NONE;
        mMapTypeToView.get(ct).dismiss(anim);
        if(anim){
            Anim alphaAnim = new Anim(this, Anim.TRANSPARENT, ANIMATION_DURA, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            alphaAnim.start();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecentPhotoViewGroup = (RecentPhotoViewGroup)findViewById(R.id.recent_photo_view_group);
        mRecentPhotoViewGroup.setContentView(this);
        mRecentFileViewGroup = (RecentFileViewGroup)findViewById(R.id.recent_file_view_group);
        mRecentFileViewGroup.setContentView(this);
        mClipboardViewGroup = (ClipboardViewGroup)findViewById(R.id.clipboard_view_group);
        mClipboardViewGroup.setContentView(this);

        mMapTypeToView.put(ContentType.PHOTO, mRecentPhotoViewGroup);
        mMapTypeToView.put(ContentType.FILE, mRecentFileViewGroup);
        mMapTypeToView.put(ContentType.CLIPBOARD, mClipboardViewGroup);
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
        }else{
            setVisibility(View.VISIBLE);
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