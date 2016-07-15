package com.smartisanos.sidebar.util;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.provider.BaseColumns;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.util.Log;
import android.os.HandlerThread;

public class FileClearDatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION =  1;
    private static final String TABLE_USELESS = "clear_file";
    private Handler mHandler;
    private List<String> mClearList = new ArrayList<String>();

    public FileClearDatabaseHelper(Context context, String name) {
        super(context, name, null, DB_VERSION);
        initClearList();
        HandlerThread thread = new HandlerThread("FileClearDatabaseHelper");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USELESS+  " ( _id INTEGER PRIMARY KEY AUTOINCREMENT," + "useless_path TEXT " + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USELESS);
        onCreate(db);
    }

    public void initClearList(){
        mClearList.clear();
        Cursor cursor = getReadableDatabase().query(TABLE_USELESS, null, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String path = cursor.getString(cursor.getColumnIndex(UselessColumns.USELESS_PATH));
                    mClearList.add(path);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public List<String> getClearList(){
        return mClearList;
    }

    public void clearDataTable(final List<FileInfo> fileList){
        mHandler.post(new Runnable(){
            @Override
            public void run() {
                SQLiteDatabase db = null;
                try{
                    db = getWritableDatabase();
                    List<String> cacheList = new ArrayList<String>();
                    for(int i = 0 ; i < fileList.size() ; i++ ){
                        FileInfo info = fileList.get(i);
                        String usePath = info.filePath+info.time;
                        if(!mClearList.contains(usePath)){
                            cacheList.add(usePath);
                        }
                    }
                    if(cacheList.size() != 0){
                        db.beginTransaction();
                        for(int j = 0 ; j < cacheList.size() ; j++){
                            ContentValues cv = new ContentValues();
                            String cachePath = cacheList.get(j);
                            mClearList.add(cachePath);
                            cv.put(UselessColumns.USELESS_PATH, cachePath);
                            db.insert(TABLE_USELESS, null, cv);
                        }
                        db.setTransactionSuccessful();
                        db.endTransaction();
                        db.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(db != null){
                        db.close();
                    }
                }
            }
        });
    }

    static class UselessColumns implements BaseColumns{
        static final String USELESS_PATH = "useless_path";
    }

}
