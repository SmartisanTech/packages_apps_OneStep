package com.smartisanos.sidebar.view;

import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import smartisanos.app.MenuDialog;

import com.smartisanos.sidebar.R;

public class ClearListener implements View.OnClickListener {
    private Runnable action;
    public ClearListener(Runnable action) {
        this.action = action;
    }

    @Override
    public void onClick(View v) {
        MenuDialog dialog = new MenuDialog(v.getContext());
        dialog.setTitle(R.string.title_confirm_delete_history);
        dialog.setPositiveButton(R.string.delete, new OnClickListener() {
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
