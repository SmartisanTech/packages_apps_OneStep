package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.DragEvent;

import com.smartisanos.sidebar.R;

public class DingDingContact extends ContactItem {
    public static final String PKG_NAME = "com.alibaba.android.rimet";
    // may be null in old version dingding.
    public final String sendUserId;
    public final String encodedUid;
    public final int systemUid;

    public DingDingContact(Context context, int systemUid, String sendUserId, String encodedUid, Bitmap avatar, CharSequence displayName) {
        super(context, avatar, displayName);
        this.sendUserId = sendUserId;
        this.encodedUid = encodedUid;
        this.systemUid = systemUid;
    }

    @Override
    public boolean acceptDragEvent(Context context, DragEvent event) {
        return event.getClipDescription().getMimeTypeCount() == 1;
    }

    @Override
    public boolean handleDragEvent(Context context, DragEvent event) {
        boolean sret = super.handleDragEvent(context, event);
        if(sret){
            return true;
        }

        if (event.getClipData().getItemCount() != 1
                || event.getClipData().getDescription().getMimeTypeCount() != 1) {
            return false;
        }

        Intent intent = new Intent("com.alibaba.android.rimet.SEND");
        intent.putExtra("user_id_string", encodedUid);
        intent.putExtra("send_user_id", sendUserId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String mimeType = event.getClipDescription().getMimeType(0);
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)) {
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_TEXT, event.getClipData().getItemAt(0).getText().toString());
        } else if (ClipDescription.compareMimeTypes(mimeType, "image/*")) {
            intent.setDataAndType(event.getClipData().getItemAt(0).getUri(), mimeType);
        } else {
            intent.setDataAndType(event.getClipData().getItemAt(0).getUri(), mimeType);
        }

        try {
            LaunchApp.start(mContext, intent, true, PKG_NAME, systemUid);
            return true;
        } catch (ActivityNotFoundException e) {
            // NA
        }
        return false;
    }

    @Override
    public boolean openUI(Context context) {
        Intent intent = new Intent("com.alibaba.android.rimet.ShortCutChat");
        intent.putExtra("user_id_string", encodedUid);
        intent.putExtra("send_user_id", sendUserId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            LaunchApp.start(mContext, intent, true, PKG_NAME, systemUid);
            Tracker.onClick(Tracker.EVENT_CLICK_CONTACTS, "1");
            return true;
        } catch (ActivityNotFoundException e) {
            // NA
        }
        return false;
    }

    @Override
    public void save() {
        DatabaseHelper.getInstance(mContext).update(this);
    }

    @Override
    public void deleteFromDatabase() {
        DatabaseHelper.getInstance(mContext).remove(this);
    }

    @Override
    public int getTypeIcon() {
        return R.drawable.contact_icon_dingding;
    }

    @Override
    public String getPackageName() {
        return PKG_NAME;
    }

    @Override
    public boolean sameContact(ContactItem ci) {
        if (!(ci instanceof DingDingContact)) {
            return false;
        }
        DingDingContact ddc = (DingDingContact) ci;
        return TextUtils.equals(sendUserId, ddc.sendUserId)
                && TextUtils.equals(encodedUid, ddc.encodedUid);
    }

    public static List<ContactItem> getContacts(Context context){
        return DatabaseHelper.getInstance(context).getContacts();
    }

    public static final class DatabaseHelper extends SQLiteOpenHelper{
        private volatile static DatabaseHelper sInstance;
        public synchronized static DatabaseHelper getInstance(Context context){
            if(sInstance == null){
                synchronized(DatabaseHelper.class){
                    if(sInstance == null){
                        sInstance = new DatabaseHelper(context);
                    }
                }
            }
            return sInstance;
        }

        private static final String TABLE_CONTACTS = "contacts";
        private static final String DB_NAME = "ding_contacts";
        private static final int DB_VERSION = 1;

        private Context mContext;
        private DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "system_uid INTEGER,"
                + "send_user_id TEXT,"
                + "encoded_uid TEXT,"
                + "avatar BLOB,"
                + "display_name TEXT,"
                + "weight INTEGER);";

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // NA
        }

        public int getId(DingDingContact ddc) {
            ThreadVerify.verify(false);
            Cursor cursor = null;
            try {
                if (!TextUtils.isEmpty(ddc.encodedUid)) {
                    cursor = getReadableDatabase().query(
                            TABLE_CONTACTS,
                            null,
                            ContactColumns.ENCODED_UID + "=?" + "and "
                                    + ContactColumns.SEND_USER_ID + "=?",
                            new String[] { ddc.encodedUid, ddc.sendUserId },
                            null, null, null);
                }
                if (cursor != null && cursor.getCount() > 0
                        && cursor.moveToFirst()) {
                    return cursor.getInt(cursor
                            .getColumnIndex(ContactColumns._ID));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return 0;
        }

        public void update(DingDingContact ddc) {
            ThreadVerify.verify(false);
            int id = getId(ddc);
            // insert
            ContentValues cv = new ContentValues();
            cv.put(ContactColumns.SYS_UID, ddc.systemUid);
            cv.put(ContactColumns.SEND_USER_ID, ddc.sendUserId);
            cv.put(ContactColumns.ENCODED_UID, ddc.encodedUid);
            cv.put(ContactColumns.AVATAR, BitmapUtils.Drawable2Bytes(ddc.getAvatar()));
            cv.put(ContactColumns.DISPLAY_NAME, ddc.getDisplayName().toString());
            cv.put(ContactColumns.WEIGHT, ddc.getIndex());
            if (id != 0) {
                // update database;
                getWritableDatabase().update(TABLE_CONTACTS, cv,
                        ContactColumns._ID + "=?", new String[] { id + "" });
            } else {
                getWritableDatabase().insert(TABLE_CONTACTS, null, cv);
            }
        }

        public void remove(DingDingContact ddc) {
            ThreadVerify.verify(false);
            int id = getId(ddc);
            if (id != 0) {
                getWritableDatabase().delete(TABLE_CONTACTS,
                        ContactColumns._ID + "=?", new String[] { id + "" });
            }
        }

        public void remove(String where) {
            ThreadVerify.verify(false);
            try {
                getWritableDatabase().delete(TABLE_CONTACTS, where, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public List<ContactItem> getContacts() {
            ThreadVerify.verify(false);
            List<ContactItem> ret = new ArrayList<ContactItem>();
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query("contacts", null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    int systemUid = cursor.getInt(cursor.getColumnIndex(ContactColumns.SYS_UID));
                    String sendUserId = cursor.getString(cursor.getColumnIndex(ContactColumns.SEND_USER_ID));
                    String encodedUid = cursor.getString(cursor.getColumnIndex(ContactColumns.ENCODED_UID));
                    Bitmap avatar = BitmapUtils.Bytes2Bitmap(cursor.getBlob(cursor.getColumnIndex(ContactColumns.AVATAR)));
                    String display_name = cursor.getString(cursor.getColumnIndex(ContactColumns.DISPLAY_NAME));
                    int index = cursor.getInt(cursor.getColumnIndex(ContactColumns.WEIGHT));
                    DingDingContact ddc = new DingDingContact(mContext, systemUid, sendUserId, encodedUid, avatar, display_name);
                    ddc.setIndex(index);
                    ret.add(ddc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return ret;
        }

        static class ContactColumns implements BaseColumns{
            static final String SYS_UID = "system_uid";
            static final String SEND_USER_ID = "send_user_id";
            static final String ENCODED_UID = "encoded_uid";
            static final String AVATAR = "avatar";
            static final String DISPLAY_NAME = "display_name";
            static final String WEIGHT = "weight";
        }
    }

    public static void removeDoppelgangerShortcut(Context context) {
        String where = DatabaseHelper.ContactColumns.SYS_UID + "=" + UserPackage.USER_DOPPELGANGER;
        DatabaseHelper.getInstance(context).remove(where);
    }
}
