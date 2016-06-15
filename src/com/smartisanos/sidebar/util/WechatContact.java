package com.smartisanos.sidebar.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.DragEvent;

import java.util.ArrayList;
import java.util.List;

public class WechatContact extends ContactItem {

    private String mName;
    private String mIntent;
    private Bitmap mIcon;
    private int mIndex;

    public static final String WECHAT = "com.tencent.mm";

    public WechatContact(Context context, String name, String intent, Bitmap icon) {
        super(context);
        mName = name;
        mIntent = intent;
        mIcon = icon;
    }

    @Override
    public Bitmap getAvatar() {
        return mIcon;
    }

    @Override
    public CharSequence getDisplayName() {
        return mName;
    }

    @Override
    public boolean accptDragEvent(DragEvent event) {
        return true;
    }

    @Override
    public boolean handleDragEvent(DragEvent event) {
        return false;
    }

    @Override
    public void save() {

    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public void setIndex(int index) {
        mIndex = index;
    }

    @Override
    public int getTypeIcon() {
        return 0;
    }

    @Override
    public boolean sameContact(ContactItem ci) {
        if (ci == null) {
            return false;
        }
        if (mIntent == null) {
            return false;
        }
        if (ci instanceof WechatContact) {
            WechatContact wc = (WechatContact) ci;
            if (mIntent.equals(wc.mIntent)) {
                return true;
            }
        }
        return false;
    }

    private static final String METHOD_QUERY_SHORTCUT = "query_shortcut";

    private static final String AUTHORITIES = "com.smartisanos.expandservice.launcher";

    public static List<ContactItem> getContacts(Context context){
        List<ContactItem> list = new ArrayList<ContactItem>();
        try {
            Uri uri = Uri.parse(AUTHORITIES);
            ContentResolver contentResolver = context.getContentResolver();
            Bundle extras = new Bundle();
            extras.putString("pkg", WECHAT);
            Bundle bundle = contentResolver.call(uri, METHOD_QUERY_SHORTCUT, null, extras);
            Bundle[] dataArr = (Bundle[]) bundle.getParcelableArray("shortcut");
            if (dataArr != null) {
                for (int i = 0; i < dataArr.length; i++) {
                    Bundle b = dataArr[i];
                    if (b == null) {
                        continue;
                    }
                    String name = b.getString("name");
                    String intent = b.getString("intent");
                    Bitmap icon = b.getParcelable("icon");
                    if (intent == null) {
                        continue;
                    }
                    WechatContact wc = new WechatContact(context, name, intent, icon);
                    list.add(wc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}