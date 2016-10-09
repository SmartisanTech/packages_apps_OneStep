package com.smartisanos.sidebar;

import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import com.smartisanos.sidebar.util.AppManager;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.OngoingManager;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.ResolveInfoManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
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
    private SidebarStatus mStatus = SidebarStatus.NORMAL;

    private float mRate = 1.0f;
    private int mScreenWidth, mScreenHeight;
    private int mSideViewWidth;
    private int mTopViewWidth, mTopViewHeight;
    private int mContentViewWidth, mContentViewHeight;

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
        context = context.getApplicationContext();
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

        AnimStatusManager.getInstance().addAnimFlagStatusChangedListener(
                AnimStatusManager.ENTER_ANIM_FLAG, new AnimStatusManager.AnimFlagStatusChangedListener() {
                    @Override
                    public void onChanged() {
                        if (!AnimStatusManager.getInstance().isEnterAnimOngoing()) {
                            onEnterAnimComplete();
                        }
                    }
                });

        IntentFilter closeSystemDialogFilter = new IntentFilter();
        closeSystemDialogFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mContext.registerReceiver(mBroadcastReceiver, closeSystemDialogFilter);

        // register receiver
        IntentFilter iconChangeFilter = new IntentFilter();
        iconChangeFilter.addAction(ACTION_UPDATE_ICON);
        mContext.registerReceiver(mIconChangeReceiver, iconChangeFilter);
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

    public void requestStatus(SidebarStatus status) {
        if (mStatus == status) {
            return;
        }
        mStatus = status;
        mTopView.requestStatus(mStatus);
        mSidebarRoot.requestStatus(mStatus);
    }

    public SidebarStatus getSidebarStatus() {
        return mStatus;
    }

    private void start(){
        updateTopViewWindowBySidebarMode();
        updateContentViewWindowBySidebarMode();
        updateSideViewWindowBySidebarMode();

        mTopView.show(true);
        mSidebarRoot.show(true);
    }

    public void onEnterAnimComplete() {
        RecentPhotoManager.getInstance(mContext).startObserver();
        RecentFileManager.getInstance(mContext).startFileObserver();
    }

    private void stop(){
        AnimStatusManager.getInstance().reset();
        mTopView.show(false);
        mSidebarRoot.show(false);
        dismissContent(false);
        RecentPhotoManager.getInstance(mContext).stopObserver();
        RecentFileManager.getInstance(mContext).stopFileObserver();
    }

    public void setEnabled(boolean enabled) {
        mSidebarRoot.setEnabled(enabled);
        mTopView.setEnabled(enabled);
    }

    private void AddWindows() {
        addTopViewWindow();
        addContentViewWindow();
        addSideViewWindow();
    }

    public TopView getSidebarTopView() {
        return mTopView;
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
        mSideView = (SideView) mSidebarRoot.findViewById(R.id.sidebar);

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
            if (mSidebarRoot.getTrash() == null) {
                log.error("updateDragWindow trash is null");
            } else {
                mSidebarRoot.getTrash().initTrashView();
            }
            lp.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            lp.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            mSidebarRoot.setBackgroundResource(R.color.sidebar_root_background);
        } else {
            if (mSidebarRoot.getTrash() != null) {
                mSidebarRoot.getTrash().hideTrashView();
            }
            lp.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            lp.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mSidebarRoot.setBackgroundResource(android.R.color.transparent);
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

    public void resumeTopView(){
        if (mTopView != null) {
            mTopView.resumeToNormal();
        }
    }

    public void setSwitchAppAvailable(boolean available) {
        mSideView.setSwitchAppAvailable(available);
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
            // NA
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
            // NA
        }

        @Override
        public void resumeSidebar() throws RemoteException {
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    Utils.resumeSidebar(mContext);
                }
            });
        }

        @Override
        public void updateOngoing(ComponentName name, int token,
                int pendingNumbers, CharSequence title, int pid) throws RemoteException {
            OngoingManager.getInstance(mContext).updateOngoing(name, token, pendingNumbers, title, pid);
        }

        @Override
        public void setEnabled(final boolean enabled) throws RemoteException {
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    SidebarController.this.setEnabled(enabled);
                }
            });
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                Utils.resumeSidebar(context);
            }
        }
    };

    private static final String ACTION_UPDATE_ICON = "com.smartisanos.launcher.update_icon";
    private static final String EXTRA_PACKAGENAME = "extra_packagename";

    private BroadcastReceiver mIconChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_UPDATE_ICON.equals(action)) {
                String packageNames = intent.getStringExtra(EXTRA_PACKAGENAME);
                if (packageNames != null) {
                    String[] packagearr = packageNames.split(",");
                    if (packagearr != null) {
                        Set<String> packages = new HashSet<String>();
                        for (String pkg : packagearr) {
                            packages.add(pkg);
                        }
                        ResolveInfoManager.getInstance(mContext).onIconChanged(packages);
                        AppManager.getInstance(mContext).onIconChanged(packages);
                    }
                }
            }
        }
    };
}
