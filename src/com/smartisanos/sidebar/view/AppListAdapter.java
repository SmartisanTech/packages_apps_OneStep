package com.smartisanos.sidebar.view;

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

public class AppListAdapter extends SidebarAdapter {
    private static final LOG log = LOG.getInstance(AppListAdapter.class);

    private Context mContext;
    private List<AppItem> mAppItems;
    private AppManager mManager;
    private boolean mPendingUpdate = false;
    private SidebarListView mListView;
    public AppListAdapter(Context context, SidebarListView listview) {
        mContext = context;
        mListView = listview;
        mListView.setUnDragNumber(1);
        mManager = AppManager.getInstance(context);
        mAppItems = mManager.getAddedAppItem();
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
            // do anim first !
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mListView.animWhenDatasetChange();
                }
            });
        }
    };

    public void updateData() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (AnimStatusManager.getInstance()
                        .canUpdateSidebarList()) {
                    mAppItems = mManager.getAddedAppItem();
                    notifyDataSetChanged();
                    mPendingUpdate = false;
                } else {
                    mPendingUpdate = true;
                }
            }
        });
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
        return mAppItems.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) {
            return null;
        }
        return mAppItems.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void moveItemPostion(Object object, int index) {
        index --;
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
        if (convertView == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.app_item, null);
            View switchApp = view.findViewById(R.id.switch_app);
            ImageView iconImage = (ImageView) view.findViewById(R.id.avatar_image_view);
            iconImage.setOnTouchListener(new View.OnTouchListener() {
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
            holder.switchApp = switchApp;
            holder.iconImageView = iconImage;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.restore();
        if (position == 0) {
            holder.showSwitchApp();
        } else {
            holder.setInfo(mAppItems.get(position - 1));
        }
        return holder.view;
    }

    public static class ViewHolder {
        public View view;
        public View switchApp;
        public ImageView iconImageView;

        public void showSwitchApp() {
            iconImageView.setVisibility(View.GONE);
            switchApp.setVisibility(View.VISIBLE);
        }

        public void setInfo(AppItem ai) {
            iconImageView.setVisibility(View.VISIBLE);
            switchApp.setVisibility(View.GONE);
            iconImageView.setImageDrawable(ai.getAvatar());
        }

        public void restore() {
            view.setVisibility(View.VISIBLE);
            view.setTranslationY(0);
        }
    }
}
