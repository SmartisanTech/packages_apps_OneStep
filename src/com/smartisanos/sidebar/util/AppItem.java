package com.smartisanos.sidebar.util;

import java.util.Comparator;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.DragEvent;

public class AppItem implements SidebarItem {

    private Context mContext;
    private int mIndex;
    public final ResolveInfo mResolveInfo;

    public AppItem(Context context, ResolveInfo ri) {
        mContext = context;
        mResolveInfo = ri;
    }

    @Override
    public CharSequence getDisplayName() {
        return mResolveInfo.loadLabel(mContext.getPackageManager());
    }

    @Override
    public Bitmap getAvatar() {
        return BitmapUtils.drawableToBitmap(mResolveInfo.loadIcon(mContext.getPackageManager()));
    }

    @Override
    public void setIndex(int index) {
        mIndex = index;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public void delete() {
        AppManager.getInstance(mContext).removeAppItem(this);
    }

    @Override
    public boolean acceptDragEvent(Context context, DragEvent event) {
        // NA
        return false;
    }
    @Override
    public boolean handleDragEvent(Context context, DragEvent event) {
        // NA
        return false;
    }

    @Override
    public boolean openUI(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(
                mResolveInfo.activityInfo.packageName,
                mResolveInfo.activityInfo.name));
        mContext.startActivity(intent);
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AppItem)) {
            return false;
        }
        AppItem other = (AppItem) o;
        if (mResolveInfo.activityInfo.packageName
                .equals(other.mResolveInfo.activityInfo.packageName)
                && mResolveInfo.activityInfo.name
                        .equals(other.mResolveInfo.activityInfo.name)) {
            return true;
        }
        return super.equals(o);
    }

    public String getPackageName() {
        return mResolveInfo.activityInfo.packageName;
    }

    public String getComponentName() {
        return mResolveInfo.activityInfo.name;
    }

    public static AppItem fromData(Context context, String pkgName, String componentName) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(componentName)) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(pkgName);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        if (list != null) {
            for (int i = 0; i < list.size(); ++i) {
                ResolveInfo ri = list.get(i);
                if (ri.activityInfo.name.equals(componentName)) {
                    return new AppItem(context, ri);
                }
            }
        }
        return null;
    }

    public static class IndexComparator implements Comparator<AppItem> {
        @Override
        public int compare(AppItem lhs, AppItem rhs) {
            if (lhs.getIndex() > rhs.getIndex()) {
                return -1;
            }
            if (lhs.getIndex() < rhs.getIndex()) {
                return 1;
            }
            return 0;
        }
    }
}
