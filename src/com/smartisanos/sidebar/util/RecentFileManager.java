package com.smartisanos.sidebar.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Video;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecentFileManager extends DataManager implements IClear{

    private static final String TAG = RecentFileManager.class.getName();
    private static final String DB_NAME = "UselessFile";

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

    private static final String[] FILE_PROJECTION = new String[] {
        FileColumns._ID,
        FileColumns.TITLE,
        FileColumns.DATA,
        FileColumns.SIZE,
        FileColumns.DATE_MODIFIED,
        FileColumns.MIME_TYPE,
    };

    private Context mContext;
    private Handler mHandler;

    private static final int MSG_UPDATE_DATABASE_LIST = 0;
    private static final int MSG_SEARCH_FILE = 1;

    private static final String VOLUME_EXTERNAL = "external";
    private static final Uri[] URIS = new Uri[] {
        Audio.Media.getContentUri(VOLUME_EXTERNAL),
        Video.Media.getContentUri(VOLUME_EXTERNAL)
    };

    private static final String[] TARGET_DIR = new String[]{
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/QQfile_recv/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MicroMsg/Download/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/DingTalk/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/微盘/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/yunpan/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/BaiduNetdisk/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/",
    };

    private List<FileInfo> mList = new ArrayList<FileInfo>();
    private List<FileInfo> mCursorCacheList = new ArrayList<FileInfo>();
    private List<FileInfo> mSearchCacheList = new ArrayList<FileInfo>();

    private boolean mRegistered;
    private DatabaseObserver mDatabaseObserver;
    private ClearDatabaseHelper mDatabaseHelper;

    private RecentFileManager(Context context) {
        mContext = context;
        HandlerThread thread = new HandlerThread(RecentFileManager.class.getName());
        thread.start();
        mHandler = new FileManagerHandler(thread.getLooper());
        mDatabaseObserver =  new DatabaseObserver(mHandler);
        mDatabaseHelper = new ClearDatabaseHelper(mContext,DB_NAME, mCallback);
    }

    private ClearDatabaseHelper.Callback mCallback = new ClearDatabaseHelper.Callback() {
        @Override
        public void onInitComplete() {
            sendMessageIfNotExist(MSG_SEARCH_FILE);
            sendMessageIfNotExist(MSG_UPDATE_DATABASE_LIST);
        }
    };

    public List<FileInfo> getFileList(){
        synchronized (RecentFileManager.class) {
            List<FileInfo> recentList = new ArrayList<FileInfo>();
            recentList.addAll(mList);
            return recentList;
        }
    }

    public void startSearchFile() {
        sendMessageIfNotExist(MSG_SEARCH_FILE);
    }

    public void startFileObserver(){
        synchronized (mDatabaseObserver) {
            if (!mRegistered) {
                for (Uri uri : URIS) {
                    mContext.getContentResolver().registerContentObserver(uri,
                            true, mDatabaseObserver);
                }
                mContext.getContentResolver().registerContentObserver(
                        RecorderInfo.RECORDER_URI, true, mDatabaseObserver);
                mRegistered = true;
            }
        }
        sendMessageIfNotExist(MSG_UPDATE_DATABASE_LIST);
    }

    public void stopFileObserver() {
        mHandler.removeMessages(MSG_SEARCH_FILE);
        synchronized (mDatabaseObserver) {
            if (mRegistered) {
                mContext.getContentResolver().unregisterContentObserver(mDatabaseObserver);
                mRegistered = false;
            }
        }
    }

    public void onClearSetChange(){
        sortRecentFileList();
    }

    private List<String> searchDestinationFolder(File dir) {
        List<String> filePathList = new ArrayList<String>();
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    filePathList.add(file.getAbsolutePath());
                } else {
                    filePathList.addAll(searchDestinationFolder(file));
                }
            }
        }
        return filePathList;
    }

    private void searchFile(){
        List<String> allFile = new ArrayList<String>();
        for(String path : TARGET_DIR){
            allFile.addAll(searchDestinationFolder(new File(path)));
        }
        mSearchCacheList.clear();
        for(int i = 0 ; i < allFile.size() ; i++){
            String filePath = allFile.get(i);
            FileInfo info = new FileInfo(filePath);
            if(info.valid()){
                mSearchCacheList.add(info);
            }
        }
        if (false) {
            Log.d(TAG, "dump search cache list !");
            for (FileInfo info : mSearchCacheList) {
                Log.d(TAG, "sdcard.fileinfo -> " + info.filePath);
            }
        }
    }

    private void updateDatabaseContent() {
        ThreadVerify.verify(false);
        mCursorCacheList.clear();
        for (Uri uri : URIS) {
            mCursorCacheList.addAll(getFileInfoByCursor(mContext
                    .getContentResolver().query(uri, FILE_PROJECTION, null, null, null)));
        }
        mCursorCacheList.addAll(RecorderInfo.getFileInfoFromRecorder(mContext));
        if (false) {
            Log.d(TAG, "dump cursor cache list !");
            for (FileInfo info : mCursorCacheList) {
                Log.d(TAG, "database.fileinfo -> " + info.filePath);
            }
        }
    }

    private void sortRecentFileList() {
        if(!mDatabaseHelper.isDataSetOk()){
            return;
        }

        List<FileInfo> allInfo = new ArrayList<FileInfo>();
        Set<Integer> clearSet = mDatabaseHelper.getSet();
        Set<String> dataSet = new HashSet<String>();
        for (FileInfo info : mCursorCacheList) {
            info.refresh();
            if (!clearSet.contains(info.getHashKey())
                    && !dataSet.contains(info.filePath)) {
                dataSet.add(info.filePath);
                allInfo.add(info);
            }
        }

        for (FileInfo info : mSearchCacheList) {
            info.refresh();
            if (!clearSet.contains(info.getHashKey())
                    && !dataSet.contains(info.filePath)) {
                dataSet.add(info.filePath);
                allInfo.add(info);
            }
        }

        FileComparator comparator = new FileComparator();
        Collections.sort(allInfo, comparator);
        synchronized (RecentFileManager.class) {
            mList.clear();
            mList.addAll(allInfo);
        }
        notifyListener();
    }

    private class FileComparator implements Comparator<FileInfo> {
        public int compare(FileInfo fileInfo1, FileInfo fileInfo2) {
            long time1 = fileInfo1.lastTime;
            long time2 = fileInfo2.lastTime;
            if (time1 == time2) {
                return 0;
            }
            if (time1 < time2) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private List<FileInfo> getFileInfoByCursor(Cursor cursor) {
        List<FileInfo> infos = new ArrayList<FileInfo>();
        if (cursor != null) {
            try {
                Set<Integer> clearSet = mDatabaseHelper.getSet();
                if (cursor.moveToFirst()) {
                    do {
                        int size = cursor.getInt(cursor.getColumnIndexOrThrow(FileColumns.SIZE));
                        if (size == 0) {
                            continue;
                        }
                        String filePath = cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.DATA));
                        String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.MIME_TYPE));
                        FileInfo info = new FileInfo(filePath, mimeType);
                        if (info.valid()) {
                            if (!clearSet.contains(info.getHashKey())) {
                                infos.add(info);
                            }
                        }
                    } while (cursor.moveToNext());
                }
            } catch(Exception e){
                    // NA
            } finally {
                cursor.close();
            }
        }
        return infos;
    }

    private class ReceiveFileOberver extends FileObserver{
        public ReceiveFileOberver(String path) {
            super(path);
        }

        @Override
        public void onEvent(int event, String path) {
            // NA
        }
    }

    private class DatabaseObserver extends ContentObserver{
        public DatabaseObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            sendMessageIfNotExist(MSG_UPDATE_DATABASE_LIST);
        }
    }

    private void sendMessageIfNotExist(int msgId) {
        if (!mHandler.hasMessages(msgId)) {
            mHandler.obtainMessage(msgId).sendToTarget();
        }
    }

    private class FileManagerHandler extends Handler {

        public FileManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_DATABASE_LIST:
                    updateDatabaseContent();
                    sortRecentFileList();
                    break;
                case MSG_SEARCH_FILE:
                    searchFile();
                    sortRecentFileList();
                    break;
            }
        }
    }

    @Override
    public void clear() {
        synchronized (RecentFileManager.class) {
            List<Integer> clearList = new ArrayList<Integer>();
            for(FileInfo fi : mList){
                clearList.add(fi.getHashKey());
            }
            mDatabaseHelper.addUselessId(clearList);
            mList.clear();
        }
        notifyListener();
    }

    public void refresh() {
        notifyListener();
    }
}
