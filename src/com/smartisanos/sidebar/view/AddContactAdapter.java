package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.AddContactManager;
import com.smartisanos.sidebar.util.AddContactManager.AddContactItem;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;

public class AddContactAdapter extends BaseAdapter {
    private static final String TAG = AddContactAdapter.class.getName();

    private Context mContext;
    private AddContactManager mManager;
    private List<AddContactItem> mItemList = new ArrayList<AddContactItem>();

    public AddContactAdapter(Context context) {
        mContext = context;
        mManager = AddContactManager.getInstance(mContext);
        mItemList.addAll(mManager.getList());
        mManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateData();
                    }
                });
            }
        });
    }

    private void updateData(){
        mItemList.clear();
        mItemList.addAll(mManager.getList());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View ret = convertView;
        if (ret == null) {
            ret = LayoutInflater.from(mContext).inflate(R.layout.add_contact_item, null);
        }

        ImageView iv = (ImageView) ret.findViewById(R.id.icon);
        iv.setImageResource(mItemList.get(position).iconId);
        TextView tv = (TextView) ret.findViewById(R.id.label);
        tv.setText(mItemList.get(position).labelId);
        ret.setOnClickListener(mItemList.get(position).mListener);
        return ret;
    }
}