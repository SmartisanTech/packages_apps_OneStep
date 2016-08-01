package com.smartisanos.sidebar.view;

import android.content.res.Configuration;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import smartisanos.app.MenuDialog;

import com.smartisanos.sidebar.R;

public class ClearListener implements View.OnClickListener {
    private Runnable action;
    private int mTitleResId;
    private MenuDialog mDialog;

    public ClearListener(Runnable action, int titleResId) {
        this.action = action;
        mTitleResId = titleResId;
    }

    @Override
    public void onClick(View v) {
        if (mDialog == null) {
            mDialog = new MenuDialog(v.getContext());
            mDialog.setTitle(mTitleResId);
            mDialog.setPositiveButton(R.string.clear, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    action.run();
                }
            });
            mDialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
            mDialog.getWindow().getAttributes().token = v.getWindowToken();
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        dismiss();
        mDialog = null;
    }
}
