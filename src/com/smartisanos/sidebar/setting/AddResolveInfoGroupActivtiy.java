package com.smartisanos.sidebar.setting;

import smartisanos.widget.SettingItemSwitch;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarStatus;
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
            SettingItemSwitch view = null;
            if(convertView == null) {
                view =  new SettingItemSwitch(mContext);
            } else {
                view = (SettingItemSwitch) convertView;
            }

            /**
            view.setOnCheckedChangeListener(null);
            view.setChecked(settingItem.mChecked);
            view.getSwitch().setId(settingItem.mId);
            view.setOnCheckedChangeListener(Settings.this);
            view.setId(settingItem.mId);
            view.setBackgroundResource(settingItem.mBgRes);
            view.setTitle(settingItem.mTitleRes);
            view.setIconResource(settingItem.mIconRes);
            **/
            view.setOnCheckedChangeListener(null);
            view.setChecked(item.checked);
            view.setOnCheckedChangeListener(new OnCheckedChangeListener() {
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

            view.setTitle(item.rig.getDisplayName());
            view.setBackgroundResource(R.drawable.common_icon_picker_bg_middle);
            return view;
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
