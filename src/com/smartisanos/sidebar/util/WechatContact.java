package com.smartisanos.sidebar.util;

import android.content.ActivityNotFoundException;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.view.DragEvent;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartisanos.sidebar.R;

public class WechatContact extends ContactItem {

    public static final String PKG_NAME = "com.tencent.mm";

    private String mIntent;

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
        if (ClipDescription.MIMETYPE_TEXT_PLAIN.equals(mimeType)) {
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
            intent.putExtra("wechat_text", event.getClipData().getItemAt(0).getText());
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
        DatabaseHelper.getInstance(mContext).update(this);
    }

    @Override
    public void deleteFromDatabase() {
        DatabaseHelper.getInstance(mContext).remove(this);
    }

    @Override
    public int getTypeIcon() {
        return R.drawable.contact_icon_wechat;
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

    public static List<ContactItem> getContacts(Context context){
        return DatabaseHelper.getInstance(context).list();
    }

    public static final class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "wechat_contacts";

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

        public static final String TABLE_NAME    = "contacts";
        public static final String ID            = "_id";
        public static final String DISPLAY_NAME  = "display_name";
        public static final String WEIGHT        = "weight";
        public static final String LAUNCH_INTENT = "launchIntent";
        public static final String AVATAR        = "avatar";

        public static final String[] columns = new String[] {ID, DISPLAY_NAME, WEIGHT, LAUNCH_INTENT, AVATAR};
        private static final Map<String, String> columnProps = new HashMap<String, String>();
        static {
            columnProps.put(ID,                "INTEGER PRIMARY KEY");
            columnProps.put(DISPLAY_NAME,      "TEXT");
            columnProps.put(WEIGHT,            "INTEGER");
            columnProps.put(LAUNCH_INTENT,     "TEXT");
            columnProps.put(AVATAR,            "BLOB");
        }

        private Context mContext;

        private DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = generateCreateSQL(TABLE_NAME, columns, columnProps);
            db.execSQL(sql);
        }

        private static String generateCreateSQL(String table, String[] columns, Map<String, String> props) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("CREATE TABLE IF NOT EXISTS " + table + " (");
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                String prop = props.get(column);
                buffer.append(column);
                buffer.append(" ");
                buffer.append(prop);
                if (i != (columns.length - 1)) {
                    buffer.append(", ");
                }
            }
            buffer.append(");");
            return buffer.toString();
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        }

        private long getRecordId(WechatContact info) {
            String launchIntent = info.mIntent;
            String where = LAUNCH_INTENT + "='"+launchIntent+"'";
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_NAME, null, where, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(ID);
                    return cursor.getInt(index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return -1;
        }

        public long update(WechatContact info) {
            long recordId = getRecordId(info);
            long result = -1;
            ContentValues cv = new ContentValues();
            cv.put(DISPLAY_NAME, info.mDisplayName.toString());
            cv.put(WEIGHT, info.getIndex());
            cv.put(LAUNCH_INTENT, info.mIntent);
            cv.put(AVATAR, BitmapUtils.Bitmap2Bytes(info.getAvatar()));
            if (recordId > 0) {
                //update
                String whereCase = ID + "=" + recordId;
                result = getWritableDatabase().update(TABLE_NAME, cv, whereCase, null);
            } else {
                //insert
                result = getWritableDatabase().insert(TABLE_NAME, null, cv);
            }
            return result;
        }

        public boolean remove(WechatContact info) {
            long recordId = getRecordId(info);
            if (recordId > 0) {
                String where = ID + "=" + recordId;
                int affectedRow = getWritableDatabase().delete(TABLE_NAME, where, null);
                if (affectedRow > 0) {
                    return true;
                }
            }
            return false;
        }

        public List<ContactItem> list() {
            List<ContactItem> list = new ArrayList<ContactItem>();
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(DISPLAY_NAME);
                    int weightIndex = cursor.getColumnIndex(WEIGHT);
                    int intentIndex = cursor.getColumnIndex(LAUNCH_INTENT);
                    int avatarIndex = cursor.getColumnIndex(AVATAR);
                    do {
                        String name = cursor.getString(nameIndex);
                        int weight = cursor.getInt(weightIndex);
                        String intent = cursor.getString(intentIndex);
                        byte[] avatar = cursor.getBlob(avatarIndex);
                        Bitmap icon = BitmapUtils.Bytes2Bitmap(avatar);
                        WechatContact contact = new WechatContact(mContext, name, intent, icon);
                        contact.setIndex(weight);
                        list.add(contact);
                    } while (cursor.moveToNext());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return list;
        }
    }
}