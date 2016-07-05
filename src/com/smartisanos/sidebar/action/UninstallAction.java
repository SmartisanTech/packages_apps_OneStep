package com.smartisanos.sidebar.action;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.view.SidebarRootView;
import com.smartisanos.sidebar.view.Trash;

public class UninstallAction {
    private static final LOG log = LOG.getInstance(UninstallAction.class);

    private Context mContext;
    private String mName;

    private static volatile boolean uninstallAppRunning = false;
    public static AlertDialog mUninstallDialog = null;

    public UninstallAction(Context context, String name) {
        mContext = context;
        mName = name;
    }

    private boolean isCancelRun = false;

    public void showUninstallDialog() {
        String content = mContext.getString(R.string.uninstall_app_dialog_text);
        content = String.format(content, mName);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle(R.string.uninstall_app_dialog_title);
        builder.setMessage(content);

        builder.setPositiveButton(R.string.uninstall_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (uninstallAppRunning) {
                    return;
                }
                uninstallAnim();
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
                    cancelAction();
                }
            }
        });
        mUninstallDialog = builder.create();
        SidebarController controller = SidebarController.getInstance(mContext);
        SidebarRootView view = controller.getSidebarRootView();
        mUninstallDialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
        mUninstallDialog.getWindow().getAttributes().token = view.getWindowToken();
        mUninstallDialog.show();
    }

    private void cancelAction() {
        SidebarController controller = SidebarController.getInstance(mContext);
        if (controller == null) {
            log.error("cancelAction failed by controller is null");
            return;
        }
        SidebarRootView rootView = controller.getSidebarRootView();
        if (rootView == null) {
            log.error("cancelAction failed by rootView is null");
            return;
        }
        Trash trash = rootView.getTrash();
        if (trash == null) {
            log.error("cancelAction failed by trash is null");
            return;
        }
        trash.stopRock();
        trash.trashDisappearWithAnim();
        rootView.dropDrag();
    }

    private void uninstallAnim() {
        SidebarController controller = SidebarController.getInstance(mContext);
        if (controller == null) {
            log.error("uninstallAnim failed by controller is null");
            return;
        }
        final SidebarRootView rootView = controller.getSidebarRootView();
        if (rootView == null) {
            log.error("uninstallAnim failed by rootView is null");
            return;
        }
        final Trash trash = rootView.getTrash();
        if (trash == null) {
            log.error("uninstallAnim failed by trash is null");
            return;
        }
        trash.stopRock();
        SidebarRootView.DragView dragView = rootView.getDraggedView();
        View view = dragView.mView;
        Vector3f from = new Vector3f(0, view.getY());
        Vector3f to = new Vector3f(0, trash.mWindowHeight);
        Anim anim = new Anim(view, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete() {
                trash.trashDisappearWithAnim();
                rootView.dropDrag();
            }
        });
        anim.start();
    }
}