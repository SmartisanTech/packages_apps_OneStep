package com.smartisanos.sidebar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.internal.sidebar.ISidebar;
import com.android.internal.sidebar.ISidebarService;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.view.ContentView;
import com.smartisanos.sidebar.view.ContentView.ContentType;
import com.smartisanos.sidebar.view.SideView;
import com.smartisanos.sidebar.view.SidebarRootView;
import com.smartisanos.sidebar.view.TopView;

public class SidebarController {
    private static final LOG log = LOG.getInstance(SidebarController.class);

    private volatile static SidebarController sInstance;

    private Context mContext;
    private Handler mHandler;
    private WindowManager mWindowManager;

    private SidebarRootView mSidebarRoot;
    private SideView mSideView;
    private TopView mTopView;
    private ContentView mContentView;

    private int mSidbarMode = SidebarMode.MODE_LEFT;

    private float mRate = 1.0f;
    private int mScreenWidth, mScreenHeight;
    private int mSideViewWidth;
    private int mTopViewWidth, mTopViewHeight;
    private int mContentViewWidth, mContentViewHeight;

    public int getContentViewWidth() {
        return mContentViewWidth;
    }

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
        Point pt = new Point();
        mWindowManager.getDefaultDisplay().getSize(pt);
        mScreenWidth = pt.x;
        mScreenHeight = pt.y;
        mSideViewWidth = mContext.getResources().getDimensionPixelSize(R.dimen.sidebar_width);
        mRate = 1.0f - mSideViewWidth * 1.0f / mScreenWidth;
        mTopViewWidth = mScreenWidth - mSideViewWidth;
        mTopViewHeight = (int) (mScreenHeight * (1.0f - mRate));
        mContentViewWidth = mScreenWidth - mSideViewWidth;
        mContentViewHeight = mScreenHeight - mTopViewHeight;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mBroadcastReceiver, filter);
    }

    public void init() {
        AddWindows();
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

    private void start(){
        updateTopViewWindowBySidebarMode();
        updateSideViewWindowBySidebarMode();
        updateContentViewWindowBySidebarMode();
        mTopView.show(true);
        mSidebarRoot.show(true);
    }

    private void stop(){
        mTopView.show(false);
        mSidebarRoot.show(false);
        dismissContent(false);
    }

    private void AddWindows() {
        addTopViewWindow();
        addSideViewWindow();
        addContentViewWindow();
    }

    public SidebarRootView getSidebarRootView() {
        return mSidebarRoot;
    }

    public SideView getSideView() {
        return mSideView;
    }

    private void addSideViewWindow() {
        mSidebarRoot = (SidebarRootView) View.inflate(mContext,
                R.layout.sidebar_view, null);
        mSidebarRoot.setTrashView();
        mSideView = (SideView) mSidebarRoot.findViewById(R.id.sidebar);
        mSideView.setRootView(mSidebarRoot);
        mSidebarRoot.setSideView(mSideView);

        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SIDEBAR_TOOLS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.setTitle("sidebar_sideview");
        lp.privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_NO_MOVE_ANIMATION;
        lp.packageName = mContext.getPackageName();
        mSidebarRoot.setVisibility(View.GONE);
        mWindowManager.addView(mSidebarRoot, lp);
    }

    private void updateSideViewWindowBySidebarMode(){
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams)mSidebarRoot.getLayoutParams();
        if (getSidebarMode() == SidebarMode.MODE_LEFT) {
            lp.gravity = Gravity.LEFT | Gravity.FILL_VERTICAL;
            FrameLayout.LayoutParams llp = (FrameLayout.LayoutParams) mSideView
                    .getLayoutParams();
            llp.gravity = Gravity.LEFT | Gravity.FILL_VERTICAL;
        } else {
            // lp.windowAnimations = R.style.Animation_SidebarWindowRightAnim;
            lp.gravity = Gravity.RIGHT | Gravity.FILL_VERTICAL;
            FrameLayout.LayoutParams llp = (FrameLayout.LayoutParams) mSideView
                    .getLayoutParams();
            llp.gravity = Gravity.RIGHT | Gravity.FILL_VERTICAL;
        }
        mWindowManager.updateViewLayout(mSidebarRoot, lp);
    }

    public void updateDragWindow(boolean toFullScreen) {
        if (mSidebarRoot == null) {
            return;
        }
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams)mSidebarRoot.getLayoutParams();
        if (toFullScreen) {
            if (mSidebarRoot.getTrash() != null) {
                mSidebarRoot.getTrash().initTrashView();
            }
            lp.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            lp.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
        } else {
            if (mSidebarRoot.getTrash() != null) {
                mSidebarRoot.getTrash().hideTrashView();
            }
            lp.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            lp.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        mWindowManager.updateViewLayout(mSidebarRoot, lp);
    }

    private void addTopViewWindow() {
        mTopView = (TopView) View.inflate(mContext, R.layout.topbar_view, null);
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                mTopViewWidth, mTopViewHeight,
                WindowManager.LayoutParams.TYPE_SIDEBAR_TOOLS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.setTitle("sidebar_topview");
        lp.packageName = mContext.getPackageName();
        mTopView.setVisibility(View.GONE);
        mWindowManager.addView(mTopView, lp);
    }

    private void updateTopViewWindowBySidebarMode(){
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mTopView.getLayoutParams();
        if (getSidebarMode() == SidebarMode.MODE_LEFT) {
            lp.gravity = Gravity.TOP | Gravity.RIGHT;
        } else {
            lp.gravity = Gravity.TOP | Gravity.LEFT;
        }
        mWindowManager.updateViewLayout(mTopView, lp);
    }

    public void addContentViewWindow() {
        mContentView = (ContentView) View.inflate(mContext,
                R.layout.content_view, null);
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                mContentViewWidth, mContentViewHeight,
                WindowManager.LayoutParams.TYPE_SIDEBAR_TOOLS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        lp.setTitle("sidebar_contentview");
        lp.packageName = mContext.getPackageName();
        lp.isEatHomeKey = true;
        mContentView.setVisibility(View.GONE);
        mWindowManager.addView(mContentView, lp);
    }

    private void updateContentViewWindowBySidebarMode() {
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mContentView.getLayoutParams();
        if (getSidebarMode() == SidebarMode.MODE_LEFT) {
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        } else {
            lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        }
        mWindowManager.updateViewLayout(mContentView, lp);
    }

    public ContentType getCurrentContentType(){
        return mContentView.getCurrentContent();
    }

    public void showContent(ContentType ct) {
        mContentView.show(ct, true);
    }

    public void dismissContent(boolean anim) {
        mContentView.dismiss(mContentView.getCurrentContent(), anim);
    }

    public void dimTopView(){
        mTopView.dimAll();
    }

    public void resumeTopView(){
        mTopView.resumeToNormal();
    }

    private final ISidebar.Stub mBinder = new ISidebar.Stub() {
        @Override
        public void onEnterSidebarModeStart(final int sidebarMode) throws RemoteException {
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    setSidebarMode(sidebarMode);
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

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra("reason");
                if ("eathomekey".equals(reason)) {
                    Utils.resumeSidebar(context);
                }
            }
        }
    };
}
