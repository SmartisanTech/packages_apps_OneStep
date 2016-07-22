package com.smartisanos.sidebar.util;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileClearDatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DATABASE_NAME = "hide_database";
    private static final String TABLE_NAME = "hide_table";
    private Context mContext;
    private Handler mHandler;
    private Set<String> mClearSet = new HashSet<String>();

    public FileClearDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        mContext = context;
        HandlerThread thread = new HandlerThread(RecentPhotoManager.class.getName());
        thread.start();
        mHandler = new DataHandler(thread.getLooper());
        mHandler.obtainMessage(MSG_INIT_DATA).sendToTarget();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME+  " ( _id INTEGER PRIMARY KEY AUTOINCREMENT," + "file_path TEXT " + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // NA
    }

    public Set<String> getClearSet(){
        Set<String> set = new HashSet<String>();
        synchronized (FileClearDatabaseHelper.class) {
            set.addAll(mClearSet);
        }
        return set;
    }

    private void initClearSet() {
        Cursor cursor = null;
        Set<String> set = new HashSet<String>();
        try {
            cursor = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String path = cursor.getString(cursor.getColumnIndex(UselessColumns.FILE_PATH));
                        set.add(path);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (set.size() != 0) {
                synchronized (FileClearDatabaseHelper.class) {
                    mClearSet.addAll(set);
                }
            }
        }
        RecentFileManager.getInstance(mContext).onClearSetChange();
    }

    public void insertTableData(List<FileInfo> hideList){
        final List<String> clearList = new ArrayList<String>();
        for(FileInfo info: hideList){
            clearList.add(info.hashKey);
        }

        synchronized (FileClearDatabaseHelper.class) {
            mClearSet.addAll(clearList);
        }
        mHandler.obtainMessage(MSG_INSERT_DATA, clearList).sendToTarget();
    }

    private void insertDatabase(List<String> list) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();
            for (String id : list) {
                ContentValues cv = new ContentValues();
                cv.put(UselessColumns.FILE_PATH, id);
                db.insert(TABLE_NAME, null, cv);
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

    /**
    public void deleteTableData(final List<String> deleteList){
        if( deleteList == null && deleteList.size() == 0 ){
            return ;
        }
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                SQLiteDatabase db = null;
                try{
                    db = getWritableDatabase();
                    db.beginTransaction();
                    for( int i = 0 ; i < deleteList.size() ; i++ ){
                        String[] args = {deleteList.get(i)};
                        db.delete(TABLE_NAME, "file_path=?", args);
                    }
                    db.setTransactionSuccessful();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(db != null){
                        db.endTransaction();
                        db.close();
                    }
                }
            }
        });
    }
    */

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
                    List<String> list = (List<String>) msg.obj;
                    insertDatabase(list);
                    break;
                case MSG_REMOVE_DATA:
                    break;
                case MSG_INIT_DATA:
                    initClearSet();
                    break;
            }
        }
    }

    static class UselessColumns implements BaseColumns{
        static final String FILE_PATH = "file_path";
    }
}
