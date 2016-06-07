package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

public class RecentFileManager extends DataManager implements IClear{

    private volatile static RecentFileManager sInstance;
    public synchronized static RecentFileManager getInstance(Context context){
        if(sInstance == null){
            synchronized(RecentFileManager.class){
                if(sInstance == null){
                    sInstance = new RecentFileManager(context);
                }
            }
        }
        return sInstance;
    }

    private static final String[] thumbCols = new String[] {
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns._ID};

    private static final String DATABASE_NAME = "recent_file";

    private Context mContext;
    private List<FileInfo> mList = new ArrayList<FileInfo>();
    private ClearDatabaseHelper mDatabaseHelper;
    private Handler mHandler;
    private RecentFileManager(Context context) {
        mContext = context;
        mDatabaseHelper = new ClearDatabaseHelper(mContext, DATABASE_NAME);

        HandlerThread thread = new HandlerThread(RecentFileManager.class.getName());
        thread.start();
        mHandler = new FileManagerHandler(thread.getLooper());
        mContext.getContentResolver().registerContentObserver(MediaStore.Files.getContentUri("external"), true, new FileObserver(mHandler));
        mHandler.obtainMessage(MSG_UPDATE_FILE_LIST).sendToTarget();
    }

    public List<FileInfo> getFileList(){
        synchronized(RecentFileManager.class){
            return mList;
        }
    }

    private void updateFileList() {
        List<FileInfo> filelist = new ArrayList<FileInfo>();
        Set<Integer> useless = mDatabaseHelper.getSet();
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Files.getContentUri("external"), thumbCols, null,
                null, null);
        while (cursor.moveToNext()) {
            FileInfo info = new FileInfo();
            info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
            info.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE));
            info.id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
            if (info.valid() && !useless.contains(info.id)) {
                filelist.add(info);
            }
        }
        cursor.close();
        Collections.reverse(filelist);

        synchronized(RecentFileManager.class){
            mList = filelist;
        }
        notifyListener();
    }

    private class FileObserver extends ContentObserver{
        public FileObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.obtainMessage(MSG_UPDATE_FILE_LIST).sendToTarget();
        }
    }

    @Override
    public void clear() {
        synchronized (RecentFileManager.class) {
            for(FileInfo fi : mList){
                mDatabaseHelper.addUselessId(fi.id);
            }
            mHandler.obtainMessage(MSG_UPDATE_FILE_LIST).sendToTarget();
        }
    }

    private static final int MSG_UPDATE_FILE_LIST = 0;

    private class FileManagerHandler extends Handler {
        public FileManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_FILE_LIST:
                updateFileList();
                break;
            }
        }
    }
}
