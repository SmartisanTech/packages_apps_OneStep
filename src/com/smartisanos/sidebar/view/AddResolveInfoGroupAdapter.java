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
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;

public class AddResolveInfoGroupAdapter extends BaseAdapter{
    private static final LOG log = LOG.getInstance(AddResolveInfoGroupAdapter.class);
    private Context mContext;
    private ResolveInfoManager mManager;
    private List<ResolveInfoGroup> mInfos;
    private Handler mHandler;
    private View mView;
    private AddItemViewGroup mViewGroup;

    public AddResolveInfoGroupAdapter(Context context, AddItemViewGroup viewGroup, View view){
        mContext = context;
        mViewGroup = viewGroup;
        mView = view;
        mManager = ResolveInfoManager.getInstance(mContext);
        mInfos = mManager.getUnAddedResolveInfoGroup();
        mHandler = new Handler(Looper.getMainLooper());
        mManager.addListener(mResolveInfoUpdateListener);
        postUpdate();
    }

    private ResolveInfoManager.ResolveInfoUpdateListener mResolveInfoUpdateListener = new ResolveInfoManager.ResolveInfoUpdateListener() {
        @Override
        public void onUpdate() {
            postUpdate();
        }
    };

    private void postUpdate() {
        log.error("postUpdate !");
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
            ret = LayoutInflater.from(mContext).inflate(R.layout.add_resolve_item, null);
            vh = new ViewHolder();
            vh.iv = (ImageView) ret.findViewById(R.id.icon);
            vh.tv = (TextView) ret.findViewById(R.id.label);
            ret.setTag(vh);
            vh.view = ret;
        } else {
            vh = (ViewHolder) ret.getTag();
        }
        final ResolveInfoGroup rig = mInfos.get(position);
        vh.iv.setImageDrawable(rig.loadIcon(mContext.getPackageManager()));
        vh.tv.setText(rig.getDisplayName());
        final int index = position;
        ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewGroup.removeItemAtIndex(index, true);
            }
        });
        vh.restore();
        return ret;
    }

    class ViewHolder {
        public View view;
        public ImageView iv;
        public TextView tv;

        public void restore() {
            if (view == null) {
                return;
            }
            view.setTranslationX(0);
            view.setTranslationY(0);
            view.setScaleX(1);
            view.setScaleY(1);
            view.setAlpha(1);
        }
    }
}
