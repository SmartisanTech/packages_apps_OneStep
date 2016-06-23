package com.smartisanos.sidebar.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.EventLog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.android.internal.sidebar.ISidebarService;
import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.view.ContentView.ContentType;

public class SideView extends RelativeLayout {
    private static final LOG log = LOG.getInstance(SideView.class);

    private SidebarController mController;

    private Button mExit;
    private Button mAdd;

    private SidebarListView mShareList, mContactList;
    private ResolveInfoListAdapter mResolveAdapter;
    private ScrollView mScrollList;

    private BaseAdapter mContactAdapter;

    private Context mContext;

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

    private SideView mSideView;
    private SidebarRootView mRootView;

    public void setRootView(SidebarRootView view) {
        mRootView = view;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mExit = (Button) findViewById(R.id.exit);
        updateExitButtonBackground();
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        mAdd = (Button)findViewById(R.id.add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SidebarController sc = SidebarController.getInstance(mContext);
                if(sc.getCurrentContentType() == ContentType.NONE){
                    sc.dimTopView();
                    sc.showContent(ContentType.ADDTOSIDEBAR);
                }else if(sc.getCurrentContentType() == ContentType.ADDTOSIDEBAR){
                    sc.resumeTopView();
                    sc.dismissContent();
                }
            }
        });

        mSideView = this;

        //contact
        mContactList = (SidebarListView) findViewById(R.id.contactlist);
        mContactList.setNeedFootView(true);
        mContactAdapter = new ContactListAdapter(mContext);
        mContactList.setAdapter(mContactAdapter);

        //resolve
        mShareList = (SidebarListView) findViewById(R.id.sharelist);
        mResolveAdapter = new ResolveInfoListAdapter(mContext);
        mShareList.setAdapter(mResolveAdapter);
        mShareList.setOnItemLongClickListener(mShareItemOnLongClickListener);

        mScrollList = (ScrollView) findViewById(R.id.sideview_scroll_list);
    }

    public void notifyAppListDataSetChanged() {
        if (mResolveAdapter != null) {
            mResolveAdapter.notifyDataSetChanged();
        }
    }

    private void updateExitButtonBackground() {
        if (SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT) {
            mExit.setBackgroundResource(R.drawable.exit_icon_left);
        } else {
            mExit.setBackgroundResource(R.drawable.exit_icon_right);
        }
    }

    public void onSidebarModeChanged(){
        updateExitButtonBackground();
        mResolveAdapter.notifyDataSetChanged();
    }

    private static final boolean DEV_FLAG = false;

    private AdapterView.OnItemLongClickListener mShareItemOnLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (DEV_FLAG) {
                return false;
            }
            if (view == null) {
                log.error("onItemLongClick return by view is null");
                return false;
            }

            if (view.getTag() == null) {
                log.error("onItemLongClick return by tag is null");
                return false;
            }
            ResolveInfoListAdapter.ViewHolder holder = (ResolveInfoListAdapter.ViewHolder) view.getTag();
            int width = holder.iconImageView.getWidth();
            int height = holder.iconImageView.getHeight();
            PackageManager pm = mContext.getPackageManager();
            ResolveInfoGroup data = holder.resolveInfoGroup;
            Drawable iconDrawable = data.loadIcon(pm);
            Bitmap icon = drawableToBitmap(iconDrawable, width, height);

            int index = mResolveAdapter.objectIndex(data);
            mShareList.setPrePosition(index);
            SidebarRootView.DragItem dragItem = new SidebarRootView.DragItem(
                    mContext, SidebarRootView.DragItem.TYPE_APPLICATION, icon, data, index);
            mRootView.startDrag(dragItem);
            mSideView.getLocationOnScreen(sideViewLoc);
            holder.view.setVisibility(View.INVISIBLE);
            return false;
        }
    };

    private Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public int[] sideViewLoc = new int[2];
    public int[] appListLoc = new int[2];
    private Rect drawingRect = new Rect();
    private int[] scrollViewLoc = new int[2];

    private int[] preLoc = new int[2];

    public void dragObjectMove(int x, int y) {
        if (x < sideViewLoc[0]) {
            return;
        }
        preLoc[0] = x;
        preLoc[1] = y;
        mShareList.getLocationOnScreen(appListLoc);
        mScrollList.getLocationOnScreen(scrollViewLoc);
//        log.error("app list view loc ["+appListLoc[0]+"], ["+appListLoc[1]+"]");
        log.error("touch at ["+x+", "+y+"] scroll view H ["+mScrollList.getHeight()+"], TOP ["+mScrollList.getTop()+"]");
        int viewWidth = mShareList.getWidth();
        int viewHeight = mShareList.getHeight();
        if (inArea(x, y, viewWidth, viewHeight, appListLoc)) {
            log.error("in list area");
            int count = mResolveAdapter.getCount();
            if (count > 0) {
                //convert global coordinate to view local coordinate
                log.error("h ["+viewHeight+"]");
                mShareList.getDrawingRect(drawingRect);
                int[] localLoc = convertToLocalCoordinate(x, y, appListLoc, drawingRect);
                log.error("local loc Y ["+localLoc[1]+"]");
                int subViewHeight = drawingRect.bottom / count;
                int position = localLoc[1] / subViewHeight;
                mShareList.pointToNewPositionWithAnim(position);
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        //list view scroll
//                    }
//                });
            } else {
            }
        } else {
            log.error("out list area");
        }
    }

    private int[] convertToLocalCoordinate(int x, int y, int[] viewLoc, Rect drawingRect) {
        int[] loc = new int[2];
        loc[0] = x - viewLoc[0];
        loc[1] = y - viewLoc[1];
        loc[0] = loc[0] + drawingRect.left;
        loc[1] = loc[1] + drawingRect.top;
        return loc;
    }

    private boolean inArea(float x, float y, int viewWidth, int viewHeight, int[] loc) {
        int left   = loc[0];
        int top    = loc[1];
        int right  = left + viewWidth;
        int bottom = top + viewHeight;
        if (left < x && x < right) {
            if (top < y && y < bottom) {
                return true;
            }
        }
        return false;
    }
}
