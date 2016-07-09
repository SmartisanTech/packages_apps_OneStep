package com.smartisanos.sidebar.view;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;

public class AddResolveInfoGroupAdapter extends BaseAdapter{
    private Context mContext;
    private ResolveInfoManager mManager;
    private List<ResolveInfoGroup> mInfos;
    private Handler mHandler;
    private View mView;

    public AddResolveInfoGroupAdapter(Context context, View view){
        mContext = context;
        mView = view;
        mManager = ResolveInfoManager.getInstance(mContext);
        mInfos = mManager.getUnAddedResolveInfoGroup();
        mHandler = new Handler(Looper.getMainLooper());
        mManager.addListener(new ResolveInfoManager.ResolveInfoUpdateListener() {
            @Override
            public void onUpdate() {
                postUpdate();
            }
        });
        postUpdate();
    }

    private void postUpdate() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mInfos = mManager.getUnAddedResolveInfoGroup();
                notifyDataSetChanged();
                if (mView != null) {
                    if (mInfos.size() <= 0) {
                        mView.setVisibility(View.GONE);
                    } else {
                        mView.setVisibility(View.VISIBLE);
                    }
                }
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View ret = convertView;
        ViewHolder vh;
        if (ret == null) {
            ret = LayoutInflater.from(mContext).inflate(R.layout.toadd_resolveinfo_group_item, null);
            vh = new ViewHolder();
            vh.iv = (ImageView) ret.findViewById(R.id.icon);
            vh.tv = (TextView) ret.findViewById(R.id.label);
            ret.setTag(vh);
        } else {
            vh = (ViewHolder) ret.getTag();
        }
        final ResolveInfoGroup rig = mInfos.get(position);
        vh.iv.setImageDrawable(rig.loadIcon(mContext.getPackageManager()));
        vh.tv.setText(rig.getDisplayName());
        ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResolveInfoManager.getInstance(mContext).addResolveInfoGroup(rig);
            }
        });
        return ret;
    }

    class ViewHolder {
        public ImageView iv;
        public TextView tv;
    }
}
