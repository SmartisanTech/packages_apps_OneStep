package com.smartisanos.sidebar;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.internal.sidebar.ISidebar;
import com.android.internal.sidebar.ISidebarService;
import com.smartisanos.sidebar.view.ContentView;
import com.smartisanos.sidebar.view.ContentView.ContentType;
import com.smartisanos.sidebar.view.SideView;
import com.smartisanos.sidebar.view.TopView;

public class SidebarController {
    private volatile static SidebarController sInstance;

    private Context mContext;
    private Handler mHandler;
    private WindowManager mWindowManager;

    private SideView mSideView;
    private TopView mTopView;
    private ContentView mContentView;

    private boolean mContentViewAdded = false;

    private int mSidbarMode = SidebarMode.MODE_LEFT;

    public static SidebarController getInstance(Context context){
        if(sInstance == null){
            synchronized(SidebarController.class){
                if(sInstance == null){
                    sInstance = new SidebarController(context);
                }
            }
        }
        return sInstance;
    }

    private SidebarController(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    public void init(){
        inflateView();
        ISidebarService sidebarService = ISidebarService.Stub.asInterface(ServiceManager.getService(Context.SIDEBAR_SERVICE));
        if (sidebarService != null) {
            try {
                sidebarService.registerSidebar(mBinder);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onSidebarModeChanged(){
        if(mSideView != null){
            mSideView.onSidebarModeChanged();
        }
    }

    public void setSidebarMode(int mode){
        if(mSidbarMode != mode){
            mSidbarMode = mode;
            onSidebarModeChanged();
        }
    }

    public int getSidebarMode(){
        return mSidbarMode;
    }

    public void start(){
        addTopView();
        addSideView();
    }

    public void stop(){
        mWindowManager.removeView(mTopView);
        mWindowManager.removeView(mSideView);
        dismissContent();
    }

    private void inflateView() {
        if(mSideView == null){
            mSideView = (SideView) View.inflate(mContext,R.layout.sidebar_view, null);
        }

        if(mTopView == null){
            mTopView = (TopView) View.inflate(mContext, R.layout.topbar_view, null);
            mTopView.setController(this);
        }

        if(mContentView == null){
            mContentView =(ContentView) View.inflate(mContext, R.layout.content_view, null);
        }
    }

    private void addSideView() {
        if (mSideView != null) {
            int sideViewWidth = mContext.getResources().getDimensionPixelSize(R.dimen.sidebar_width);
            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    sideViewWidth,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SIDEBAR_TOOLS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT);
            if(getSidebarMode() == SidebarMode.MODE_LEFT){
                lp.gravity = Gravity.LEFT | Gravity.FILL_VERTICAL;
            }else{
                lp.gravity = Gravity.RIGHT | Gravity.FILL_VERTICAL;
            }
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            lp.setTitle("sidebar_sideview");
            lp.packageName = mContext.getPackageName();
            mWindowManager.addView(mSideView, lp);
        }
    }

    private void addTopView() {
        if (mTopView != null) {
            int topViewWidth = mContext.getResources().getDimensionPixelSize(R.dimen.topbar_width);
            int topViewHeight = mContext.getResources().getDimensionPixelSize(R.dimen.topbar_height);
            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    topViewWidth,
                    topViewHeight,
                    WindowManager.LayoutParams.TYPE_SIDEBAR_TOOLS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT);
            if (getSidebarMode() == SidebarMode.MODE_LEFT) {
                lp.gravity = Gravity.TOP | Gravity.RIGHT;
            } else {
                lp.gravity = Gravity.TOP | Gravity.LEFT;
            }
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            lp.setTitle("sidebar_topview");
            lp.packageName = mContext.getPackageName();
            mWindowManager.addView(mTopView, lp);
        }
    }

    public void addContentView() {
        if (mContentView != null && !mContentViewAdded) {
            mContentViewAdded = true;

            int contentViewWidth = mContext.getResources().getDimensionPixelSize(R.dimen.content_width);
            int contentViewHeight = mContext.getResources().getDimensionPixelSize(R.dimen.content_height);
            final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    contentViewWidth,
                    contentViewHeight,
                    WindowManager.LayoutParams.TYPE_SIDEBAR_TOOLS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT);
            if (getSidebarMode() == SidebarMode.MODE_LEFT) {
                lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            } else {
                lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
            }
            lp.setTitle("sidebar_contentview");
            lp.packageName = mContext.getPackageName();
            mWindowManager.addView(mContentView, lp);
        }
    }

    public void removeContentView() {
        if (mContentViewAdded) {
            mWindowManager.removeView(mContentView);
            mContentViewAdded = false;
        }
    }

    public ContentType getCurrentContentType(){
        return mContentView.getCurrentContent();
    }

    public void showContent(ContentType ct) {
        mContentView.show(ct, true);
    }

    public void dismissContent() {
        mContentView.dismiss(mContentView.getCurrentContent(), true);
    }

    public void resumeTopView(){
        mTopView.resumeToNormal();
    }

    public void disableSideView(){
        mSideView.disable();
    }

    public void resumeSideView(){
        mSideView.resume();
    }

    private final ISidebar.Stub mBinder = new ISidebar.Stub() {
        @Override
        public void onEnterSidebarModeStart(int sidebarMode) throws RemoteException {
            setSidebarMode(sidebarMode);
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    start();
                }
            });
        }

        @Override
        public void onEnterSidebarModeEnd() throws RemoteException {
            //TODO
        }

        @Override
        public void onExitSidebarModeStart() throws RemoteException {
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    stop();
                }
            });
        }

        @Override
        public void onExitSidebarModeEnd() throws RemoteException {
            //TODO
        }
    };
}
