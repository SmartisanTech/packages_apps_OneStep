package com.smartisanos.sidebar.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.provider.BaseColumns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileClearDatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DATABASE_NAME = "hide_database";
    private static final String TABLE_NAME = "hide_table";
    private Handler mHandler;
    private Set<String> mClearSet = new HashSet<String>();

    public FileClearDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME+  " ( _id INTEGER PRIMARY KEY AUTOINCREMENT," + "file_path TEXT " + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // NA
    }

    public void initFileClearHelper(Handler handler){
        mHandler = handler;
        initClearSet();
    }

    public Set<String> getClearSet(){
        synchronized (FileClearDatabaseHelper.class) {
            return mClearSet;
        }
    }

    public void initClearSet(){
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                Cursor cursor = null;
                Set<String> set = new HashSet<String>();
                try{
                    set.clear();
                    cursor = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                String path = cursor.getString(cursor.getColumnIndex(UselessColumns.FILE_PATH));
                                set.add(path);
                            } while (cursor.moveToNext());
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(cursor != null){
                        cursor.close();
                    }
                    if( set.size() != 0 ){
                        synchronized (FileClearDatabaseHelper.class) {
                            mClearSet.addAll(set);
                        }
                    }
                }
            }
        });
    }

    public void insertTableData(final List<FileInfo> hideList){
        if( hideList == null || hideList.size() == 0 ){
            return ;
        }

        synchronized (FileClearDatabaseHelper.class) {
            for(FileInfo info : hideList){
                mClearSet.add(info.pathID);
            }
        }

        mHandler.post(new Runnable(){
            @Override
            public void run() {
                SQLiteDatabase db = null;
                try{
                    db = getWritableDatabase();
                    db.beginTransaction();
                    for( FileInfo fileInfo : hideList){
                        ContentValues cv = new ContentValues();
                        String cachePath = fileInfo.pathID;
                        cv.put(UselessColumns.FILE_PATH, cachePath);
                        db.insert(TABLE_NAME, null, cv);
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

    static class UselessColumns implements BaseColumns{
        static final String FILE_PATH = "file_path";
    }
}
