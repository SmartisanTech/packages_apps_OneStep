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

import smartisanos.util.SidebarUtils;

public class ResolveInfoListAdapter extends BaseAdapter {
    private static final String TAG = ResolveInfoListAdapter.class.getName();

    private Context mContext;
    private List<ResolveInfoGroup> mResolveInfos;
    private ResolveInfoManager mManager;
    public ResolveInfoListAdapter(Context context) {
        mContext = context;
        mManager = ResolveInfoManager.getInstance(context);
        mResolveInfos = mManager.getAddedResolveInfoGroup();
        mManager.addListener(new ResolveInfoManager.ResolveInfoUpdateListener() {
            @Override
            public void onUpdate() {
                mResolveInfos = mManager.getAddedResolveInfoGroup();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return mResolveInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mResolveInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
            ViewGroup parent) {
        View ret = convertView;
        if (ret == null) {
            ret = LayoutInflater.from(mContext).inflate(R.layout.shareitem, null);
        }
        final ImageView iv = (ImageView) ret.findViewById(R.id.shareitemimageview);
        final Drawable icon = mResolveInfos.get(position).loadIcon(mContext.getPackageManager());
        iv.setImageBitmap(BitmapUtils.convertToBlackWhite(icon));
        if(SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT){
            ret.findViewById(R.id.icon_input_left).setVisibility(View.INVISIBLE);
            ret.findViewById(R.id.icon_input_right).setVisibility(View.VISIBLE);
        }else{
            ret.findViewById(R.id.icon_input_left).setVisibility(View.VISIBLE);
            ret.findViewById(R.id.icon_input_right).setVisibility(View.INVISIBLE);
        }
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        iv.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                ResolveInfo ri = mResolveInfos.get(position).get(0);
                switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    boolean accept = mResolveInfos.get(position).accpetDragEvent(mContext, event);
                    if(accept){
                        iv.setImageDrawable(icon);
                    }
                    Log.d(TAG,"ACTION_DRAG_STARTED, accpet -> " + accept);
                    return accept;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "ACTION_DRAG_ENTERED");
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d(TAG, "ACTION_DRAG_EXITED");
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    Log.d(TAG, "ACTION_DRAG_LOCATION");
                    return true;
                case DragEvent.ACTION_DROP:
                    boolean ret =  mResolveInfos.get(position).handleEvent(mContext, event);
                    if(ret){
                        SidebarController.getInstance(mContext).resumeTopView();
                        SidebarController.getInstance(mContext).dismissContent();
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

    public Set<ResolveInfo> getAllPackages(Context context) {
        Set<ResolveInfo> ret = new HashSet<ResolveInfo>();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : infos) {
            if (info.activityInfo != null) {
                ret.add(info);
            }
        }
        return ret;
    }
}
