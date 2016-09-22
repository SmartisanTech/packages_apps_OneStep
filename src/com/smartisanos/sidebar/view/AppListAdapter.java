package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.AppItem;
import com.smartisanos.sidebar.util.AppManager;
import com.smartisanos.sidebar.util.DataManager;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
import com.smartisanos.sidebar.util.anim.Vector3f;

public class AppListAdapter extends DragEventAdapter {
    private static final LOG log = LOG.getInstance(AppListAdapter.class);

    private Context mContext;
    private List<AppItem> mAppItems;
    private List<AppItem> mAcceptableAppItems = new ArrayList<AppItem>();
    private DragEvent mDragEvent;
    private AppManager mManager;
    private boolean mPendingUpdate = false;
    public AppListAdapter(Context context) {
        mContext = context;
        mManager = AppManager.getInstance(context);
        mAppItems = mManager.getAddedAppItem();
        mAcceptableAppItems.addAll(mAppItems);
        mManager.addListener(resolveInfoUpdateListener);
        AnimStatusManager.getInstance().addAnimFlagStatusChangedListener(
                AnimStatusManager.SIDEBAR_ITEM_DRAGGING,
                new AnimStatusManager.AnimFlagStatusChangedListener() {
                    @Override
                    public void onChanged() {
                        if (mPendingUpdate) {
                            updateData();
                        }
                    }
                });
    }

    private DataManager.RecentUpdateListener resolveInfoUpdateListener = new DataManager.RecentUpdateListener() {
        @Override
        public void onUpdate() {
            updateData();
        }
    };

    private void updateData() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (AnimStatusManager.getInstance()
                        .canUpdateSidebarList()) {
                    mAppItems = mManager.getAddedAppItem();
                    updateAcceptableResolveInfos();
                    mPendingUpdate = false;
                } else {
                    mPendingUpdate = true;
                }
            }
        });
    }

    private void updateAcceptableResolveInfos() {
        mAcceptableAppItems.clear();
        for (AppItem ai : mAppItems) {
            if (mDragEvent == null || ai.acceptDragEvent(mContext, mDragEvent)) {
                mAcceptableAppItems.add(ai);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        SideView sideView = SidebarController.getInstance(mContext).getSideView();
        if (sideView != null) {
            sideView.refreshDivider();
        }
    }

    @Override
    public void onDragStart(DragEvent event) {
        /**
        if (mDragEvent != null) {
            mDragEvent.recycle();
            mDragEvent = null;
        }
        mDragEvent = DragEvent.obtain(event);
        updateAcceptableResolveInfos();
        **/
    }

    @Override
    public void onDragEnd() {
        /**
        if (mDragEvent == null) {
            return;
        }
        mDragEvent.recycle();
        mDragEvent = null;
        updateAcceptableResolveInfos();
        **/
    }

    @Override
    public int getCount() {
        return mAcceptableAppItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mAcceptableAppItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void moveItemPostion(Object object, int index) {
        AppItem item = (AppItem)object;
        if (index < 0) {
            index = 0;
        }
        if (index >= mAppItems.size()) {
            index = mAppItems.size() - 1;
        }
        int now = mAppItems.indexOf(item);
        if (now == -1 || now == index) {
            return;
        }
        mAppItems.remove(item);
        mAppItems.add(index, item);
        onOrderChange();
    }

    private void onOrderChange() {
        for(int i = 0; i < mAppItems.size(); ++ i){
            mAppItems.get(i).setIndex(mAppItems.size() - 1 - i);
        }
        mManager.updateOrder();
    }

    private Anim mIconTouchedAnim;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        AppItem ai = mAcceptableAppItems.get(position);
        if (convertView == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.app_item, null);
            ImageView iconImage = (ImageView) view.findViewById(R.id.avatar_image_view);
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View view, MotionEvent motionEvent) {
                    if (view == null || motionEvent == null) {
                        return false;
                    }
                    int action = motionEvent.getAction();
                    if (action != MotionEvent.ACTION_DOWN) {
                        return false;
                    }
                    if (mIconTouchedAnim != null) {
                        mIconTouchedAnim.cancel();
                    }
                    view.setAlpha(0.4f);
                    mIconTouchedAnim = new Anim(view, Anim.TRANSPARENT, 100, Anim.CUBIC_OUT, new Vector3f(0, 0, 0.4f), new Vector3f(0, 0, 1));
                    mIconTouchedAnim.setListener(new AnimListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onComplete(int type) {
                            if (mIconTouchedAnim != null) {
                                view.setAlpha(1);
                                mIconTouchedAnim = null;
                            }
                        }
                    });
                    mIconTouchedAnim.setDelay(200);
                    mIconTouchedAnim.start();
                    return false;
                }
            });

            holder = new ViewHolder();
            holder.view = view;
            holder.iconImageView = iconImage;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.restore();
        holder.setInfo(ai);
        //Utils.setAlwaysCanAcceptDrag(holder.view, true);
        return holder.view;
    }

    public int objectIndex(AppItem data) {
        if (mAppItems == null) {
            return -1;
        }
        return mAppItems.indexOf(data);
    }

    public static class ViewHolder {
        public View view;
        public ImageView iconImageView;
        public AppItem ai;

        public void setInfo(AppItem ai) {
            this.ai = ai;
            iconImageView.setImageBitmap(ai.getAvatar());
        }

        public void restore(){
            if (view.getVisibility() == View.INVISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
            view.setTranslationY(0);
        }
    }
}
