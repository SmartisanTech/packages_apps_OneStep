package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.DragEvent;

public abstract class ContactItem implements SidebarItem{
    protected Context mContext;
    protected Bitmap mAvatar;
    protected Bitmap mAvatarWithGray;
    protected CharSequence mDisplayName;
    protected int mIndex;

    public ContactItem(Context context, Bitmap avatar, CharSequence displayName) {
        mContext = context;
        mAvatar = avatar;
        mAvatarWithGray = BitmapUtils.convertToBlackWhite(mAvatar);
        mDisplayName = displayName;
    }

    public Bitmap getAvatar() {
        return mAvatar;
    }

    public Bitmap getAvatarWithGray() {
        return mAvatarWithGray;
    }

    public CharSequence getDisplayName() {
        return mDisplayName;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public final void delete(){
        ContactManager.getInstance(mContext).remove(this);
    }

    /**
     * this method can only be called by ContactManger. don't invoke it in other place!
     * */
    public abstract void deleteFromDatabase();
    public abstract boolean accptDragEvent(DragEvent event);
    public abstract boolean handleDragEvent(DragEvent event);
    public abstract void save();
    public abstract int getTypeIcon();
    public abstract boolean sameContact(ContactItem ci);
    public static List<ContactItem> getContactList(Context context){
        List<ContactItem> all = new ArrayList<ContactItem>();
        all.addAll(DingDingContact.getContacts(context));
        all.addAll(WechatContact.getContacts(context));
        all.addAll(MmsContact.getContacts(context));
        all.addAll(MailContact.getContacts(context));
        return all;
    }
}
