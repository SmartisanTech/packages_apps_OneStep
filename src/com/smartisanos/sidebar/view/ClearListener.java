package com.smartisanos.sidebar.view;

import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import smartisanos.app.MenuDialog;

import com.smartisanos.sidebar.R;

public class ClearListener implements View.OnClickListener {
    private Runnable action;
    private int mTitleResId;
    public ClearListener(Runnable action, int titleResId) {
        this.action = action;
        mTitleResId = titleResId;
    }

    @Override
    public void onClick(View v) {
        MenuDialog dialog = new MenuDialog(v.getContext());
        dialog.setTitle(mTitleResId);
        dialog.setPositiveButton(R.string.clear, new OnClickListener() {
            @Override
            public void onClick(View v) {
                action.run();
            }
        });
        dialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
        dialog.getWindow().getAttributes().token = v.getWindowToken();
        dialog.show();
    }
}
