package com.smartisanos.sidebar.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.internal.sidebar.ISidebarService;
import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.SidebarStatus;
import com.smartisanos.sidebar.util.AppItem;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.DingDingContact;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.MailContact;
import com.smartisanos.sidebar.util.MmsContact;
import com.smartisanos.sidebar.util.Tracker;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.WechatContact;
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

    private OngoingAdapter mOngoingAdapter;
    private AppListAdapter mAppAdapter;
    private ResolveInfoListAdapter mResolveAdapter;
    private DragScrollView mScrollViewNormal, mScrollViewDragged;

    private ContactListAdapter mContactAdapter;

    private Context mContext;
    private SidebarListView mDraggedListView;

    private LinearLayout mSideViewContentDragged;

    private DimSpaceView mDimView;

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
                Tracker.onClick(Tracker.EVENT_SET);
            }
        });

        mSideViewContentDragged = (LinearLayout) findViewById(R.id.side_view_dragged);

//        //ongoing
        mOngoingList = (SidebarListView) findViewById(R.id.ongoinglist);
        mOngoingList.setSideView(this);
        mOngoingList.setAdapter(mOngoingAdapter = new OngoingAdapter(mContext));

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

        mAppList = (SidebarListView) findViewById(R.id.applist);
        mAppList.setSideView(this);
        mAppAdapter = new AppListAdapter(mContext, mAppList);
        mAppList.setAdapter(mAppAdapter);
        mAppList.setOnItemClickListener(mAppItemOnClickListener);

        mShareList = (SidebarListView) findViewById(R.id.sharelist);
        mShareList.setSideView(this);
        mShareList.setAdapter(mResolveAdapter = new ResolveInfoListAdapter(mContext, mShareList));

        mScrollViewNormal = (DragScrollView) findViewById(R.id.sideview_scroll_list_normal);
        mScrollViewDragged = (DragScrollView) findViewById(R.id.sideview_scroll_list_dragged);

        Utils.setAlwaysCanAcceptDragForAll(mSideViewContentDragged, true);
        mScrollViewDragged.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                //this is necessary, if the parent of mSideViewContentDragged return false, the sideview
                //will not dispatch event to mSideViewContentDragged ...
                return true;
            }
        });
        ViewGroup vg= (ViewGroup) mScrollViewDragged.getParent();
        vg.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                //this is necessary, if the parent of mSideViewContentDragged return false, the sideview
                //will not dispatch event to mSideViewContentDragged ...
                return true;
            }
        });
    }

    public void notifyDataSetChanged() {
        mAppAdapter.notifyDataSetChanged();
        mResolveAdapter.notifyDataSetChanged();
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        refreshDivider();
    }

    private void refreshDivider() {
        // for normal
        int now = 0;
        mOngoingList.setNeedFootView(now > 0);
        now += mOngoingList.getChildCount();
        mContactList.setNeedFootView(now > 0);
        now += mContactList.getChildCount();
        mAppList.setNeedFootView(now > 0);

        // for fake
        now = 0;
        mOngoingListFake.setNeedFootView(now > 0);
        now += mOngoingListFake.getChildCount();
        mContactListFake.setNeedFootView(now > 0);
        now += mContactListFake.getChildCount();
        mShareList.setNeedFootView(now > 0);
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
        int time = 300;
        final List<View> disappearViews = new ArrayList<View>();
        disappearViews.addAll(mOngoingList.getViewList());
        disappearViews.addAll(mContactList.getViewList());
        disappearViews.addAll(mAppList.getViewList());
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
        mScrollViewDragged.setTranslationX(outTo);
        Anim inAnim = new Anim(mScrollViewDragged, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(outTo, 0), new Vector3f());
        inAnim.setDelay(time / 4);
        mSwitchContentAnim.addAnim(inAnim);
        mSwitchContentAnim.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                mScrollViewDragged.setVisibility(VISIBLE);
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
                    mScrollViewNormal.setVisibility(GONE);
                    mScrollViewDragged.setVisibility(VISIBLE);
                    mScrollViewDragged.setTranslationX(0);
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
        mSwitchContentAnim = new AnimTimeLine();
        int time = 300;
        final List<View> disappearViews = new ArrayList<View>();
        disappearViews.addAll(mOngoingList.getViewList());
        disappearViews.addAll(mContactList.getViewList());
        disappearViews.addAll(mAppList.getViewList());
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

        Anim outAnim = new Anim(mScrollViewDragged, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(outTo, 0));
        mSwitchContentAnim.addAnim(outAnim);
        mSwitchContentAnim.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                mScrollViewNormal.setVisibility(VISIBLE);
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

                    mScrollViewNormal.setVisibility(VISIBLE);
                    mScrollViewDragged.setTranslationX(0);
                    mScrollViewDragged.setVisibility(GONE);
                    mScrollViewDragged.scrollTo(0, 0);
                    mSwitchContentAnim = null;
                }
            }
        });
        mSwitchContentAnim.start();
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

    private AdapterView.OnItemClickListener mAppItemOnClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Object obj = adapterView.getAdapter().getItem(position);
            if (obj != null && obj instanceof AppItem) {
                AppItem ai = (AppItem) obj;
                Utils.dismissAllDialog(mContext);
                ai.openUI(mContext);
                Tracker.onClick(Tracker.EVENT_CLICK_APP, "package", ai.getPackageName());
            } else {
                if (position < mAppList.getHeaderViewsCount()) {
                    //this is divider
                    return;
                }
                log.info("launch previous app!");
                Utils.dismissAllDialog(mContext);
                Utils.launchPreviousApp(mContext);
                Tracker.onClick(Tracker.EVENT_CLICK_CHANGE);
            }
        }
    };

    private AdapterView.OnItemClickListener mContactItemOnClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (view == null || view.getTag() == null) {
                return;
            }
            Object obj = adapterView.getAdapter().getItem(position);
            if (obj != null && obj instanceof ContactItem) {
                ContactItem ci = (ContactItem) obj;
                Utils.dismissAllDialog(mContext);
                ci.openUI(mContext);
            }
        }
    };

    public void dragObjectMove(MotionEvent event, long eventTime) {
        if (mScrollViewNormal.getVisibility() == View.VISIBLE) {
            mScrollViewNormal.scrollByMotionEvent(event);
        } else {
            mScrollViewDragged.scrollByMotionEvent(event);
        }
        mDraggedListView.dragObjectMove((int)(event.getRawX()), (int)(event.getRawY()));
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

    public void reportToTracker() {
        int countWechat = 0;
        int countDingDing = 0;
        int countMms = 0;
        int countEmail = 0;
        for (ContactItem item : ContactManager.getInstance(mContext).getContactList()) {
            if (item instanceof WechatContact) {
                countWechat++;
            } else if (item instanceof DingDingContact) {
                countDingDing++;
            } else if (item instanceof MmsContact) {
                countMms++;
            } else if (item instanceof MailContact) {
                countEmail++;
            }
        }
        int appNum = 0;
        if (mAppAdapter != null) {
            appNum = mAppAdapter.getCount();
        }
        int shareNum = 0;
        if (mResolveAdapter != null) {
            shareNum = mResolveAdapter.getCount();
        }
        Tracker.reportStatus(Tracker.STATUS_APPNAME,
                "wechat_contacts", countWechat + "",
                "dingding_contacts", countDingDing + "",
                "message_contacts", countMms + "",
                "email_contacts", countEmail + "",
                "app_num", appNum + "",
                "share_num", shareNum + "");
    }
}
