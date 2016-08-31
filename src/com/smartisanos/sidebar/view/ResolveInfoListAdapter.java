package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.Constants;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;

public class ResolveInfoListAdapter extends DragEventAdapter {
    private static final LOG log = LOG.getInstance(ResolveInfoListAdapter.class);
    private static final float SCALE_SIZE = 1.4f;

    private Context mContext;
    private List<ResolveInfoGroup> mResolveInfos;
    private List<ResolveInfoGroup> mAcceptableResolveInfos = new ArrayList<ResolveInfoGroup>();
    private DragEvent mDragEvent;
    private ResolveInfoManager mManager;
    private boolean mPendingUpdate = false;
    public ResolveInfoListAdapter(Context context) {
        mContext = context;
        mManager = ResolveInfoManager.getInstance(context);
        mResolveInfos = mManager.getAddedResolveInfoGroup();
        mAcceptableResolveInfos.addAll(mResolveInfos);
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

    private ResolveInfoManager.ResolveInfoUpdateListener resolveInfoUpdateListener = new ResolveInfoManager.ResolveInfoUpdateListener() {
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
                    mResolveInfos = mManager.getAddedResolveInfoGroup();
                    updateAcceptableResolveInfos();
                    mPendingUpdate = false;
                } else {
                    mPendingUpdate = true;
                }
            }
        });
    }

    private void updateAcceptableResolveInfos() {
        mAcceptableResolveInfos.clear();
        for (ResolveInfoGroup rig : mResolveInfos) {
            if (mDragEvent == null || rig.acceptDragEvent(mContext, mDragEvent)) {
                mAcceptableResolveInfos.add(rig);
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
        if (mDragEvent != null) {
            mDragEvent.recycle();
            mDragEvent = null;
        }
        mDragEvent = DragEvent.obtain(event);
        updateAcceptableResolveInfos();
    }

    @Override
    public void onDragEnd() {
        if (mDragEvent == null) {
            return;
        }
        mDragEvent.recycle();
        mDragEvent = null;
        updateAcceptableResolveInfos();
    }

    @Override
    public int getCount() {
        return mAcceptableResolveInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mAcceptableResolveInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void moveItemPostion(Object object, int index) {
        ResolveInfoGroup item = (ResolveInfoGroup)object;
        if (index < 0) {
            index = 0;
        }
        if (index >= mResolveInfos.size()) {
            index = mResolveInfos.size() - 1;
        }
        int now = mResolveInfos.indexOf(item);
        if (now == -1 || now == index) {
            return;
        }
        mResolveInfos.remove(item);
        mResolveInfos.add(index, item);
        onOrderChange();
    }

    private void onOrderChange() {
        for(int i = 0; i < mResolveInfos.size(); ++ i){
            mResolveInfos.get(i).setIndex(mResolveInfos.size() - 1 - i);
        }
        mManager.updateOrder();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        ResolveInfoGroup resolveInfoGroup = mAcceptableResolveInfos.get(position);
        if (convertView == null || !(convertView instanceof RelativeLayout)) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.shareitem, null);
            ImageView iconInputLeft = (ImageView) view.findViewById(R.id.icon_input_left);
            ImageView iconInputRight = (ImageView) view.findViewById(R.id.icon_input_right);
            ImageView iconInputDragLeft = (ImageView) view.findViewById(R.id.icon_input_drag_left);
            ImageView iconInputDragRight = (ImageView) view.findViewById(R.id.icon_input_drag_right);
            ImageView iconImage = (ImageView) view.findViewById(R.id.shareitemimageview);

            holder = new ViewHolder();
            holder.view = view;
            holder.context = mContext;
            holder.iconInputLeft = iconInputLeft;
            holder.iconInputRight = iconInputRight;
            holder.iconInputDragLeft = iconInputDragLeft;
            holder.iconInputDragRight = iconInputDragRight;
            holder.iconImageView = iconImage;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.restore();
        boolean isLeftMode = SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT;
        holder.setInfo(resolveInfoGroup, mDragEvent != null, isLeftMode);
        Utils.setAlwaysCanAcceptDrag(holder.view, true);
        holder.view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
//                        log.d("ACTION_DRAG_ENTERED");
                        FloatText.show(mContext, holder.view, holder.resolveInfoGroup.getDisplayName().toString());
                        holder.view.animate().scaleX(SCALE_SIZE).scaleY(SCALE_SIZE)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setStartDelay(0)
                        .setDuration(100).start();
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
//                        log.d("ACTION_DRAG_EXITED");
                        FloatText.hide();
                        holder.view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
//                        log.d("ACTION_DRAG_LOCATION");
                        return true;
                    case DragEvent.ACTION_DROP:
//                        log.d("ACTION_DRAG_DROP");
                        holder.view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        return holder.resolveInfoGroup.handleDragEvent(mContext, event);
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
        return holder.view;
    }

    public int objectIndex(ResolveInfoGroup data) {
        if (mResolveInfos == null) {
            return -1;
        }
        return mResolveInfos.indexOf(data);
    }

    public static class ViewHolder {
        public View view;
        public Context context;
        public ImageView iconInputLeft;
        public ImageView iconInputRight;
        public ImageView iconInputDragLeft;
        public ImageView iconInputDragRight;
        public ImageView iconImageView;
        public Drawable icon;
        public ResolveInfoGroup resolveInfoGroup;
        public boolean isNewAdded = false;

        public void setInfo(ResolveInfoGroup info, boolean dragging, boolean isLeftMode) {
            resolveInfoGroup = info;
            if (info == null) {
                return;
            }
            iconImageView.setImageDrawable(resolveInfoGroup.loadIcon());
            if (dragging) {
                int offsetX = Constants.share_item_offset_x;
                if (isLeftMode) {
                    offsetX = -offsetX;
                }
                iconImageView.setTranslationX(offsetX);
                iconImageView.setTranslationY(Constants.share_item_offset_y);
                iconImageView.setScaleX(0.8f);
                iconImageView.setScaleY(0.8f);
            } else {
                iconImageView.setTranslationX(0);
                iconImageView.setTranslationY(0);
                iconImageView.setScaleX(1);
                iconImageView.setScaleY(1);
            }
            if (isLeftMode) {
                iconInputLeft.setVisibility(View.INVISIBLE);
                iconInputDragLeft.setVisibility(View.INVISIBLE);
                if (dragging) {
                    iconInputRight.setVisibility(View.INVISIBLE);
                    iconInputDragRight.setVisibility(View.VISIBLE);
                } else {
                    iconInputDragRight.setVisibility(View.INVISIBLE);
                    iconInputRight.setVisibility(View.VISIBLE);
                }
            } else {
                iconInputRight.setVisibility(View.INVISIBLE);
                iconInputDragRight.setVisibility(View.INVISIBLE);
                if (dragging) {
                    iconInputLeft.setVisibility(View.INVISIBLE);
                    iconInputDragLeft.setVisibility(View.VISIBLE);
                } else {
                    iconInputDragLeft.setVisibility(View.INVISIBLE);
                    iconInputLeft.setVisibility(View.VISIBLE);
                }
            }
        }

        public void restore(){
            if (view.getVisibility() == View.INVISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
            view.setTranslationY(0);
        }
    }
}
