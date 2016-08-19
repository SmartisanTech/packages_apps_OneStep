package com.smartisanos.sidebar;

import com.smartisanos.sidebar.util.AddContactManager;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.ResolveInfoManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackagesMonitor extends BroadcastReceiver {
    private static final String TAG = PackagesMonitor.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // the context passed in is ReceiverRestrictedContext, don't use it
        context = context.getApplicationContext();
        String action = intent.getAction();
        String packageName = intent.getData().getSchemeSpecificPart();
        boolean replace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        if(Intent.ACTION_PACKAGE_REMOVED.equals(action)){
            if (!replace) {
                ResolveInfoManager.getInstance(context).onPackageRemoved(packageName);
                AddContactManager.getInstance(context).onPackageRemoved(packageName);
                ContactManager.getInstance(context).onPackageRemoved(packageName);
            }
        }else if(Intent.ACTION_PACKAGE_ADDED.equals(action)){
            if (!replace) {
                ResolveInfoManager.getInstance(context).onPackageAdded(packageName);
                AddContactManager.getInstance(context).onPackageAdded(packageName);
                ContactManager.getInstance(context).onPackageAdded(packageName);
            }
        }
    }
}
