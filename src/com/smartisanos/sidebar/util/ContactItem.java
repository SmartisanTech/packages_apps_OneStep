package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.DragEvent;

public abstract class ContactItem {
    protected Context mContext;

    public ContactItem(Context context) {
        mContext = context;
    }

    public abstract Bitmap getAvatar();
    public abstract CharSequence getDisplayName();
    public abstract boolean accptDragEvent(DragEvent event);
    public abstract boolean handleDragEvent(DragEvent event);
    public abstract void save();
    public abstract int getIndex();
    public abstract void setIndex(int index);
    public abstract int getTypeIcon();
    public abstract boolean sameContact(ContactItem ci);
    public static List<ContactItem> getContactList(Context context){
        List<ContactItem> all = new ArrayList<ContactItem>();
        all.addAll(DingDingContact.getContacts(context));
        all.addAll(WechatContact.getContacts(context));
        return all;
    }
}
