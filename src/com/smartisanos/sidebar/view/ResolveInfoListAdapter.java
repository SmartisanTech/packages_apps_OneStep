package com.smartisanos.sidebar.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;
import com.smartisanos.sidebar.util.SidebarAdapter;
import com.smartisanos.sidebar.util.Utils;

import smartisanos.util.SidebarUtils;

public class ResolveInfoListAdapter extends SidebarAdapter {
    private static final LOG log = LOG.getInstance(ResolveInfoListAdapter.class);

    private Context mContext;
    private List<ResolveInfoGroup> mResolveInfos;
    private List<ResolveInfoGroup> mAcceptableResolveInfos = new ArrayList<ResolveInfoGroup>();
    private ResolveInfoManager mManager;
    public ResolveInfoListAdapter(Context context) {
        mContext = context;
        mManager = ResolveInfoManager.getInstance(context);
        mResolveInfos = mManager.getAddedResolveInfoGroup();
        mAcceptableResolveInfos.addAll(mResolveInfos);
        mManager.addListener(new ResolveInfoManager.ResolveInfoUpdateListener() {
            @Override
            public void onUpdate() {
                mResolveInfos = mManager.getAddedResolveInfoGroup();
                updateAcceptableResolveInfos();
            }
        });
    }

    private void updateAcceptableResolveInfos() {
        mAcceptableResolveInfos.clear();
        for (ResolveInfoGroup rig : mResolveInfos) {
            if (rig.accpetDragEvent(mContext, getDragEvent())) {
                mAcceptableResolveInfos.add(rig);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void setDragEvent(DragEvent event) {
        super.setDragEvent(event);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        ResolveInfoGroup resolveInfoGroup = mResolveInfos.get(position);
        if (convertView == null || !(convertView instanceof RelativeLayout)) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.shareitem, null);
            ImageView iconInputLeft = (ImageView) view.findViewById(R.id.icon_input_left);
            ImageView iconInputRight = (ImageView) view.findViewById(R.id.icon_input_right);
            ImageView iconImage = (ImageView) view.findViewById(R.id.shareitemimageview);
            Drawable icon = resolveInfoGroup.loadIcon(mContext.getPackageManager());
            iconImage.setImageBitmap(BitmapUtils.convertToBlackWhite(icon));
            holder = new ViewHolder();
            holder.view = view;
            holder.iconInputLeft = iconInputLeft;
            holder.iconInputRight = iconInputRight;
            holder.iconImageView = iconImage;
            holder.icon = icon;
            holder.resolveInfoGroup = resolveInfoGroup;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.resolveInfoGroup = resolveInfoGroup;
            if (holder.view.getVisibility() == View.INVISIBLE) {
                holder.view.setVisibility(View.VISIBLE);
            }
        }
        holder.updateIconFlag(SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT);
        holder.view.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        boolean accept = holder.resolveInfoGroup.accpetDragEvent(mContext, event);
                        if(accept){
                            holder.iconImageView.setImageDrawable(holder.resolveInfoGroup.loadIcon(mContext.getPackageManager()));
                        }
                        log.d("ACTION_DRAG_STARTED, accpet -> " + accept);
                        return accept;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        log.d("ACTION_DRAG_ENTERED");
                        holder.view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
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
                        boolean ret =  holder.resolveInfoGroup.handleEvent(mContext, event);
                        if(ret){
                            Utils.dismissAllDialog(mContext);
                        }
                        return ret;
                    case DragEvent.ACTION_DRAG_ENDED:
                        holder.iconImageView.setImageBitmap(BitmapUtils.convertToBlackWhite(holder.resolveInfoGroup.loadIcon(mContext.getPackageManager())));
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
        public ImageView iconInputLeft;
        public ImageView iconInputRight;
        public ImageView iconImageView;
        public Drawable icon;
        public ResolveInfoGroup resolveInfoGroup;

        public void updateIconFlag(boolean showLeft) {
            if (showLeft) {
                iconInputLeft.setVisibility(View.INVISIBLE);
                iconInputRight.setVisibility(View.VISIBLE);
            } else {
                iconInputLeft.setVisibility(View.VISIBLE);
                iconInputRight.setVisibility(View.INVISIBLE);
            }
        }
    }
}
