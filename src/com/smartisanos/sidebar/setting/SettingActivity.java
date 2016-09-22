package com.smartisanos.sidebar.setting;

import java.util.List;

import smartisanos.util.SidebarUtils;
import smartisanos.widget.SettingItemSwitch;
import smartisanos.widget.SettingItemText;
import smartisanos.widget.Title;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.AddContactManager;
import com.smartisanos.sidebar.util.AddContactManager.AddContactItem;

public class SettingActivity extends BaseActivity {
    public static final int BIT_SIDEBAR_IN_LEFT_TOP_MODE = 1 << 0;
    public static final int BIT_SIDEBAR_IN_RIGHT_TOP_MODE = 1 << 1;

    private Title mTitle;
    private SettingItemSwitch mSidebarSwitch;
    private SettingItemText mAddApp, mAddShare;

    private final int[] mAddContactId = new int[] { R.id.add_contact_1,
            R.id.add_contact_2, R.id.add_contact_3, R.id.add_contact_4 };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        // recyle window background bitmap to release memory
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
                            boolean left = Settings.Global.getInt(
                                    getContentResolver(), Settings.Global.ONE_HAND_MODE, 1) == 0;
                            SidebarUtils.requestEnterSidebarMode(left ? BIT_SIDEBAR_IN_LEFT_TOP_MODE
                                            : BIT_SIDEBAR_IN_RIGHT_TOP_MODE);
                        }
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        setEnable(isSidebarEnable());
        updateAddContactGroup();
    }

    private boolean isSidebarEnable() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.SIDE_BAR_MODE, 1) == 1;
    }

    private void setEnable(boolean enable) {
        for (int i = 0; i < mAddContactId.length; ++i) {
            SettingItemText itemText = (SettingItemText) findViewById(mAddContactId[i]);
            //itemText.setEnable(enable);
            itemText.setEnabled(enable);
        }
        mAddApp.setEnabled(enable);
        mAddShare.setEnabled(enable);
    }

    private void updateAddContactGroup() {
        List<AddContactItem> list = AddContactManager.getInstance(this).getList();
        for (int i = 0; i < mAddContactId.length; ++i) {
            SettingItemText itemText = (SettingItemText) findViewById(mAddContactId[i]);
            if (i < list.size()) {
                itemText.setVisibility(View.VISIBLE);
                itemText.setTitle(list.get(i).labelId);
                itemText.setIconResource(list.get(i).iconId);
                itemText.setOnClickListener(list.get(i).mListener);
                itemText.setArrowVisible(false);
                if (list.size() == 1) {
                    // this should never happen !
                    itemText.setBackgroundResource(R.drawable.selector_setting_sub_item_bg_single);
                } else {
                    if (i == 0) {
                        itemText.setBackgroundResource(R.drawable.selector_setting_sub_item_bg_top);
                    } else if (i == list.size() - 1) {
                        itemText.setBackgroundResource(R.drawable.selector_setting_sub_item_bg_bottom);
                    } else {
                        itemText.setBackgroundResource(R.drawable.selector_setting_sub_item_bg_middle);
                    }
                }
            } else {
                itemText.setVisibility(View.GONE);
            }
        }
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
