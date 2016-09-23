package com.smartisanos.sidebar.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;

import smartisanos.widget.SwitchEx;

public final class AddResolveInfoGroupAdapter extends BaseAdapter {

    private Context mContext;
    private List<Item> mList = new ArrayList<Item>();

    public AddResolveInfoGroupAdapter(Context context) {
        mContext = context;
        refreshData();
    }

    private ResolveInfoManager.ResolveInfoUpdateListener mUpdateListener = new ResolveInfoManager.ResolveInfoUpdateListener() {
        @Override
        public void onUpdate() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    onDataChange();
                }
            });
        }
    };

    private void onDataChange() {
        for (Item item : mList) {
            boolean added = ResolveInfoManager.getInstance(mContext).isResolveInfoGroupAdded(item.rig);
            item.checked = added;
        }
        notifyDataSetChanged();
    }

    public void onStart() {
        refreshData();
        notifyDataSetChanged();
        ResolveInfoManager.getInstance(mContext).addListener(mUpdateListener);
    }

    public void onStop() {
        ResolveInfoManager.getInstance(mContext).removeListener(mUpdateListener);
    }

    public void refreshData() {
        mList.clear();
        for (ResolveInfoGroup rig : ResolveInfoManager
                .getInstance(mContext).getAddedResolveInfoGroup()) {
            mList.add(new Item(rig, true));
        }
        for (ResolveInfoGroup rig : ResolveInfoManager
                .getInstance(mContext).getUnAddedResolveInfoGroup()) {
            mList.add(new Item(rig, false));
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Item item = mList.get(position);
        View view = convertView;
        ViewHolder vh = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.setting_item_switch_layout, null);
            vh = new ViewHolder();
            vh.iconView = view.findViewById(R.id.item_icon);
            vh.titleView = (TextView) view.findViewById(R.id.item_title);
            vh.switchView = (SwitchEx) view.findViewById(R.id.item_switch);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }
        vh.iconView.setBackground(item.rig.loadIcon());
        vh.titleView.setText(item.rig.getDisplayName());
        vh.switchView.setOnCheckedChangeListener(null);
        vh.switchView.setChecked(item.checked);
        vh.switchView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                item.checked = isChecked;
                if(isChecked) {
                    ResolveInfoManager.getInstance(mContext).addResolveInfoGroup(item.rig);
                } else {
                    ResolveInfoManager.getInstance(mContext).delete(item.rig);
                }
            }
        });
        return view;
    }

    final class ViewHolder {
        public View iconView;
        public TextView titleView;
        public SwitchEx switchView;
    }

    private final class Item {
        public ResolveInfoGroup rig;
        public boolean checked;

        public Item(ResolveInfoGroup rig, boolean checked) {
            this.rig = rig;
            this.checked = checked;
        }
    }
}