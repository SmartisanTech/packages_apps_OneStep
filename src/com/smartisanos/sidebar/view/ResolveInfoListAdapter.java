package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimInterpolator;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;

public class ResolveInfoListAdapter extends DragEventAdapter {
    private static final LOG log = LOG.getInstance(ResolveInfoListAdapter.class);
    private static final float SCALE_SIZE = 1.4f;

    private Context mContext;
    private List<ResolveInfoGroup> mResolveInfos;
    private List<ResolveInfoGroup> mAcceptableResolveInfos = new ArrayList<ResolveInfoGroup>();
    private DragEvent mDragEvent;
    private ResolveInfoManager mManager;

    public ResolveInfoListAdapter(Context context) {
        mContext = context;
        mManager = ResolveInfoManager.getInstance(context);
        mResolveInfos = mManager.getAddedResolveInfoGroup();
        mAcceptableResolveInfos.addAll(mResolveInfos);
        mManager.addListener(resolveInfoUpdateListener);
    }

    private ResolveInfoManager.ResolveInfoUpdateListener resolveInfoUpdateListener = new ResolveInfoManager.ResolveInfoUpdateListener() {
        @Override
        public void onUpdate() {
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    mResolveInfos = mManager.getAddedResolveInfoGroup();
                    updateAcceptableResolveInfos();
                }
            });
        }
    };

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
            ImageView iconImage = (ImageView) view.findViewById(R.id.shareitemimageview);
            holder = new ViewHolder();
            holder.view = view;
            holder.context = mContext;
            holder.iconInputLeft = iconInputLeft;
            holder.iconInputRight = iconInputRight;
            holder.iconImageView = iconImage;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.restore();
        holder.setInfo(resolveInfoGroup, mDragEvent != null);
        holder.updateIconFlag(SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT);
        Utils.setAlwaysCanAcceptDrag(holder.view, true);
        holder.view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        log.d("ACTION_DRAG_ENTERED");
                        holder.view.animate().scaleX(SCALE_SIZE).scaleY(SCALE_SIZE)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setStartDelay(0)
                        .setDuration(100).start();
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        log.d("ACTION_DRAG_EXITED");
                        holder.view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        return true;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        log.d("ACTION_DRAG_LOCATION");
                        return true;
                    case DragEvent.ACTION_DROP:
                        log.d("ACTION_DRAG_DROP");
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
        public ImageView iconImageView;
        public Drawable icon;
        public ResolveInfoGroup resolveInfoGroup;
        public boolean isNewAdded = false;

        public void setInfo(ResolveInfoGroup info, boolean color) {
            resolveInfoGroup = info;
            if (info == null) {
                return;
            }
            if (color) {
                iconImageView.setImageDrawable(resolveInfoGroup.loadIcon());
            } else {
                iconImageView.setImageBitmap(resolveInfoGroup.loadBlackWhiteIcon());
            }
        }

        public void updateIconFlag(boolean showLeft) {
            if (showLeft) {
                iconInputLeft.setVisibility(View.INVISIBLE);
                iconInputRight.setVisibility(View.VISIBLE);
            } else {
                iconInputLeft.setVisibility(View.VISIBLE);
                iconInputRight.setVisibility(View.INVISIBLE);
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
