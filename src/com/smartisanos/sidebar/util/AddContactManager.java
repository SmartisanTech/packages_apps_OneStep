package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartisanos.sidebar.R;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class AddContactManager extends DataManager {
    private static final String TAG = AddContactManager.class.getName();

    private volatile static AddContactManager sInstance;
    public synchronized static AddContactManager getInstance(Context context){
        if(sInstance == null){
            synchronized(AddContactManager.class){
                if(sInstance == null){
                    sInstance = new AddContactManager(context);
                }
            }
        }
        return sInstance;
    }

    private Map<AddContactItem, String> mMapPackageToItem;

    private Context mContext;
    private List<AddContactItem> mItemList = new ArrayList<AddContactItem>();

    private AddContactManager(Context context){
        mContext = context;
        mMapPackageToItem = new HashMap<AddContactItem, String>();
        mMapPackageToItem.put(new AddContactItem(R.drawable.icon_dingding, R.string.add_contact_dingding, mDingDingListener), "com.alibaba.android.rimet");
        mMapPackageToItem.put(new AddContactItem(R.drawable.icon_mms, R.string.add_contact_mms, mMmsListener), "com.android.contacts");
        mMapPackageToItem.put(new AddContactItem(R.drawable.icon_mail, R.string.add_contact_mail, mMailListener), "com.android.contacts");
        //mMapPackageToItem.put(new AddContactItem(R.drawable.icon_wechat, R.string.add_contact_wechat, mWechatListener), "com.tencent.mm");
        updateData();
    }

    public List<AddContactItem> getList() {
        List<AddContactItem> list = new ArrayList<AddContactItem>();
        synchronized (mItemList) {
            list.addAll(mItemList);
        }
        return list;
    }

    private void updateData() {
        List<AddContactItem> list = new ArrayList<AddContactItem>();
        for (Map.Entry<AddContactItem, String> entry : mMapPackageToItem.entrySet()) {
            if (Utils.isPackageInstalled(mContext, entry.getValue())) {
                list.add(entry.getKey());
            }
        }
        synchronized (mItemList) {
            mItemList.clear();
            mItemList.addAll(list);
        }
    }

    public void onPackageAdded(String packageName) {
        updateData();
        notifyListener();
    }

    public void onPackageRemoved(String packageName) {
        updateData();
        notifyListener();
    }

    private View.OnClickListener mDingDingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent("com.alibaba.android.rimet.ShortCutSelect");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try{
                Utils.dismissAllDialog(mContext);
                mContext.startActivity(intent);
            }catch(ActivityNotFoundException e){
                // TODO show dialog
            }
        }
    };

    private View.OnClickListener mWechatListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder.setMessage(R.string.add_wechat_hint)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
                                        intent.setComponent(cmp);
                                        //intent.setPackage("com.tencent.mm");
                                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                        Utils.dismissAllDialog(mContext);
                                    } catch (ActivityNotFoundException e) {
                                        Log.d(TAG, "wechat not installed !", e);
                                    }
                                }
                            });
            AlertDialog dialog = builder.create();
            dialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
            dialog.getWindow().getAttributes().token = v.getWindowToken();
            dialog.show();
        }
    };

    private View.OnClickListener mMmsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.smartisanos.sidebar", "com.smartisanos.sidebar.SelectContactActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try{
                Utils.dismissAllDialog(mContext);
                mContext.startActivity(intent);
            }catch(ActivityNotFoundException e){
                // NA
                Log.d(TAG, "not found !", e);
            }
        }
    };

    private View.OnClickListener mMailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.smartisanos.sidebar", "com.smartisanos.sidebar.SelectMailContactActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try{
                Utils.dismissAllDialog(mContext);
                mContext.startActivity(intent);
            }catch(ActivityNotFoundException e){
                // NA
                Log.d(TAG, "not found !", e);
            }
        }
    };

    public static class AddContactItem{
        public final int iconId;
        public final int labelId;
        public final View.OnClickListener mListener;
        public AddContactItem(int iconId, int labelId, View.OnClickListener listener){
            this.iconId = iconId;
            this.labelId = labelId;
            this.mListener = listener;
        }
    }
}
