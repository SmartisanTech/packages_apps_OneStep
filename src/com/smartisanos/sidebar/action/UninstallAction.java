package com.smartisanos.sidebar.action;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.R;

public class UninstallAction {
    private static final LOG log = LOG.getInstance(UninstallAction.class);

    private Context mContext;
    private String name;

    private static volatile boolean uninstallAppRunning = false;
    public static AlertDialog mUninstallDialog = null;

    public UninstallAction(Context context) {
        mContext = context;
    }

    private boolean isCancelRun = false;

    public void showUninstallDialog() {
        String content = mContext.getString(R.string.uninstall_app_dialog_text);
        content = String.format(content, name);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.uninstall_app_dialog_title);
        builder.setMessage(content);

        builder.setPositiveButton(R.string.uninstall_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (uninstallAppRunning) {
                    return;
                }
            }
        });
        builder.setNegativeButton(R.string.uninstall_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!uninstallAppRunning) {
                    isCancelRun = true;
                }
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(!uninstallAppRunning) {
                    isCancelRun = true;
                }
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                uninstallAppRunning = false;
                mUninstallDialog = null;
                if(isCancelRun) {
                    isCancelRun = false;
//                    cancelAction(dialogType);
                }
            }
        });
        mUninstallDialog = builder.create();
        mUninstallDialog.show();
    }

}