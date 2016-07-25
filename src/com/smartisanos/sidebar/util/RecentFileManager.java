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
import android.provider.MediaStore.Files;
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
    private static final String fileSelection = "(mime_type == 'application/zip') OR (_data LIKE '%.7z') OR (_data LIKE '%.rar') OR (_data LIKE '%.apk') OR (mime_type=='application/msword') OR (mime_type=='application/vnd.ms-powerpoint') OR (mime_type=='text/plain') OR (mime_type=='application/vnd.ms-excel') OR (mime_type=='application/pdf') OR (_data LIKE '%.pptx') OR (_data LIKE '%.key') OR (_data LIKE '%.numbers') OR (_data LIKE '%.xlsx') OR (_data LIKE '%.docx') OR (_data LIKE '%.pages')";

    private static final int MUSIC_TYPE = 1;
    private static final int VIDEO_TYPE = 2;
    private static final int FILE_TYPE = 3;

    private final Uri fileUri = Files.getContentUri(VOLUME_EXTERNAL);
    private final Uri musicUri = Audio.Media.getContentUri(VOLUME_EXTERNAL);
    private final Uri videoUri = Video.Media.getContentUri(VOLUME_EXTERNAL);

    private static final String[] TARGET_DIR = new String[]{
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/QQfile_recv/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MicroMsg/Download/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/Tencent/QQfile_recv/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/Tencent/MicroMsg/Download/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/DingTalk/"
    };

    private List<FileInfo> mList = new ArrayList<FileInfo>();
    private List<FileInfo> mCursorCacheList = new ArrayList<FileInfo>();
    private List<FileInfo> mSearchCacheList = new ArrayList<FileInfo>();

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
            mHandler.obtainMessage(MSG_SEARCH_FILE).sendToTarget();
            mHandler.obtainMessage(MSG_UPDATE_DATABASE_LIST).sendToTarget();
        }
    };

    public List<FileInfo> getFileList(){
        synchronized (RecentFileManager.class) {
            List<FileInfo> recentList = new ArrayList<FileInfo>();
            recentList.addAll(mList);
            return recentList;
        }
    }

    public void startSearchFile(){
        if(mHandler.hasMessages(MSG_SEARCH_FILE)){
            return ;
        }
        mHandler.obtainMessage(MSG_SEARCH_FILE).sendToTarget();
    }

    public void startFileObserver(){
        mContext.getContentResolver().registerContentObserver(fileUri, true, mDatabaseObserver);
        mContext.getContentResolver().registerContentObserver(musicUri, true, mDatabaseObserver);
        mContext.getContentResolver().registerContentObserver(videoUri, true, mDatabaseObserver);
        mContext.getContentResolver().registerContentObserver(RecorderInfo.RECORDER_URI, true, mDatabaseObserver);
        mHandler.obtainMessage(MSG_UPDATE_DATABASE_LIST).sendToTarget();
    }

    public void stopFileObserver() {
        if (mHandler.hasMessages(MSG_SEARCH_FILE)) {
            mHandler.removeMessages(MSG_SEARCH_FILE);
        }
        mContext.getContentResolver().unregisterContentObserver(mDatabaseObserver);
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
        for(int i = 0 ; i < allFile.size() ; i++){
            String filePath = allFile.get(i);
            FileInfo info = new FileInfo(filePath);
            if(info.valid()){
                mSearchCacheList.add(info);
            }
        }
        if(false){
            for(FileInfo info : mSearchCacheList){
                Log.d(TAG, "sdcard.fileinfo -> " + info.filePath);
            }
        }
    }

    private void updateDatabaseContent(){
        mCursorCacheList.clear();
        mCursorCacheList.addAll(getFileInfoByCursor(getContentCursor(MUSIC_TYPE)));
        mCursorCacheList.addAll(getFileInfoByCursor(getContentCursor(VIDEO_TYPE)));
        mCursorCacheList.addAll(getFileInfoByCursor(getContentCursor(FILE_TYPE)));
        mCursorCacheList.addAll(RecorderInfo.getFileInfoFromRecorder(mContext));
        if(false){
            for(FileInfo info : mCursorCacheList){
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

    private Cursor getContentCursor(int type){
        if(type == MUSIC_TYPE){
            return mContext.getContentResolver().query(musicUri, FILE_PROJECTION, null,null, null);
        }else if(type == VIDEO_TYPE){
            return mContext.getContentResolver().query(videoUri, FILE_PROJECTION, null,null, null);
        }else if(type == FILE_TYPE){
            return mContext.getContentResolver().query(fileUri, FILE_PROJECTION, fileSelection,null, null);
        }else{
            return mContext.getContentResolver().query(fileUri, FILE_PROJECTION, fileSelection,null, null);
        }
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
            mHandler.obtainMessage(MSG_UPDATE_DATABASE_LIST).sendToTarget();
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
}
