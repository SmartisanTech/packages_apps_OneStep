package com.smartisanos.sidebar.util;

import java.util.List;

import com.smartisanos.sidebar.PendingDragEventTask;

import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.view.DragEvent;

public class OngoingItem implements SidebarItem {

    private ComponentName mName;
    private int mToken;
    private int mPendingNumbers;
    private CharSequence mTitle;
    private int mPid;
    public OngoingItem(ComponentName name, int token, int pendingNumbers,
            CharSequence title, int pid) {
        mName = name;
        mToken = token;
        mPendingNumbers = pendingNumbers;
        mTitle = title;
        mPid = pid;
    }

    public int getPid() {
        return mPid;
    }

    public int getPendingNumbers() {
        return mPendingNumbers;
    }

    @Override
    public CharSequence getDisplayName() {
        return mTitle;
    }

    @Override
    public void delete() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean acceptDragEvent(Context context, DragEvent event) {
        if (event == null) {
            return true;
        }
        if (event.getClipDescription().getMimeTypeCount() <= 0) {
            return false;
        }

        String mimeType = event.getClipDescription().getMimeType(0);
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)) {
            Intent sharingIntent = new Intent(
                    android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(sharingIntent, 0);
            if (infos != null) {
                for (ResolveInfo ri : infos) {
                    ComponentName cn = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
                    if (mName.equals(cn)) {
                        return true;
                    }
                }
            }
        } else {
            for (String action : ResolveInfoManager.ACTIONS) {
                Intent intent = new Intent(action);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setType(mimeType);
                intent.setPackage(mName.getPackageName());
                List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, 0);
                if (infos != null) {
                    for (ResolveInfo ri : infos) {
                        ComponentName cn = new ComponentName(
                                ri.activityInfo.packageName,
                                ri.activityInfo.name);
                        if (mName.equals(cn)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean handleDragEvent(Context context, DragEvent event) {
        boolean isPending = PendingDragEventTask.tryPending(context, event,
                this);
        if (isPending) {
            return true;
        }

        if (event.getClipData().getItemCount() <= 0
                || event.getClipDescription() == null
                || event.getClipDescription().getMimeTypeCount() <= 0) {
            return false;
        }

        String mimeType = event.getClipDescription().getMimeType(0);
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)
                && !TextUtils.isEmpty(event.getClipData().getItemAt(0).getText())) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setPackage(mName.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_TEXT, event.getClipData().getItemAt(0)
                    .getText());
            intent.setComponent(mName);
            Utils.dismissAllDialog(context);
            start(context, intent);
            return true;
        } else {
            if (event.getClipData().getItemAt(0).getUri() == null) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setComponent(mName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM,
                    event.getClipData().getItemAt(0).getUri());
            Utils.dismissAllDialog(context);
            start(context, intent);
            return true;
        }
    }

    public boolean isSameItem(ComponentName name, int token) {
        return mToken == token && mName.equals(name);
    }

    public void setPendingNumbers(int pendingNumbers) {
        mPendingNumbers = pendingNumbers;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public void start(Context context, Intent extraIntent){
        Intent realIntent = new Intent();
        realIntent.setComponent(mName);
        realIntent.putExtra("token", mToken);
        realIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        if(extraIntent != null){
            realIntent.putExtra(Intent.EXTRA_INTENT, extraIntent);
        }
        context.startActivity(realIntent);
    }
}
