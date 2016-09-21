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
    private static final LOG log = LOG.getInstance(WechatContact.class);

    public static final String PKG_NAME = "com.tencent.mm";

    private String mIntent;
    private int mUid;

    public WechatContact(Context context, String name, String intent, Bitmap icon) {
        super(context, icon, name);
        mIntent = intent;
    }

    public int getUserId() {
        return mUid;
    }

    public void setUserId(int id) {
        mUid = id;
    }

    @Override
    public boolean acceptDragEvent(Context context, DragEvent event) {
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
    public boolean handleDragEvent(Context context, DragEvent event) {
        boolean sret = super.handleDragEvent(context, event);
        if(sret){
            return true;
        }
        if(event.getClipData().getItemCount() <= 0){
            return false;
        }
        Intent intent = null;
        try {
            intent = Intent.parseUri(mIntent, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("wechat_text", event.getClipData().getItemAt(0).getText());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (intent == null) {
            //lose intent
            return false;
        }
        log.error("start with uid ["+mUid+"]");
        try {
            LaunchApp.start(mContext, intent, true, PKG_NAME, mUid);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean openUI(Context context) {
        Intent intent = null;
        try {
            intent = Intent.parseUri(mIntent, 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }

        try {
            LaunchApp.start(mContext, intent, true, PKG_NAME, mUid);
            return true;
        } catch (ActivityNotFoundException e) {
            // NA
        }
        return false;
    }

    public static void removeDoppelgangerShortcut(Context context) {
        String where = DatabaseHelper.UID + "=" + UserPackage.USER_DOPPELGANGER;
        DatabaseHelper.getInstance(context).remove(where);
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

        private static final int DB_VERSION = 2;
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
        public static final String UID           = "user_id";

        public static final String[] columns = new String[] {ID, DISPLAY_NAME, WEIGHT, UID, LAUNCH_INTENT, AVATAR};
        private static final Map<String, String> columnProps = new HashMap<String, String>();
        static {
            columnProps.put(ID,                "INTEGER PRIMARY KEY");
            columnProps.put(DISPLAY_NAME,      "TEXT");
            columnProps.put(WEIGHT,            "INTEGER");
            columnProps.put(UID,               "INTEGER");
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
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            log.error("update db to version => old ["+oldVersion+"] new ["+newVersion+"]");
            //to version 2
            for (int i = oldVersion; i < newVersion; i++) {
                int version = oldVersion + 1;
                upgradeTo(db, version);
            }
        }

        private void upgradeTo(SQLiteDatabase db, int version) {
            log.error("upgradeTo => " + version);
            switch (version) {
                case 2 : {
                    String sql = generateCreateSQL(TABLE_NAME, columns, columnProps);
                    String[] oldColumns = new String[] {ID, DISPLAY_NAME, WEIGHT, LAUNCH_INTENT, AVATAR};
                    boolean success = formatTable(db, TABLE_NAME, oldColumns, sql);
                    log.error("formatTable ["+TABLE_NAME+"] => " + success);
                    break;
                }
            }
        }

        /**
         * merge data from old table to new table.
         * make sure table name won't change and column name & type is same with old table
         * @param tableName
         * @param columns backup data columns
         * @param createSql sql for create table
         */
        public static boolean formatTable(SQLiteDatabase db, final String tableName, final String[] columns, String createSql) {
            boolean success = true;
            db.beginTransaction();
            try {
                success = formatTableImpl(db, tableName, columns, createSql);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                success = false;
                e.printStackTrace();
            } finally {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    success = false;
                    e.printStackTrace();
                }
            }
            return success;
        }

        private static final String DROP_TABLE_SQL_PREFIX = "DROP TABLE IF EXISTS ";

        public static String dropTableSql(String tableName) {
            return DROP_TABLE_SQL_PREFIX + tableName;
        }

        /**
         * merge data from old table to new table.
         * make sure table name won't change and column name & type is same with old table
         * @param tableName
         * @param columns backup data columns
         * @param createSql sql for create table
         */
        private static boolean formatTableImpl(SQLiteDatabase db, final String tableName, final String[] columns, String createSql) {
            if (tableName == null) {
                log.error("mergeTable return by tableName is null");
                return false;
            }
            if (columns == null || columns.length == 0) {
                log.error("mergeTable return by columns is empty");
                return false;
            }
            // rename old table
            String oldTableName = tableName + "_old";
            String renameTableSql = "ALTER TABLE " + tableName + " RENAME TO " + oldTableName;
            db.execSQL(renameTableSql);
            // create table with format
            db.execSQL(createSql);
            // merge data to new table
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < columns.length; i++) {
                buffer.append(columns[i]);
                if (i != (columns.length - 1)) {
                    buffer.append(", ");
                }
            }
            String mergeColumns = buffer.toString();
            String mergeSql = "INSERT INTO " + tableName + " (" + mergeColumns +
                    ") SELECT " + mergeColumns + " FROM " + oldTableName;
            db.execSQL(mergeSql);
            // drop tmp table
            db.execSQL(dropTableSql(oldTableName));
            return true;
        }

        private long getRecordId(WechatContact info) {
            ThreadVerify.verify(false);
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
            ThreadVerify.verify(false);
            long recordId = getRecordId(info);
            long result = -1;
            ContentValues cv = new ContentValues();
            cv.put(UID, info.mUid);
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
            ThreadVerify.verify(false);
            long recordId = getRecordId(info);
            if (recordId > 0) {
                String where = ID + "=" + recordId;
                return remove(where);
            }
            return false;
        }

        public boolean remove(String where) {
            ThreadVerify.verify(false);
            try {
                SQLiteDatabase db = getWritableDatabase();
                int affectedRow = db.delete(TABLE_NAME, where, null);
                if (affectedRow > 0) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        public List<ContactItem> list() {
            ThreadVerify.verify(false);
            List<ContactItem> list = new ArrayList<ContactItem>();
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(DISPLAY_NAME);
                    int weightIndex = cursor.getColumnIndex(WEIGHT);
                    int intentIndex = cursor.getColumnIndex(LAUNCH_INTENT);
                    int avatarIndex = cursor.getColumnIndex(AVATAR);
                    int uidIndex    = cursor.getColumnIndex(UID);
                    do {
                        String name   = cursor.getString(nameIndex);
                        int weight    = cursor.getInt(weightIndex);
                        String intent = cursor.getString(intentIndex);
                        int uid       = cursor.getInt(uidIndex);
                        byte[] avatar = cursor.getBlob(avatarIndex);
                        Bitmap icon   = BitmapUtils.Bytes2Bitmap(avatar);
                        WechatContact contact = new WechatContact(mContext, name, intent, icon);
                        contact.setUserId(uid);
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