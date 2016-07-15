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
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.media.MediaMetadataRetriever;
import java.io.IOException;
import java.io.FileInputStream;

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
    private List<FileInfo> mList = new ArrayList<FileInfo>();

    private final String sortOrder = "date_modified desc";
    private final String fileSelection = "(mime_type == 'application/zip') OR (_data LIKE '%.7z') OR (_data LIKE '%.rar') OR (_data LIKE '%.apk') OR (mime_type=='application/msword') OR (mime_type=='application/vnd.ms-powerpoint') OR (mime_type=='text/plain') OR (mime_type=='application/vnd.ms-excel') OR (mime_type=='application/pdf') OR (_data LIKE '%.pptx') OR (_data LIKE '%.key') OR (_data LIKE '%.numbers') OR (_data LIKE '%.xlsx') OR (_data LIKE '%.docx') OR (_data LIKE '%.pages')";

    private final int MUSIC_TYPE = 1;
    private final int VIDEO_TYPE = 2;
    private final int FILE_TYPE = 3;

    private final String VOLUME_EXTERNAL = "external";
    private final String DATABASE_NAME = "recent_file";

    private static final int MSG_UPDATE_FILE_LIST = 0;
    public static final int MSG_SIDEBAR_START = 1;
    public static final int MSG_SIDEBAR_STOP = 2;
    public static final int MSG_SEARCH_FILE = 3;

    private final Uri fileUri = Files.getContentUri(VOLUME_EXTERNAL);
    private final Uri musicUri = Audio.Media.getContentUri(VOLUME_EXTERNAL);
    private final Uri videoUri = Video.Media.getContentUri(VOLUME_EXTERNAL);

    private final String QQFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/QQfile_recv/";
    private final String MsgFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MicroMsg/";
    private final String DingFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DingTalk/";

    private File mQQFile = new File(QQFilePath);
    private File mMsgFile = new File(MsgFilePath);
    private File mDingFile = new File(DingFilePath);

    private List<String> mQQList = new ArrayList<String>();
    private List<String> mMsgList = new ArrayList<String>();
    private List<String> mDingList = new ArrayList<String>();

    private FileClearDatabaseHelper mDatabaseHelper;
    private List<String> mClearList;
    private boolean mInitSreach = false;
    private boolean mUpdateSearchFile = false;
    private List<FileInfo> mCacheList = new ArrayList<FileInfo>();

    private RecentFileManager(Context context) {
        mContext = context;
        mDatabaseHelper = new FileClearDatabaseHelper(mContext, DATABASE_NAME);
        mHandler = new FileManagerHandler();
        initSreachFileList();
        mContext.getContentResolver().registerContentObserver(fileUri, true, new FileObserver(mHandler));
        mContext.getContentResolver().registerContentObserver(musicUri, true, new FileObserver(mHandler));
        mContext.getContentResolver().registerContentObserver(videoUri, true, new FileObserver(mHandler));
        mHandler.obtainMessage(MSG_UPDATE_FILE_LIST).sendToTarget();
    }

    public List<FileInfo> getFileList(){
        synchronized(RecentFileManager.class){
            return mList;
        }
    }


    public void sendSideBarState(int message){
        if(message == MSG_SEARCH_FILE){
            FileSreachThread mFileSreachThread = new FileSreachThread();
            mFileSreachThread.start();
        }
    }

    private void updateListContent(){
        synchronized (RecentFileManager.class) {
            mList.clear();
            mClearList = mDatabaseHelper.getClearList();
            setListDataFromCursor(getContentCursor(MUSIC_TYPE));
            setListDataFromCursor(getContentCursor(VIDEO_TYPE));
            setListDataFromCursor(getContentCursor(FILE_TYPE));
            FileComparator comparator = new FileComparator();
            Collections.sort(mList,comparator);
        }
        notifyListener();
    }

    private void updateSearchListContent(){
        synchronized (RecentFileManager.class) {
            FileComparator comparator = new FileComparator();
            for(int i = 0;i<mCacheList.size();i++){
                if( !mClearList.contains(mCacheList.get(i).pathID) ){
                    mList.add(mCacheList.get(i));
                }
            }
            Collections.sort(mList,comparator);
        }
        notifyListener();
    }

    private class FileComparator implements Comparator<FileInfo> {
         public int compare(FileInfo fileInfo1, FileInfo fileInfo2) {
             if ( fileInfo1.time < fileInfo2.time ) {
                 return 1;
             } else {
                 return -1;
             }
         }
     }

    private void setListDataFromCursor(Cursor cursor){
        if (cursor != null) {

            if (cursor.moveToFirst()) {
                do {

                    FileInfo info = new FileInfo();
                    info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.DATA));
                    info.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.MIME_TYPE));
                    info.id = cursor.getInt(cursor.getColumnIndexOrThrow(FileColumns._ID));
                    info.size = cursor.getInt(cursor.getColumnIndexOrThrow(FileColumns.SIZE));
                    if ( info.valid() ) {
                        if(!mClearList.contains(info.pathID)){
                            mList.add(info);
                        }
                    }
                } while (cursor.moveToNext());

            }
            cursor.close();
        }
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
            mDatabaseHelper.clearDataTable(mList);
            mHandler.obtainMessage(MSG_UPDATE_FILE_LIST).sendToTarget();
        }
    }

    private class FileSreachThread extends Thread{
        public void run() {
            mCacheList.clear();
            mUpdateSearchFile = false;
            sreachDataFile(mQQFile,mQQList);
            if(mUpdateSearchFile){
                mHandler.obtainMessage(MSG_SEARCH_FILE).sendToTarget();
            }
        }
    }

    private void initSreachFileList(){
        if(!mInitSreach){

            if(mQQFile.exists()){
                for(File qqFile : mQQFile.listFiles()){
                    if(qqFile.isFile()){
                        mQQList.add(qqFile.getAbsolutePath());
                    }
                }
            }

            if(mMsgFile.exists()){
                for(File msgFile : mMsgFile.listFiles()){
                    if(msgFile.isFile()){
                        mMsgList.add(msgFile.getAbsolutePath());
                    }
                }
            }

            if(mDingFile.exists()){
                for(File dingFile : mDingFile.listFiles()){
                    if(dingFile.isFile()){
                        mDingList.add(dingFile.getAbsolutePath());
                    }
                }
            }
            mInitSreach = true;
        }
    }

    private void sreachDataFile(File file,List<String> list){
        if(file.exists()){
            boolean fileAdd = false;
            for(File nfile : file.listFiles()){
                if( nfile.exists() ){
                    String filePath = nfile.getAbsolutePath();
                    int start = filePath.lastIndexOf("/");
                    int end = filePath.lastIndexOf(".");
                    String fileTitle = filePath.substring(start+1,end);
                    if( !list.contains(filePath) && !fileTitle.equals("") ){
                        FileInfo info = new FileInfo();
                        info.filePath = nfile.getAbsolutePath();
                        info.mimeType = getFileMimeType(nfile.getAbsolutePath());
                        info.size = getFileSize(nfile);
                        info.time = file.lastModified();
                        info.pathID = info.filePath + info.time;
                        if(!mClearList.contains(info.pathID)){
                            mCacheList.add(info);
                            mUpdateSearchFile = true;
                            fileAdd = true;
                        }
                    }
                }
            }

            if(fileAdd){
                list.clear();
                for(File nfile : file.listFiles()){
                    String filePath = nfile.getAbsolutePath();
                    int start = filePath.lastIndexOf("/");
                    int end = filePath.lastIndexOf(".");
                    String fileTitle = filePath.substring(start+1,end);
                    if( nfile.exists() && !fileTitle.equals("")){
                        list.add(filePath);
                    }
                }
            }
        }
    }

    private String getFileMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "text/plain";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
         }
        return mime;
    }



    private int getFileSize(File file) {
        try {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            return fis.available();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private class FileManagerHandler extends Handler {

        public FileManagerHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_FILE_LIST:
                    updateListContent();
                    break;

                case MSG_SIDEBAR_START:
                    break;

                case MSG_SIDEBAR_STOP:

                    break;
                case MSG_SEARCH_FILE:
                    updateSearchListContent();
                    break;
            }
        }
    }

}
