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
import android.widget.AdapterView;
import android.widget.FrameLayout;
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

public class SideView extends RelativeLayout {
    private static final LOG log = LOG.getInstance(SideView.class);

    private View mExitAndAdd;
    private View mLeftShadow, mRightShadow;
    private ImageView mExit, mSetting;

    private SidebarListView mOngoingList, mAppList, mContactList;
    private SidebarListView mOngoingListFake, mShareListFake, mContactListFake;

    private AppListAdapter mAppAdapter;
    private ResolveInfoListAdapter mResolveAdapter;
    private ScrollView mScrollView;

    private ContactListAdapter mContactAdapter;

    private Context mContext;
    private FrameLayout mDarkBgView;
    private LinearLayout mAddAndExitDarkBg;
    private SidebarListView mDraggedListView;

    private LinearLayout mSideViewContentNormal;
    private LinearLayout mSideViewContentDragged;

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
        mDarkBgView = (FrameLayout) findViewById(R.id.side_view_dark_bg);
        mAddAndExitDarkBg = (LinearLayout) findViewById(R.id.exit_and_add_dark_bg);
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
        mContactList.setAdapter(mContactAdapter);
        mContactList.setOnItemClickListener(mContactItemOnClickListener);

        mContactListFake = (SidebarListView) findViewById(R.id.contactlist_fake);
        mContactListFake.setSideView(this);
        mContactListFake.setNeedFootView(true);
        mContactListFake.setAdapter(new ContactListAdapter(mContext));

        //resolve
        mAppList = (SidebarListView) findViewById(R.id.applist);
        mAppList.setSideView(this);
        mAppAdapter = new AppListAdapter(mContext);
        mAppList.setAdapter(mAppAdapter);
        mAppList.setOnItemClickListener(mAppItemOnClickListener);

        mShareListFake = (SidebarListView) findViewById(R.id.sharelist);
        mShareListFake.setSideView(this);
        mShareListFake.setAdapter(mResolveAdapter = new ResolveInfoListAdapter(mContext));

        mScrollView = (ScrollView) findViewById(R.id.sideview_scroll_list);
    }

    public void requestStatus(SidebarStatus status) {
        if(status == SidebarStatus.NORMAL) {
            mSideViewContentDragged.setVisibility(GONE);
            mSideViewContentNormal.setVisibility(VISIBLE);
        } else {
            mSideViewContentDragged.setVisibility(VISIBLE);
            mSideViewContentNormal.setVisibility(GONE);
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
        boolean leftMode = (SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT);
        int width = getWidth();
        final int outTo;
        final int inFrom;
        if (leftMode) {
            outTo = -width;
            inFrom = -width;
        } else {
            outTo = width;
            inFrom = width;
        }
        int time = 200;
        Anim moveOut = new Anim(mSideViewContentNormal, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(outTo, 0));
        moveOut.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(int type) {
                mSideViewContentNormal.setVisibility(GONE);
                mSideViewContentDragged.setVisibility(VISIBLE);
            }
        });
        Anim moveIn = new Anim(mSideViewContentDragged, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(inFrom, 0), new Vector3f());
        moveIn.setDelay(time);
        mSwitchContentAnim = new AnimTimeLine();
        mSwitchContentAnim.addAnim(moveOut);
        mSwitchContentAnim.addAnim(moveIn);
        mSwitchContentAnim.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                //show mSideViewContentDragged
                mOngoingListFake.onDragStart(event);
                mContactListFake.onDragStart(event);
                mShareListFake.onDragStart(event);
                mSideViewContentDragged.setTranslationX(outTo);
            }

            @Override
            public void onComplete(int type) {
                if (mSwitchContentAnim != null) {
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
        boolean leftMode = (SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT);
        int width = getWidth();
        final int outTo;
        final int inFrom;
        if (leftMode) {
            outTo = -width;
            inFrom = -width;
        } else {
            outTo = width;
            inFrom = width;
        }
        int time = 200;
        Anim moveOut = new Anim(mSideViewContentDragged, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(outTo, 0));
        moveOut.setListener(new AnimListener() {
            @Override
            public void onStart() {
                mSideViewContentNormal.setTranslationX(outTo);
            }

            @Override
            public void onComplete(int type) {
                mSideViewContentDragged.setVisibility(GONE);
                mSideViewContentNormal.setVisibility(VISIBLE);
                mOngoingListFake.onDragEnd();
                mContactListFake.onDragEnd();
                mShareListFake.onDragEnd();
            }
        });
        Anim moveIn = new Anim(mSideViewContentNormal, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(inFrom, 0), new Vector3f());
        moveIn.setDelay(time);

        mSwitchContentAnim = new AnimTimeLine();
        mSwitchContentAnim.addAnim(moveOut);
        mSwitchContentAnim.addAnim(moveIn);
        mSwitchContentAnim.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(int type) {
                if (mSwitchContentAnim != null) {
                    mSideViewContentNormal.setTranslationX(0);
                    mSideViewContentNormal.setVisibility(VISIBLE);
                    mSideViewContentDragged.setVisibility(GONE);
                    mSwitchContentAnim = null;
                }
            }
        });
        mSwitchContentAnim.start();
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        int action = event.getAction();
        FloatText.handleDragEvent(mContext, event);
        switch (action) {
        case DragEvent.ACTION_DRAG_STARTED:
            onDragStart(event);
            return super.dispatchDragEvent(event);
        case DragEvent.ACTION_DRAG_ENDED:
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

    private AdapterView.OnItemClickListener mAppItemOnClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            AppItem ai = (AppItem) mAppAdapter.getItem(position);
            ai.openUI(mContext);
        }
    };

    private AdapterView.OnItemClickListener mContactItemOnClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (view == null || view.getTag() == null) {
                return;
            }
            ContactItem ci = (ContactItem) mContactAdapter.getItem(position);
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

    /**
    private AnimTimeLine shakeIconAnim(View view) {
        AnimTimeLine timeLine = new AnimTimeLine();
        int time = 70;
        Anim anim1 = new Anim(view, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(-6, 0));
        Anim anim2 = new Anim(view, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(-6, 0), new Vector3f(12, 0));
        anim2.setDelay(time);
        Anim anim3 = new Anim(view, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(12, 0), new Vector3f(-9, 0));
        anim3.setDelay(time * 2);
        Anim anim4 = new Anim(view, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(-9, 0), new Vector3f(6, 0));
        anim4.setDelay(time * 3);
        Anim anim5 = new Anim(view, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(6, 0), new Vector3f(-5, 0));
        anim5.setDelay(time * 4);
        Anim anim6 = new Anim(view, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(-5, 0), new Vector3f());
        anim6.setDelay(time * 5);
        timeLine.addAnim(anim1);
        timeLine.addAnim(anim2);
        timeLine.addAnim(anim3);
        timeLine.addAnim(anim4);
        timeLine.addAnim(anim5);
        timeLine.addAnim(anim6);
        return timeLine;
    }
    **/

    public FrameLayout getDarkBgView() {
        return mDarkBgView;
    }

    public LinearLayout getAddAndExitDarkBg() {
        return mAddAndExitDarkBg;
    }

    public boolean setBgMode(boolean toDark) {
        if (mDarkBgView == null || mAddAndExitDarkBg == null) {
            log.error("mDarkBgView or mAddAndExitDarkBg is null");
            return false;
        }
        int color = Constants.SHADOW_BG_COLOR_LIGHT;
        if (toDark) {
            color = Constants.SHADOW_BG_COLOR_DARK;
        }
        mDarkBgView.setBackgroundColor(color);
        mAddAndExitDarkBg.setBackgroundColor(color);
        return true;
    }
}
