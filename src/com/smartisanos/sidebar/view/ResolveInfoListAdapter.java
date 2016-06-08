package com.smartisanos.sidebar.view;

import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;
import com.smartisanos.sidebar.util.SidebarAdapter;
import com.smartisanos.sidebar.util.Utils;

import smartisanos.util.SidebarUtils;

public class ResolveInfoListAdapter extends SidebarAdapter {
    private static final String TAG = ResolveInfoListAdapter.class.getName();

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
    public View getView(final int position, View convertView,
            ViewGroup parent) {
        final View ret;
        if (convertView == null) {
            ret = LayoutInflater.from(mContext).inflate(R.layout.shareitem, null);
        }else{
            ret = convertView;
        }
        final ResolveInfoGroup rig = mAcceptableResolveInfos.get(position);
        final ImageView iv = (ImageView) ret.findViewById(R.id.shareitemimageview);
        final Drawable icon = rig.loadIcon(mContext.getPackageManager());
        iv.setImageBitmap(BitmapUtils.convertToBlackWhite(icon));
        if(SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT){
            ret.findViewById(R.id.icon_input_left).setVisibility(View.INVISIBLE);
            ret.findViewById(R.id.icon_input_right).setVisibility(View.VISIBLE);
        }else{
            ret.findViewById(R.id.icon_input_left).setVisibility(View.VISIBLE);
            ret.findViewById(R.id.icon_input_right).setVisibility(View.INVISIBLE);
        }
        ret.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        ret.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    boolean accept = rig.accpetDragEvent(mContext, event);
                    if(accept){
                        iv.setImageDrawable(icon);
                    }
                    Log.d(TAG,"ACTION_DRAG_STARTED, accpet -> " + accept);
                    return accept;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "ACTION_DRAG_ENTERED");
                    ret.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "ACTION_DRAG_EXITED");
                    ret.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    Log.d(TAG, "ACTION_DRAG_LOCATION");
                    return true;
                case DragEvent.ACTION_DROP:
                    Log.d(TAG, "ACTION_DRAG_DROP");
                    ret.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    boolean ret =  rig.handleEvent(mContext, event);
                    if(ret){
                        Utils.dismissAllDialog(mContext);
                    }
                    return ret;
                case DragEvent.ACTION_DRAG_ENDED:
                    iv.setImageBitmap(BitmapUtils.convertToBlackWhite(icon));
                    return true;
                }
                return false;
            }
        });
        return ret;
    }
}
