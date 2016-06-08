package com.smartisanos.sidebar.view;

import java.util.List;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ToAddResolveInfoGroupAdapter extends BaseAdapter{

    private Context mContext;
    private ResolveInfoManager mManager;
    private List<ResolveInfoGroup> mInfos;

    public ToAddResolveInfoGroupAdapter(Context context){
        mContext = context;
        mManager = ResolveInfoManager.getInstance(mContext);
        mInfos = mManager.getUnAddedResolveInfoGroup();
        mManager.addListener(new ResolveInfoManager.ResolveInfoUpdateListener() {
            @Override
            public void onUpdate() {
                mInfos = mManager.getUnAddedResolveInfoGroup();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return mInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View ret = convertView;
        if(ret == null){
            ret = LayoutInflater.from(mContext).inflate(R.layout.toadd_resolveinfo_group_item, null);
        }
        ImageView iv = (ImageView) ret.findViewById(R.id.icon);
        TextView tv = (TextView) ret.findViewById(R.id.label);
        iv.setImageDrawable(mInfos.get(position).loadIcon(mContext.getPackageManager()));
        tv.setText(mInfos.get(position).loadLabel(mContext.getPackageManager()));
        ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResolveInfoManager.getInstance(mContext).addResolveInfo(mInfos.get(position));
            }
        });
        return ret;
    }
}
