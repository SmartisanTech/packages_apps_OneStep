package com.smartisanos.sidebar.action;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.view.SidebarRootView;
import com.smartisanos.sidebar.view.SidebarRootView.DragItem;
import com.smartisanos.sidebar.view.Trash;

public class UninstallAction {
    private static final LOG log = LOG.getInstance(UninstallAction.class);

    private Context mContext;
    private DragItem mDragItem;
    private AlertDialog mUninstallDialog;
    private boolean isCancelRun = false;

    public UninstallAction(Context context, DragItem dragItem) {
        mContext = context;
        mDragItem = dragItem;
    }

    public void dismissDialog() {
        if (mUninstallDialog != null) {
            mUninstallDialog.dismiss();
        }
    }

    public void showUninstallDialog() {
        if(mUninstallDialog != null && mUninstallDialog.isShowing()){
            return ;
        }
        String content = mContext.getString(R.string.uninstall_app_dialog_text);
        content = String.format(content, mDragItem.displayName);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.uninstall_app_dialog_title);
        builder.setMessage(content);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDragItem.delelte();
                uninstallAnim();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    isCancelRun = true;
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                    isCancelRun = true;
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mUninstallDialog = null;
                if(isCancelRun) {
                    isCancelRun = false;
                    cancelAction();
                }
            }
        });
        mUninstallDialog = builder.create();
        SidebarRootView view = SidebarController.getInstance(mContext).getSidebarRootView();
        mUninstallDialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
        mUninstallDialog.getWindow().getAttributes().token = view.getWindowToken();
        mUninstallDialog.show();
    }

    private void cancelAction() {
        SidebarRootView rootView = SidebarController.getInstance(mContext).getSidebarRootView();
        Trash trash = rootView.getTrash();
        if (trash == null) {
            log.error("cancelAction failed by trash is null");
            return;
        }
        trash.stopRock();
        rootView.dropDrag();
    }

    private void uninstallAnim() {
        SidebarRootView rootView = SidebarController.getInstance(mContext).getSidebarRootView();
        if (rootView == null) {
            log.error("uninstallAnim failed by rootView is null");
            return;
        }
        Trash trash = rootView.getTrash();
        if (trash == null) {
            log.error("uninstallAnim failed by trash is null");
            return;
        }
        trash.stopRock();
        rootView.deleteDrag();
    }
}