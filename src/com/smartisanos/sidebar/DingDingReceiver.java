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
import android.text.TextUtils;
import android.util.Log;

public class DingDingReceiver extends BroadcastReceiver {

    private static final String TAG = DingDingReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ContactManager cm = ContactManager.getInstance(context);
        if ("com.alibaba.android.rimet.ShortCutSelectResult".equals(action)) {
            String encodedUid = intent.getStringExtra("user_id_string");
            //maybe null, old version doesn't contain this key
            String sendUserId = intent.getStringExtra("send_user_id");
            // old version for name
            String displayName = intent.getStringExtra("name");
            if (TextUtils.isEmpty(displayName)) {
                // new version for name
                displayName = intent.getStringExtra("intent_key_user_name");
            }
            // old version for avatar
            Bitmap avatar = intent.getParcelableExtra("avatar");
            if (avatar == null) {
                // new version for avatar
                avatar = intent.getParcelableExtra("intent_key_user_avatar");
            }
            if (TextUtils.isEmpty(encodedUid)
                    || TextUtils.isEmpty(displayName) || avatar == null
                    || avatar.getWidth() <= 0 || avatar.getHeight() <= 0) {
                Log.e(TAG, "invalid data from dingding !");
                return;
            }
            avatar = BitmapUtils.getContactAvatar(context, avatar);
            ContactItem ci = new DingDingContact(context, Utils.getUidFromIntent(intent), sendUserId, encodedUid, avatar, displayName);
            cm.addContact(ci);
        }
    }
}
