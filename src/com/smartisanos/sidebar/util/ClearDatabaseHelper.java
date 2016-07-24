package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;

public class ClearDatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION =  1;
    private static final String TABLE_USELESS = "useless";
    private Handler mHandler;
    private Set<Integer> mSet = new HashSet<Integer>();
    private Callback mCallback;
    private boolean mIsDataSetOk;

    public ClearDatabaseHelper(Context context, String name, Callback callback) {
        super(context, name, null, DB_VERSION);
        mCallback = callback;
        HandlerThread thread = new HandlerThread(name);
        thread.start();
        mHandler = new DataHandler(thread.getLooper());
        mHandler.obtainMessage(MSG_INIT_DATA).sendToTarget();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USELESS+  " ( _id INTEGER PRIMARY KEY AUTOINCREMENT," + "useless_id INTEGER" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing to do now
    }

    private void initData(){
        Cursor cursor = null;
        List<Integer> list = new ArrayList<Integer>();
        try {
            cursor = getReadableDatabase().query(TABLE_USELESS, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(UselessColumns.USELESS_ID));
                list.add(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        synchronized(mSet){
            mSet.clear();
            mSet.addAll(list);
        }

        mIsDataSetOk = true;
        if(mCallback != null){
            mCallback.onInitComplete();
        }
    }

    public boolean isDataSetOk(){
        return mIsDataSetOk;
    }

    public Set<Integer> getSet(){
        Set<Integer> set = new HashSet<Integer>();
        synchronized(mSet){
            set.addAll(mSet);
        }
        return set;
    }

    public void addUselessId(List<Integer> list){
        List<Integer> local = new ArrayList<Integer>();
        synchronized(mSet){
            for(Integer value : list){
                if(!mSet.contains(value)){
                    mSet.add(value);
                    local.add(value);
                }
            }
        }
        mHandler.obtainMessage(MSG_INSERT_DATA, local).sendToTarget();
    }

    private void insertDatabase(List<Integer> list) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Integer value : list) {
                ContentValues cv = new ContentValues();
                cv.put(UselessColumns.USELESS_ID, value);
                db.insert(TABLE_USELESS, null, cv);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    static class UselessColumns implements BaseColumns{
        static final String USELESS_ID = "useless_id";
    }

    private static final int MSG_INSERT_DATA = 0;
    private static final int MSG_REMOVE_DATA = 1;
    private static final int MSG_INIT_DATA = 2;
    private class DataHandler extends Handler {
        public DataHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INSERT_DATA:
                    List<Integer> list = (List<Integer>) msg.obj;
                    insertDatabase(list);
                    break;
                case MSG_REMOVE_DATA:
                    break;
                case MSG_INIT_DATA:
                    initData();
                    break;
            }
        }
    }

    public interface Callback{
        void onInitComplete();
    }
}
