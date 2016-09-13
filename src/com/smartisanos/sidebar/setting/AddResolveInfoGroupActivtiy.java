package com.smartisanos.sidebar.setting;


import smartisanos.widget.SwitchEx;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarStatus;
import com.smartisanos.sidebar.util.AppManager;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.ResolveInfoManager;

public class AddResolveInfoGroupActivtiy extends BaseActivity {
    private static final String TAG = AddResolveInfoGroupActivtiy.class.getName();

    private ListView mListView;
    private AddResolveInfoGroupAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AddApplicationActivity.onCreate()...");
        setContentView(R.layout.add_resolve_layout);
        getWindow().setBackgroundDrawable(null);
        setupBackBtnOnTitle();
        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new AddResolveInfoGroupAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.refreshData();
        SidebarController.getInstance(getApplicationContext()).requestStatus(
                SidebarStatus.UNNAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SidebarController.getInstance(getApplicationContext()).requestStatus(
                SidebarStatus.NORMAL);
    }

    private static final class AddResolveInfoGroupAdapter extends BaseAdapter {

        private Context mContext;
        private List<Item> mList = new ArrayList<Item>();

        public AddResolveInfoGroupAdapter(Context context) {
            mContext = context;
            refreshData();
            ResolveInfoManager.getInstance(mContext).addListener(mUpdateListener);
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

        private void refreshData() {
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
}
