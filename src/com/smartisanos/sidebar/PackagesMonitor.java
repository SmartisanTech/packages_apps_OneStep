package com.smartisanos.sidebar;

import com.smartisanos.sidebar.util.ResolveInfoManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackagesMonitor extends BroadcastReceiver {
    private static final String TAG = PackagesMonitor.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getData().getSchemeSpecificPart();
        Log.d(TAG, "action -> " + action + " , packageName -> " + packageName);
        if(Intent.ACTION_PACKAGE_REMOVED.equals(action)){
            ResolveInfoManager.getInstance(context).onPackageRemoved(packageName);
        }else if(Intent.ACTION_PACKAGE_ADDED.equals(action)){
            ResolveInfoManager.getInstance(context).onPackageAdded(packageName);
        }
    }
}
