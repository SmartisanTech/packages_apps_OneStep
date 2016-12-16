package com.smartisanos.sidebar.setting;

import java.util.List;

import smartisanos.widget.SettingItemSwitch;
import smartisanos.widget.SettingItemText;
import smartisanos.widget.Title;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.onestep.OneStepManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.AddContactManager;
import com.smartisanos.sidebar.util.Tracker;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.AddContactManager.AddContactItem;

public class SettingActivity extends BaseActivity {
    public static final int BIT_SIDEBAR_IN_LEFT_TOP_MODE = 1 << 0;
    public static final int BIT_SIDEBAR_IN_RIGHT_TOP_MODE = 1 << 1;

    private Title mTitle;
    private SettingItemSwitch mSidebarSwitch;
    private SettingItemText mAddContact, mAddApp, mAddShare;

    private TextView mIntroText;
    private OneStepManager mOneStepManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        // recyle window background bitmap to release memory
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mOneStepManager = (OneStepManager) getSystemService(Context.ONE_STEP_SERVICE);
        mTitle = (Title) findViewById(R.id.title_bar);
        mTitle.getBackButton().setVisibility(View.INVISIBLE);
        mSidebarSwitch = (SettingItemSwitch) findViewById(R.id.sidebar_switch);
        mSidebarSwitch.setChecked(isSidebarEnable());
        mSidebarSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        Settings.Global.putInt(getContentResolver(),
                                Settings.Global.SIDE_BAR_MODE, isChecked ? 1 : 0);
                        setEnable(isChecked);
                        if(isChecked) {
                            enterSidebarMode();
                        }
                        Tracker.onClick(Tracker.EVENT_SWITCH, "status", isChecked ? "1" : "0");
                    }
                });

        mAddContact = (SettingItemText) findViewById(R.id.add_contact);
        mAddContact.setTitle(R.string.add_contact_to_sidebar);
        mAddContact.setIconResource(R.drawable.icon_add_contact);
        mAddContact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startAddContactActivity();
            }
        });

        mAddApp = (SettingItemText) findViewById(R.id.add_app);
        mAddApp.setTitle(R.string.add_app_to_sidebar);
        mAddApp.setIconResource(R.drawable.icon_add_app);
        mAddApp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startAddApplicationActivity();
            }
        });

        mAddShare = (SettingItemText) findViewById(R.id.add_share);
        mAddShare.setTitle(R.string.add_share_to_sidebar);
        mAddShare.setIconResource(R.drawable.icon_add_share);
        mAddShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startAddResolveInfoGroupActivtiy();
            }
        });

        mIntroText = (TextView) findViewById(R.id.introduction_link);
        mIntroText.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG );
        mIntroText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.smartisan.com/pr/#/video/onestep-Introduction");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
              getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
              int minHeight = findViewById(R.id.settings).getHeight() -  mTitle.getHeight();
              findViewById(R.id.setting_content).setMinimumHeight(minHeight);
            }
        });
        tryEnterSidebarMode();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tryEnterSidebarMode();
    }

    private void tryEnterSidebarMode() {
        if (isSidebarEnable() && !mOneStepManager.isInOneStepMode()) {
            // this means we enter one step mode due to user click on laucher
            Tracker.onClick(Tracker.EVENT_ONLAUNCH);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    enterSidebarMode();
                }
            }, 500);// waiting for window-animation finished !
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEnable(isSidebarEnable());
    }

    private void enterSidebarMode() {
        boolean left = false;
        mOneStepManager.requestEnterOneStepMode(left ? BIT_SIDEBAR_IN_LEFT_TOP_MODE
                        : BIT_SIDEBAR_IN_RIGHT_TOP_MODE);
    }

    private boolean isSidebarEnable() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.SIDE_BAR_MODE, 1) == 1;
    }

    private void setEnable(boolean enable) {
        mAddContact.setEnabled(enable);
        mAddApp.setEnabled(enable);
        mAddShare.setEnabled(enable);
    }

    private void startAddContactActivity() {
        Intent intent = new Intent();
        intent.setClassName("com.smartisanos.sidebar",
                "com.smartisanos.sidebar.setting.AddContactActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(smartisanos.widget.Title.EXTRA_BACK_BTN_TEXT,
                getResources().getString(R.string.back_text));
        startActivity(intent,false);
    }

    private void startAddApplicationActivity() {
        Intent intent = new Intent();
        intent.setClassName("com.smartisanos.sidebar",
                "com.smartisanos.sidebar.setting.AddApplicationActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(smartisanos.widget.Title.EXTRA_BACK_BTN_TEXT,
                getResources().getString(R.string.back_text));
        startActivity(intent,false);
    }

    private void startAddResolveInfoGroupActivtiy() {
        Intent intent = new Intent();
        intent.setClassName("com.smartisanos.sidebar",
                "com.smartisanos.sidebar.setting.AddResolveInfoGroupActivtiy");
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(smartisanos.widget.Title.EXTRA_BACK_BTN_TEXT,
                getResources().getString(R.string.back_text));
        startActivity(intent,false);
    }
}
