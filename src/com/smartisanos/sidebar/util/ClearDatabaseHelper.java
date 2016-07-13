package com.smartisanos.sidebar.util;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.BaseColumns;

public class ClearDatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION =  1;
    private static final String TABLE_USELESS = "useless";
    private Handler mHandler;
    private Set<Integer> mSet = new HashSet<Integer>();

    public ClearDatabaseHelper(Context context, String name) {
        super(context, name, null, DB_VERSION);
        HandlerThread thread = new HandlerThread("ClearDatabaseHelper");
        thread.start();
        mHandler = new Handler(thread.getLooper());
        // get set
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(TABLE_USELESS, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(UselessColumns.USELESS_ID));
                mSet.add(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USELESS+  " ( _id INTEGER PRIMARY KEY AUTOINCREMENT," + "useless_id INTEGER" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing to do now
    }

    public Set<Integer> getSet(){
        return mSet;
    }

    public void addUselessId(final int id){
        if(!mSet.contains(id)){
            mSet.add(id);
            mHandler.post(new Runnable(){
                @Override
                public void run() {
                    ContentValues cv = new ContentValues();
                    cv.put(UselessColumns.USELESS_ID, id);
                    getWritableDatabase().insert(TABLE_USELESS, null, cv);
                }
            });
        }
    }

    static class UselessColumns implements BaseColumns{
        static final String USELESS_ID = "useless_id";
    }
}
