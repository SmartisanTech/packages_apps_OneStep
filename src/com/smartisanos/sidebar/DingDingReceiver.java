package com.smartisanos.sidebar;

import com.smartisanos.sidebar.receiver.ShortcutReceiver;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.ContactManager;
import com.smartisanos.sidebar.util.DingDingContact;
import com.smartisanos.sidebar.util.UserPackage;
import com.smartisanos.sidebar.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.UserHandle;

public class DingDingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ContactManager cm = ContactManager.getInstance(context);
        if ("com.alibaba.android.rimet.ShortCutSelectResult".equals(action)) {
            //get system uid

            int callingUid = intent.getIntExtra(ShortcutReceiver.EXTRA_UID, -1);
            //lose callingUid
            if(callingUid == -1) {
                callingUid = Binder.getCallingUid();
            }
            int userId = UserHandle.getUserId(callingUid);
            int systemUid = 0;
            if (userId == UserPackage.USER_DOPPELGANGER) {
                systemUid = UserPackage.USER_DOPPELGANGER;
            } else if (Utils.appIsDoubleOpen(DingDingContact.PKG_NAME)) {
                systemUid = UserPackage.USER_DOPPELGANGER;
            }

            long uid = intent.getLongExtra("uid", 0);
            String encodedUid = intent.getStringExtra("user_id_string");
            String displayName = intent.getStringExtra("name");
            Bitmap avatar = intent.getParcelableExtra("avatar");
            avatar = BitmapUtils.getContactAvatar(context, avatar);
            ContactItem ci = new DingDingContact(context, systemUid, uid, encodedUid, avatar, displayName);
            cm.addContact(ci);
        }
    }
}
