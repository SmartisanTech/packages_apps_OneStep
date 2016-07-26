package com.smartisanos.sidebar;

import java.io.File;
import java.net.URI;

import smartisanos.app.SmartisanProgressDialog;

import com.smartisanos.sidebar.util.SidebarItem;
import com.smartisanos.sidebar.util.Utils;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.DragEvent;
import android.view.WindowManager;
import android.widget.Toast;

public class PendingDragEventTask {
    private static final int CHECK_GAP = 1000;
    private static final int MAX_TIMES = 20;

    private Context mContext;
    private DragEvent mPendingEvent;
    private SidebarItem mSidebarItem;
    private SmartisanProgressDialog mDialog;
    private Handler mHandler;
    private int mTimes = 0;

    public static boolean tryPending(Context context, DragEvent event,
            SidebarItem item) {
        if (isPendingData(event)) {
            new PendingDragEventTask(context, DragEvent.obtain(event), item)
                    .start();
            return true;
        }
        return false;
    }

    private PendingDragEventTask(Context context, DragEvent event,
            SidebarItem item) {
        mContext = context;
        mPendingEvent = event;
        mSidebarItem = item;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mContext.registerReceiver(mBroadcastReceiver, filter);

        mDialog = new SmartisanProgressDialog(mContext);
        mDialog.setMessage(R.string.pending_ongoing);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mHandler.removeCallbacks(mCheckPending);
            }
        });
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                showToast(R.string.pending_cancel);
            }
        });
        mDialog.getWindow().setCloseOnTouchOutside(false);
        mDialog.getWindow().getAttributes().flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mDialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mDialog.getWindow().getAttributes().isEatHomeKey = true;
        mDialog.show();
        mTimes = 0;
        mHandler.post(mCheckPending);
    }

    private static boolean isPendingData(DragEvent event) {
        ClipData cd = event.getClipData();
        if (cd == null) {
            return false;
        }
        for (int i = 0; i < cd.getItemCount(); ++i) {
            Uri uri = cd.getItemAt(i).getUri();
            if (uri != null && "file".equals(uri.getScheme())) {
                File file = new File(uri.getPath());
                if (!file.exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showToast(int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
    }

    private Runnable mCheckPending = new Runnable() {
        @Override
        public void run() {
            mTimes++;
            if (mTimes > MAX_TIMES) {
                mDialog.dismiss();
                showToast(R.string.pending_fail);
                return;
            }
            if (!mDialog.isShowing()) {
                return;
            }
            if (!isPendingData(mPendingEvent)) {
                mDialog.dismiss();
                mSidebarItem.handleDragEvent(mContext, mPendingEvent);
            } else {
                mHandler.postDelayed(mCheckPending, CHECK_GAP);
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra("reason");
                if ("eathomekey".equals(reason)) {
                    mDialog.cancel();
                    mContext.unregisterReceiver(mBroadcastReceiver);
                }
            }
        }
    };
}
