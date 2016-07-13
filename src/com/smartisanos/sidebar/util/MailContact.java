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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.DragEvent;

public class MailContact extends ContactItem {

    private String mMailAddress;

    public MailContact(Context context, CharSequence displayName, String mailAddress) {
        this(context, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_contact_avatar), displayName, mailAddress);
    }

    public MailContact(Context context, Bitmap avatar, CharSequence displayName, String mailAddress) {
        super(context, avatar, displayName);
        if (TextUtils.isEmpty(mailAddress)) {
            throw new IllegalArgumentException("Addresss is empty !");
        }
        mMailAddress = mailAddress;
    }

    public String getAddress(){
        return mMailAddress;
    }

    @Override
    public boolean accptDragEvent(DragEvent event) {
        // we can handle all mimeType!
        return true;
    }

    @Override
    public boolean handleDragEvent(DragEvent event) {
        if (event.getClipData().getItemCount() <= 0) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + mMailAddress));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String mimeType = event.getClipDescription().getMimeType(0);
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)) {
            intent.putExtra(Intent.EXTRA_TEXT, event.getClipData().getItemAt(0).getText());
        } else if (event.getClipData().getItemAt(0).getUri() != null) {
            intent.putExtra(Intent.EXTRA_STREAM, event.getClipData().getItemAt(0).getUri());
        }

        try {
            mContext.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void save() {
        MailDatabaseHelper.getInstance(mContext).update(this);
    }


    @Override
    public void deleteFromDatabase() {
        MailDatabaseHelper.getInstance(mContext).remove(this);
    }

    @Override
    public int getTypeIcon() {
        return R.drawable.contact_icon_mail;
    }

    @Override
    public boolean sameContact(ContactItem ci) {
        if (ci == null || !(ci instanceof MailContact)) {
            return false;
        }
        MailContact mc = (MailContact) ci;
        return mMailAddress.equals(mc.mMailAddress);
    }

    public static List<ContactItem> getContacts(Context context){
        return MailDatabaseHelper.getInstance(context).getContacts();
    }

    private static final class MailDatabaseHelper extends SQLiteOpenHelper{
        private volatile static MailDatabaseHelper sInstance;
        public synchronized static MailDatabaseHelper getInstance(Context context){
            if(sInstance == null){
                synchronized(MailDatabaseHelper.class){
                    if(sInstance == null){
                        sInstance = new MailDatabaseHelper(context);
                    }
                }
            }
            return sInstance;
        }

        private static final String DB_NAME = "mail_contacts";
        private static final int DB_VERSION = 1;

        private Context mContext;
        private MailDatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_CONTACTS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "mail_address TEXT,"
                    + "avatar BLOB,"
                    + "display_name TEXT,"
                    + "weight INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //NA
        }

        public int getId(MailContact contact) {
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_CONTACTS, null,
                        ContactColumns.MAIL_ADDRESS + "=?",
                        new String[] {contact.mMailAddress }, null, null, null);
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

        public void update(MailContact contact) {
            int id = getId(contact);
            // insert
            ContentValues cv = new ContentValues();
            cv.put(ContactColumns.MAIL_ADDRESS, contact.mMailAddress);
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

        public void remove(MailContact contact) {
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
                cursor = getReadableDatabase().query(TABLE_CONTACTS, null,
                        null, null, null, null, null);
                while (cursor.moveToNext()) {
                    String mailAddress = cursor.getString(cursor.getColumnIndex(ContactColumns.MAIL_ADDRESS));
                    Bitmap avatar = BitmapUtils.Bytes2Bitmap(cursor.getBlob(cursor.getColumnIndex(ContactColumns.AVATAR)));
                    String display_name = cursor.getString(cursor.getColumnIndex(ContactColumns.DISPLAY_NAME));
                    int index = cursor.getInt(cursor.getColumnIndex(ContactColumns.WEIGHT));
                    MailContact mc = new MailContact(mContext, avatar, display_name, mailAddress);
                    mc.setIndex(index);
                    ret.add(mc);
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
            static final String MAIL_ADDRESS = "mail_address";
            static final String AVATAR = "avatar";
            static final String DISPLAY_NAME = "display_name";
            static final String WEIGHT = "weight";
        }
    }
}
