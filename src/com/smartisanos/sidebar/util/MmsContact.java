package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.List;

import com.smartisanos.sidebar.R;

import android.content.ActivityNotFoundException;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.DragEvent;

public class MmsContact extends ContactItem {
    public static final String PKG_NAME = "com.android.contacts";

    private Context context;
    private int mContactId;
    private String mPhoneNumber;

    public MmsContact(Context context, int contactId, String number, CharSequence displayName) {
        this(context, contactId, number, BitmapUtils.getDefaultContactAvatar(context), displayName);
    }

    public MmsContact(Context context, int contactId, String number, Bitmap avatar, CharSequence displayName) {
        super(context, avatar, displayName);
        if(contactId <= 0 || TextUtils.isEmpty(number)){
            throw new IllegalArgumentException("contactId <= 0 or  num is empty !");
        }
        this.context = context;
        this.mContactId = contactId;
        this.mPhoneNumber = number;
    }

    @Override
    public boolean acceptDragEvent(Context context, DragEvent event) {
        if (event.getClipDescription().getMimeTypeCount() != 1) {
            return false;
        }
        String mimeType = event.getClipDescription().getMimeType(0);
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)
                || ClipDescription.compareMimeTypes(mimeType, "image/*")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean handleDragEvent(Context context, DragEvent event) {
        boolean sret = super.handleDragEvent(context, event);
        if(sret){
            return true;
        }

        if (event.getClipData().getItemCount() != 1) {
            return false;
        }
        Intent intent = null;
        String mimeType = event.getClipDescription().getMimeType(0);
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)) {
            intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + mPhoneNumber));
            intent.putExtra("sms_body", event.getClipData().getItemAt(0).getText());
        } else if (ClipDescription.compareMimeTypes(mimeType, "image/*")) {
            intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra("address", mPhoneNumber);
            intent.putExtra(Intent.EXTRA_STREAM, event.getClipData().getItemAt(0).getUri());
            intent.setType("image/*");
            intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
        }
        if (intent != null) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                // NA
            }
        }
        return false;
    }

    @Override
    public boolean openUI(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + mPhoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            // NA
        }
        return false;
    }

    @Override
    public void save() {
        MmsDatabaseHelper.getInstance(context).update(this);
    }

    @Override
    public void deleteFromDatabase() {
        MmsDatabaseHelper.getInstance(context).delete(this);
    }

    @Override
    public int getTypeIcon() {
        return R.drawable.contact_icon_mms;
    }

    @Override
    public String getPackageName() {
        return PKG_NAME;
    }

    @Override
    public boolean sameContact(ContactItem ci) {
        if (ci == null) {
            return false;
        }
        if (ci instanceof MmsContact) {
            MmsContact o = (MmsContact) ci;
            if (mContactId == o.mContactId
                    && mPhoneNumber.equals(o.mPhoneNumber)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MmsContact(");
        sb.append("id -> " + mContactId + ",");
        sb.append("phoneNumber -> " + mPhoneNumber + ",");
        sb.append("displayName -> " + mDisplayName);
        sb.append(")");
        return sb.toString();
    }

    public static List<ContactItem> getContacts(Context context){
        return MmsDatabaseHelper.getInstance(context).getContacts();
    }

    private static final class MmsDatabaseHelper extends SQLiteOpenHelper{
        private volatile static MmsDatabaseHelper sInstance;
        public synchronized static MmsDatabaseHelper getInstance(Context context){
            if(sInstance == null){
                synchronized(MmsDatabaseHelper.class){
                    if(sInstance == null){
                        sInstance = new MmsDatabaseHelper(context);
                    }
                }
            }
            return sInstance;
        }

        private static final String DB_NAME = "mms_contacts";
        private static final int DB_VERSION = 1;

        private Context mContext;
        private MmsDatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_CONTACTS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "contact_id INTEGER,"
                    + "phone_number TEXT,"
                    + "avatar BLOB,"
                    + "display_name TEXT,"
                    + "weight INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //NA
        }

        public int getId(MmsContact contact) {
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_CONTACTS, null,
                        ContactColumns.CONTACT_ID + "=?" + " and " + ContactColumns.PHONE_NUMBER + "=?",
                        new String[] { contact.mContactId + "", contact.mPhoneNumber }, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
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

        public void update(MmsContact contact) {
            int id = getId(contact);
            // insert
            ContentValues cv = new ContentValues();
            cv.put(ContactColumns.CONTACT_ID, contact.mContactId + "");
            cv.put(ContactColumns.PHONE_NUMBER, contact.mPhoneNumber);
            cv.put(ContactColumns.AVATAR, BitmapUtils.Bitmap2Bytes(contact.getAvatar()));
            cv.put(ContactColumns.DISPLAY_NAME, contact.getDisplayName().toString());
            cv.put(ContactColumns.WEIGHT, contact.getIndex());
            if (id != 0) {
                // update database;
                getWritableDatabase().update(TABLE_CONTACTS, cv,
                        ContactColumns._ID + "=?", new String[] { id + "" });
            } else {
                getWritableDatabase().insert(TABLE_CONTACTS, null, cv);
            }
        }

        public void delete(MmsContact contact) {
            int id = getId(contact);
            if (id != 0) {
                getWritableDatabase().delete(TABLE_CONTACTS,
                        ContactColumns._ID + "=?", new String[] { id + "" });
            }
        }

        public List<ContactItem> getContacts(){
            List<ContactItem> ret = new ArrayList<ContactItem>();
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_CONTACTS, null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    int contactId = cursor.getInt(cursor.getColumnIndex(ContactColumns.CONTACT_ID));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactColumns.PHONE_NUMBER));
                    Bitmap avatar = BitmapUtils.Bytes2Bitmap(cursor.getBlob(cursor.getColumnIndex(ContactColumns.AVATAR)));
                    String display_name = cursor.getString(cursor.getColumnIndex(ContactColumns.DISPLAY_NAME));
                    int index = cursor.getInt(cursor.getColumnIndex(ContactColumns.WEIGHT));
                    MmsContact ddc = new MmsContact(mContext, contactId, phoneNumber, avatar, display_name);
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
            static final String CONTACT_ID = "contact_id";
            static final String PHONE_NUMBER = "phone_number";
            static final String AVATAR = "avatar";
            static final String DISPLAY_NAME = "display_name";
            static final String WEIGHT = "weight";
        }
    }
}
