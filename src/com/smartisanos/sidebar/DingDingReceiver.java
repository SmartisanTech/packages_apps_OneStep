package com.smartisanos.sidebar;

import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.DingDingContact;
import com.smartisanos.sidebar.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

public class DingDingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ContactManager cm = ContactManager.getInstance(context);
        if ("com.alibaba.android.rimet.ShortCutSelectResult".equals(action)) {
            long uid = intent.getLongExtra("uid", 0);
            String encodedUid = intent.getStringExtra("user_id_string");
            String displayName = intent.getStringExtra("name");
            Bitmap avatar = intent.getParcelableExtra("avatar");
            avatar = BitmapUtils.getContactAvatar(context, avatar);
            ContactItem ci = new DingDingContact(context, Utils.getUidFromIntent(intent), uid, encodedUid, avatar, displayName);
            cm.addContact(ci);
        }
    }
}
