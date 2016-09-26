package com.smartisanos.sidebar.util;

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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;

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
        mMapPackageToItem.put(new AddContactItem(R.drawable.icon_add_ding, R.string.add_contact_dingding, mDingDingListener), DingDingContact.PKG_NAME);
        mMapPackageToItem.put(new AddContactItem(R.drawable.icon_add_sms, R.string.add_contact_mms, mMmsListener), MmsContact.PKG_NAME);
        mMapPackageToItem.put(new AddContactItem(R.drawable.icon_add_mail, R.string.add_contact_mail, mMailListener), MailContact.PKG_NAME);
        mMapPackageToItem.put(new AddContactItem(R.drawable.icon_add_wechat, R.string.add_contact_wechat, mWechatListener), WechatContact.PKG_NAME);
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
            intent.putExtra("can_choose_current_user", true);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder.setView(R.layout.wechat_hint)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Utils.dismissAllDialog(mContext);
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
                                        intent.setComponent(cmp);
                                        //intent.setPackage("com.tencent.mm");
                                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Log.d(TAG, "wechat not installed !", e);
                                    }
                                }
                            });
            AlertDialog dialog = builder.create();
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
