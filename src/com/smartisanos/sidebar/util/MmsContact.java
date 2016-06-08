package com.smartisanos.sidebar.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.DragEvent;

public class MmsContact extends ContactItem {
    private long contactId;
    private String phoneNumber;
    private Bitmap avatar;

    public MmsContact(Context context, long contactId, String number, Bitmap avatar) {
        super(context);
        this.contactId = contactId;
        this.phoneNumber = number;
        this.avatar = avatar;
    }

    @Override
    public Bitmap getAvatar() {
        return avatar;
    }

    @Override
    public CharSequence getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean accptDragEvent(DragEvent event) {
        //TODO
        return true;
    }

    @Override
    public boolean handleDragEvent(DragEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void save() {
        // TODO Auto-generated method stub
    }

    @Override
    public int getIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setIndex(int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getTypeIcon() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean sameContact(ContactItem ci) {
        // TODO Auto-generated method stub
        return false;
    }
}
