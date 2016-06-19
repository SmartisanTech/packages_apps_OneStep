package com.smartisanos.sidebar.util;

import android.content.ActivityNotFoundException;
import android.content.ClipDescription;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.DragEvent;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.smartisanos.sidebar.R;

public class WechatContact extends ContactItem {

    private String mIntent;

    public static final String WECHAT = "com.tencent.mm";

    public WechatContact(Context context, String name, String intent, Bitmap icon) {
        super(context, icon, name);
        mIntent = intent;
    }
    @Override
    public boolean accptDragEvent(DragEvent event) {
        if (event.getClipDescription().getMimeTypeCount() <= 0) {
            return false;
        }
        String mimeType = event.getClipDescription().getMimeType(0);
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)
                || ClipDescription.compareMimeTypes(mimeType, "image/*")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean handleDragEvent(DragEvent event) {
        Intent intent = null;
        try {
            intent = Intent.parseUri(mIntent, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }

        try {
            mContext.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            // NA
        }
        return false;
    }

    @Override
    public void save() {

    }

    @Override
    public int getTypeIcon() {
        return R.drawable.contact_icon_wechat;
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

    private static final String AUTHORITIES = "content://com.smartisanos.expandservice.launcher";

    public static List<ContactItem> getContacts(Context context){
        List<ContactItem> list = new ArrayList<ContactItem>();
        try {
            Uri uri = Uri.parse(AUTHORITIES);
            ContentResolver contentResolver = context.getContentResolver();
            Bundle extras = new Bundle();
            extras.putString("pkg", WECHAT);
            Bundle bundle = contentResolver.call(uri, METHOD_QUERY_SHORTCUT, null, extras);
            Parcelable[] dataArr = bundle.getParcelableArray("shortcut");
            if (dataArr != null) {
                for (int i = 0; i < dataArr.length; i++) {
                    Bundle b = (Bundle)dataArr[i];
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