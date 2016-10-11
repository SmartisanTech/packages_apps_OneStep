package com.smartisanos.sidebar.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.android.internal.sidebar.ISidebarService;
import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.SidebarStatus;
import com.smartisanos.sidebar.util.AppItem;
import com.smartisanos.sidebar.util.Constants;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SideView extends RelativeLayout {
    private static final LOG log = LOG.getInstance(SideView.class);

    private View mExitAndAdd;
    private View mLeftShadow, mRightShadow;
    private ImageView mExit, mSetting;

    private SidebarListView mOngoingList, mContactList, mAppList;
    private SidebarListView mOngoingListFake, mContactListFake, mShareList;

    private AppListAdapter mAppAdapter;
    private ResolveInfoListAdapter mResolveAdapter;
    private ScrollView mScrollView;

    private ContactListAdapter mContactAdapter;

    private Context mContext;
    private SidebarListView mDraggedListView;

    private LinearLayout mSideViewContentNormal;
    private LinearLayout mSideViewContentDragged;

    private DimSpaceView mDimView;

    private boolean mSwitchAppAvailable = false;
    private SwitchAppView mSwitchAppView;

    public SideView(Context context) {
        this(context, null);
    }

    public SideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SideView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public void setDraggedList(SidebarListView listview) {
        mDraggedListView = listview;
    }

    public SidebarListView getDraggedListView() {
        return mDraggedListView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDimView = (DimSpaceView)findViewById(R.id.side_dim_view);
        mExitAndAdd = findViewById(R.id.exit_and_add);
        mExit = (ImageView) findViewById(R.id.exit);
        mLeftShadow = findViewById(R.id.left_shadow);
        mRightShadow = findViewById(R.id.right_shadow);
        updateUIBySidebarMode();
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityManager.isUserAMonkey()) {
                    return;
                }
                AnimStatusManager asm = AnimStatusManager.getInstance();
                if (asm.isEnterAnimOngoing() || asm.isExitAnimOngoing()) {
                    return;
                }
                ISidebarService sidebarService = ISidebarService.Stub.asInterface(ServiceManager.getService(Context.SIDEBAR_SERVICE));
                if (sidebarService != null) {
                    try {
                        sidebarService.resetWindow();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mSetting = (ImageView) findViewById(R.id.setting);
        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.dismissAllDialog(mContext);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage(mContext.getPackageName());// ourself
                mContext.startActivity(intent);
            }
        });

        mSideViewContentNormal = (LinearLayout) findViewById(R.id.side_view_normal);
        mSideViewContentDragged = (LinearLayout) findViewById(R.id.side_view_dragged);

//        //ongoing
        mOngoingList = (SidebarListView) findViewById(R.id.ongoinglist);
        mOngoingList.setSideView(this);
        mOngoingList.setAdapter(new OngoingAdapter(mContext));

        mOngoingListFake = (SidebarListView) findViewById(R.id.ongoinglist_fake);
        mOngoingListFake.setSideView(this);
        mOngoingListFake.setAdapter(new OngoingAdapter(mContext));

        //contact
        mContactList = (SidebarListView) findViewById(R.id.contactlist);
        mContactList.setSideView(this);
        mContactList.setNeedFootView(true);
        mContactAdapter = new ContactListAdapter(mContext);
        mContactAdapter.isEnableIconShadow = true;
        mContactList.setAdapter(mContactAdapter);
        mContactList.setOnItemClickListener(mContactItemOnClickListener);

        mContactListFake = (SidebarListView) findViewById(R.id.contactlist_fake);
        mContactListFake.setSideView(this);
        mContactListFake.setNeedFootView(true);
        mContactListFake.setAdapter(new ContactListAdapter(mContext));

        //resolve
        mSwitchAppAvailable = Utils.isSwitchAppAvailable(mContext);
        mSwitchAppView = (SwitchAppView) findViewById(R.id.switch_app);
        mSwitchAppView.setOnClickListener(mSwitchAppListener);
        mSwitchAppView.setVisibility(mSwitchAppAvailable ? View.VISIBLE : View.GONE);

        mAppList = (SidebarListView) findViewById(R.id.applist);
        mAppList.setSideView(this);
        mAppAdapter = new AppListAdapter(mContext, mAppList);
        mAppList.setAdapter(mAppAdapter);
        mAppList.setOnItemClickListener(mAppItemOnClickListener);

        mShareList = (SidebarListView) findViewById(R.id.sharelist);
        mShareList.setSideView(this);
        mShareList.setAdapter(mResolveAdapter = new ResolveInfoListAdapter(mContext, mShareList));

        mScrollView = (ScrollView) findViewById(R.id.sideview_scroll_list);
        Utils.setAlwaysCanAcceptDragForAll(mSideViewContentDragged, true);
        ViewGroup vg = (ViewGroup) mSideViewContentDragged.getParent();
        vg.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                //this is necessary, if the parent of mSideViewContentDragged return false, the sideview
                //will not dispatch event to mSideViewContentDragged ...
                return true;
            }
        });
    }

    public void setSwitchAppAvailable(boolean available) {
        if(mSwitchAppAvailable != available) {
            mSwitchAppAvailable = available;
            mSwitchAppView.setVisibility(mSwitchAppAvailable ? View.VISIBLE : View.GONE);
        }
    }

    public void requestStatus(SidebarStatus status) {
        if(status == SidebarStatus.NORMAL) {
            //show mSideViewContentNormal
            onDragEnd(null);
        } else {
            //show mSideViewContentDragged
            onDragStart(null);
        }
    }

    public View getShadowLineView() {
        if (mLeftShadow != null) {
            if (mLeftShadow.getVisibility() == VISIBLE) {
                return mLeftShadow;
            }
        }
        if (mRightShadow != null) {
            if (mRightShadow.getVisibility() == VISIBLE) {
                return mRightShadow;
            }
        }
        return null;
    }

    public void refreshDivider() {
        if (mContactList != null) {
            mContactList.requestLayout();
        }
        if (mContactListFake != null) {
            mContactListFake.requestLayout();
        }
    }

    public boolean someListIsEmpty() {
        if (mContactList != null && mContactList.getAdapter() != null && mContactList.getAdapter().getCount() > 0
                && mAppList != null && mAppList.getAdapter() != null && mAppList.getAdapter().getCount() > 0) {
            return false;
        }
        return true;
    }

    private AnimTimeLine mSwitchContentAnim;

    private void onDragStart(final DragEvent event) {
        if (mSwitchContentAnim != null) {
            mSwitchContentAnim.cancel();
        }
        int deltaWidth = mContext.getResources().getDimensionPixelSize(R.dimen.sidebar_list_anim_padding);
        boolean leftMode = (SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT);
        int width = getWidth() + deltaWidth;
        int outTo = leftMode ? -width : width;
        mSwitchContentAnim = new AnimTimeLine();
        int[] contentLoc = new int[2];
        mSideViewContentNormal.getLocationOnScreen(contentLoc);
        int time = 300;
        final List<View> disappearViews = new ArrayList<View>();
        Anim hideSwitchAppViewDivider = null;
        if (mSwitchAppView.getVisibility() == VISIBLE) {
            disappearViews.add(mSwitchAppView.getIconView());
            Vector3f alphaFrom = new Vector3f(0, 0, 1);
            Vector3f alphaTo   = new Vector3f(0, 0, 0);
            hideSwitchAppViewDivider = new Anim(mSwitchAppView.getDivider(), Anim.TRANSPARENT, time, Anim.CUBIC_OUT, alphaFrom, alphaTo);
        }
        disappearViews.addAll(mOngoingList.shownViewList(contentLoc[1]));
        disappearViews.addAll(mContactList.shownViewList(contentLoc[1]));
        disappearViews.addAll(mAppList.shownViewList(contentLoc[1]));
        if (disappearViews.size() > 0) {
            Vector3f scaleFrom = new Vector3f(1, 1);
            Vector3f scaleTo   = new Vector3f(0.2f, 0.2f);
            Vector3f alphaFrom = new Vector3f(0, 0, 1);
            Vector3f alphaTo   = new Vector3f(0, 0, 0);
            int count = disappearViews.size();
            for (int i = 0; i < count; i++) {
                View view = disappearViews.get(i);
                Anim scale = new Anim(view, Anim.SCALE, time, Anim.CUBIC_OUT, scaleFrom, scaleTo);
                Anim alpha = new Anim(view, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, alphaFrom, alphaTo);
                mSwitchContentAnim.addAnim(scale);
                mSwitchContentAnim.addAnim(alpha);
            }
        }

        if (event != null) {
            mOngoingListFake.onDragStart(event);
            mContactListFake.onDragStart(event);
            mShareList.onDragStart(event);
        }
        mSideViewContentDragged.setTranslationX(outTo);
        Anim inAnim = new Anim(mSideViewContentDragged, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(outTo, 0), new Vector3f());
        inAnim.setDelay(time / 4);
        if (hideSwitchAppViewDivider != null) {
            mSwitchContentAnim.addAnim(hideSwitchAppViewDivider);
        }
        mSwitchContentAnim.addAnim(inAnim);
        mSwitchContentAnim.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                mSideViewContentDragged.setVisibility(VISIBLE);
            }

            @Override
            public void onComplete(int type) {
                if (mSwitchContentAnim != null) {
                    int count = disappearViews.size();
                    for (int i = 0; i < count; i++) {
                        View view = disappearViews.get(i);
                        view.setAlpha(1);
                        view.setScaleX(1);
                        view.setScaleY(1);
                    }
                    if (mSwitchAppView.getVisibility() == VISIBLE) {
                        mSwitchAppView.getDivider().setAlpha(1);
                    }
                    mSideViewContentNormal.setVisibility(GONE);
                    mSideViewContentDragged.setVisibility(VISIBLE);
                    mSideViewContentDragged.setTranslationX(0);
                    mSwitchContentAnim = null;
                }
            }
        });
        mSwitchContentAnim.start();
    }

    private void onDragEnd(DragEvent event) {
        if (mSwitchContentAnim != null) {
            mSwitchContentAnim.cancel();
        }
        int deltaWidth = mContext.getResources().getDimensionPixelSize(R.dimen.sidebar_list_anim_padding);
        boolean leftMode = (SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT);
        int width = getWidth() + deltaWidth;
        int outTo = leftMode ? -width : width;
        int time = 300;
        int[] contentLoc = new int[2];
        mSideViewContentNormal.getLocationOnScreen(contentLoc);
        final List<View> disappearViews = new ArrayList<View>();
        if (mSwitchAppView.getVisibility() == VISIBLE) {
            disappearViews.add(mSwitchAppView.getIconView());
        }
        disappearViews.addAll(mOngoingList.shownViewList(contentLoc[1]));
        disappearViews.addAll(mContactList.shownViewList(contentLoc[1]));
        disappearViews.addAll(mAppList.shownViewList(contentLoc[1]));
        mSwitchContentAnim = new AnimTimeLine();
        int subViewCount = disappearViews.size();
        if (subViewCount > 0) {
            AnimTimeLine timeLine = new AnimTimeLine();
            Vector3f scaleFrom = new Vector3f(0.2f, 0.2f);
            Vector3f scaleTo   = new Vector3f(1, 1);
            Vector3f alphaFrom = new Vector3f(0, 0, 0);
            Vector3f alphaTo   = new Vector3f(0, 0, 1);
            for (int i = 0; i < subViewCount; i++) {
                View view = disappearViews.get(i);
                Anim scale = new Anim(view, Anim.SCALE, time, Anim.CUBIC_OUT, scaleFrom, scaleTo);
                Anim alpha = new Anim(view, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, alphaFrom, alphaTo);
                timeLine.addAnim(scale);
                timeLine.addAnim(alpha);
            }
            timeLine.setDelay(time / 4);
            mSwitchContentAnim.addTimeLine(timeLine);
        }

        Anim outAnim = new Anim(mSideViewContentDragged, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(outTo, 0));
        mSwitchContentAnim.addAnim(outAnim);
        mSwitchContentAnim.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                mSideViewContentNormal.setVisibility(VISIBLE);
            }

            @Override
            public void onComplete(int type) {
                if (mSwitchContentAnim != null) {
                    int count = disappearViews.size();
                    for (int i = 0; i < count; i++) {
                        View view = disappearViews.get(i);
                        view.setAlpha(1);
                        view.setScaleX(1);
                        view.setScaleY(1);
                    }

                    mOngoingListFake.onDragEnd();
                    mContactListFake.onDragEnd();
                    mShareList.onDragEnd();

                    mSideViewContentNormal.setVisibility(VISIBLE);
                    mSideViewContentDragged.setTranslationX(0);
                    mSideViewContentDragged.setVisibility(GONE);
                    mSwitchContentAnim = null;
                }
            }
        });
        mSwitchContentAnim.start();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mAppAdapter.getCount() + mContactAdapter.getCount() <= 0) {
            mSwitchAppView.setDividerVisibility(View.GONE);
        } else {
            mSwitchAppView.setDividerVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        int action = event.getAction();
        switch (action) {
        case DragEvent.ACTION_DRAG_STARTED:
            FloatText.getInstance(mContext).start();
            onDragStart(event);
            return super.dispatchDragEvent(event);
        case DragEvent.ACTION_DRAG_ENDED:
            FloatText.getInstance(mContext).end();
            boolean ret = super.dispatchDragEvent(event);
            onDragEnd(event);
            return ret;
        }
        return super.dispatchDragEvent(event);
    }

    private void updateUIBySidebarMode() {
        if (SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT) {
            //mExit.setBackgroundResource(R.drawable.exit_icon_left);
            mExit.setImageResource(R.drawable.exit_icon_left);
            mExitAndAdd.setBackgroundResource(R.drawable.exitandadd_bg_left);
            mLeftShadow.setVisibility(View.VISIBLE);
            mRightShadow.setVisibility(View.GONE);
        } else {
            //mExit.setBackgroundResource(R.drawable.exit_icon_right);
            mExit.setImageResource(R.drawable.exit_icon_right);
            mExitAndAdd.setBackgroundResource(R.drawable.exitandadd_bg_right);
            mLeftShadow.setVisibility(View.GONE);
            mRightShadow.setVisibility(View.VISIBLE);
        }
    }

    public void onSidebarModeChanged(){
        updateUIBySidebarMode();
        mResolveAdapter.notifyDataSetChanged();
    }

    private View.OnClickListener mSwitchAppListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.dismissAllDialog(mContext);
            Utils.launchPreviousApp(mContext);
        }
    };

    private AdapterView.OnItemClickListener mAppItemOnClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (position < mAppAdapter.getCount()) {
                AppItem ai = (AppItem) mAppAdapter.getItem(position);
                Utils.dismissAllDialog(mContext);
                ai.openUI(mContext);
            }
        }
    };

    private AdapterView.OnItemClickListener mContactItemOnClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (view == null || view.getTag() == null) {
                return;
            }
            ContactItem ci = (ContactItem) mContactAdapter.getItem(position);
            Utils.dismissAllDialog(mContext);
            ci.openUI(mContext);
        }
    };

    private int AREA_TYPE_NORMAL = 0;
    private int AREA_TYPE_TOP = 1;
    private int AREA_TYPE_BOTTOM = 2;

    private int areaType(int x, int y) {
        int touchArea = getResources().getDimensionPixelSize(R.dimen.drag_scroll_view_bottom_area);
        Rect scrollViewRect = new Rect();
        int[] scrollViewLoc = new int[2];
        mScrollView.getLocationOnScreen(scrollViewLoc);
        mScrollView.getDrawingRect(scrollViewRect);
        if (x < scrollViewLoc[0]) {
            return AREA_TYPE_NORMAL;
        }
        if ((scrollViewLoc[1] - 20) < y && y < (scrollViewLoc[1] + touchArea)) {
            if (scrollViewRect.top != 0) {
                return AREA_TYPE_TOP;
            }
        }
        if (y > Constants.WindowHeight - touchArea) {
            if (scrollViewRect.bottom < (mAppList.getHeight() + mContactList.getHeight())) {
                return AREA_TYPE_BOTTOM;
            }
        }
        return AREA_TYPE_NORMAL;
    }

    private volatile boolean scrolling = false;

    private void setScrollTo(int area) {
        int itemViewHeight = getResources().getDimensionPixelSize(R.dimen.drag_scroll_view_bottom_area);
        int scrollY = 0;
        if (area == AREA_TYPE_TOP) {
            scrollY = -itemViewHeight;
        } else if (area == AREA_TYPE_BOTTOM) {
            scrollY = itemViewHeight;
        }
        final int y = scrollY;
//        log.error("setScrollTo type "+area+", Y => " + y);
        scrolling = true;
        mScrollView.setSmoothScrollingEnabled(true);
        post(new Runnable() {
            @Override
            public void run() {
                mScrollView.smoothScrollBy(0, y);
                post(new Runnable() {
                    @Override
                    public void run() {
                        scrolling = false;
                    }
                });
            }
        });
    }

    private long preScrollTime;

    public void dragObjectMove(int x, int y, long eventTime) {
        if (x < mScrollView.getLocationOnScreen()[0]) {
            return;
        }
        mDraggedListView.dragObjectMove(x, y);
        int area = areaType(x, y);
        if (area != AREA_TYPE_NORMAL) {
            if (scrolling) {
                return;
            }
            long delta = eventTime - preScrollTime;
            if (delta < 0) {
                delta = delta * -1;
            }
            if (delta < 250) {
//                log.error("preScrollTime ["+preScrollTime+"] ["+eventTime+"]");
                return;
            }
            preScrollTime = eventTime;
            setScrollTo(area);
        }
    }

    private void restoreListItemView(SidebarListView listView) {
        if (listView != null) {
            try {
                int count = listView.getCount();
                if (count == 0) {
                    return;
                }
                for (int i = 0; i < count; i++) {
                    View view = listView.getChildAt(i);
                    if (view == null) {
                        continue;
                    }
                    view.setScaleX(1);
                    view.setScaleY(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void restoreView() {
        restoreListItemView(mContactList);
        restoreListItemView(mAppList);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            mDimView.resume().start();
        } else {
            mDimView.dim().start();
        }
    }
}
