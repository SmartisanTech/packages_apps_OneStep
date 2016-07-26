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
    public final long uid;
    public final String encodedUid;

    public DingDingContact(Context context, long uid, String encodedUid, Bitmap avatar, CharSequence displayName) {
        super(context, avatar, displayName);
        this.uid = uid;
        this.encodedUid = encodedUid;
    }

    @Override
    public boolean acceptDragEvent(Context context, DragEvent event) {
        return true;
    }

    @Override
    public boolean handleDragEvent(Context context, DragEvent event) {
        boolean sret = super.handleDragEvent(context, event);
        if(sret){
            return true;
        }

        if (event.getClipData().getItemCount() <= 0
                || event.getClipData().getDescription().getMimeTypeCount() <= 0) {
            return false;
        }

        Intent intent = new Intent("com.alibaba.android.rimet.SEND");
        intent.putExtra("user_id", uid);
        intent.putExtra("user_id_string", encodedUid);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String mimeType = event.getClipDescription().getMimeType(0);
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)) {
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_TEXT, event.getClipData().getItemAt(0).getText());
        } else if (ClipDescription.compareMimeTypes(mimeType, "image/*")) {
            intent.setDataAndType(event.getClipData().getItemAt(0).getUri(), mimeType);
        } else {
            intent.setDataAndType(event.getClipData().getItemAt(0).getUri(), mimeType);
        }

        try {
            LaunchApp.start(mContext, intent, true, PKG_NAME, 0);
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
        if (uid != 0 && ddc.uid != 0) {
            return uid == ddc.uid;
        }
        if (!TextUtils.isEmpty(encodedUid)
                && !TextUtils.isEmpty(ddc.encodedUid)) {
            return encodedUid.equals(ddc.encodedUid);
        }
        return false;
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

        private static final String DB_NAME = "dingding_contacts";
        private static final int DB_VERSION = 1;

        private Context mContext;
        private DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_CONTACTS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "uid TEXT,"
                    + "encoded_uid TEXT,"
                    + "avatar BLOB,"
                    + "display_name TEXT,"
                    + "weight INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //NA
        }

        public int getId(DingDingContact ddc) {
            ThreadVerify.verify(false);
            Cursor cursor = null;
            try {
                if (ddc.uid != 0) {
                    cursor = getReadableDatabase().query(TABLE_CONTACTS, null,
                            ContactColumns.UID + "=?",
                            new String[] { ddc.uid + "" }, null, null, null);
                }
                if ((cursor == null || cursor.getCount() <= 0)
                        && !TextUtils.isEmpty(ddc.encodedUid)) {
                    cursor = getReadableDatabase().query(TABLE_CONTACTS, null,
                            ContactColumns.ENCODED_UID + "=?",
                            new String[] { ddc.encodedUid + "" }, null, null, null);
                }
                if (cursor.moveToFirst()) {
                    return cursor.getInt(cursor.getColumnIndex(ContactColumns._ID));
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
            cv.put(ContactColumns.UID, ddc.uid + "");
            cv.put(ContactColumns.ENCODED_UID, ddc.encodedUid);
            cv.put(ContactColumns.AVATAR, BitmapUtils.Bitmap2Bytes(ddc.getAvatar()));
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

        public List<ContactItem> getContacts() {
            ThreadVerify.verify(false);
            List<ContactItem> ret = new ArrayList<ContactItem>();
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query("contacts", null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    long uid = Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactColumns.UID)));
                    String encodedUid = cursor.getString(cursor.getColumnIndex(ContactColumns.ENCODED_UID));
                    Bitmap avatar = BitmapUtils.Bytes2Bitmap(cursor.getBlob(cursor.getColumnIndex(ContactColumns.AVATAR)));
                    String display_name = cursor.getString(cursor.getColumnIndex(ContactColumns.DISPLAY_NAME));
                    int index = cursor.getInt(cursor.getColumnIndex(ContactColumns.WEIGHT));
                    DingDingContact ddc = new DingDingContact(mContext, uid, encodedUid, avatar, display_name);
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

        private static final String TABLE_CONTACTS = "contacts";
        static class ContactColumns implements BaseColumns{
            static final String UID = "uid";
            static final String ENCODED_UID = "encoded_uid";
            static final String AVATAR = "avatar";
            static final String DISPLAY_NAME = "display_name";
            static final String WEIGHT = "weight";
        }
    }
}
