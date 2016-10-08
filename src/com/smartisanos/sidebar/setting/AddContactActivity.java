package com.smartisanos.sidebar.setting;

import java.util.List;

import android.os.Bundle;
import android.view.View;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.AddContactManager;
import com.smartisanos.sidebar.util.AddContactManager.AddContactItem;

import smartisanos.widget.SettingItemText;

public class AddContactActivity extends BaseActivity {

    private final int[] mAddContactId = new int[] { R.id.add_contact_1,
            R.id.add_contact_2, R.id.add_contact_3, R.id.add_contact_4 };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_layout);
        getWindow().setBackgroundDrawable(null);
        setupBackBtnOnTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAddContactGroup();
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

}
