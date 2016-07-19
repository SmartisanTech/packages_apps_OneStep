package com.smartisanos.sidebar.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Video;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    private static final int MSG_UPDATE_FILE_LIST = 0;
    private static final int MSG_SEARCH_FILE = 1;

    private static final String VOLUME_EXTERNAL = "external";
    private static final String fileSelection = "(mime_type == 'application/zip') OR (_data LIKE '%.7z') OR (_data LIKE '%.rar') OR (_data LIKE '%.apk') OR (mime_type=='application/msword') OR (mime_type=='application/vnd.ms-powerpoint') OR (mime_type=='text/plain') OR (mime_type=='application/vnd.ms-excel') OR (mime_type=='application/pdf') OR (_data LIKE '%.pptx') OR (_data LIKE '%.key') OR (_data LIKE '%.numbers') OR (_data LIKE '%.xlsx') OR (_data LIKE '%.docx') OR (_data LIKE '%.pages')";

    private static final int MUSIC_TYPE = 1;
    private static final int VIDEO_TYPE = 2;
    private static final int FILE_TYPE = 3;

    private final Uri fileUri = Files.getContentUri(VOLUME_EXTERNAL);
    private final Uri musicUri = Audio.Media.getContentUri(VOLUME_EXTERNAL);
    private final Uri videoUri = Video.Media.getContentUri(VOLUME_EXTERNAL);

    private final String QQFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/QQfile_recv/";
    private final String MsgFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MicroMsg/Download/";
    private final String DingFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DingTalk/";

    private File mQQFile = new File(QQFilePath);
    private File mMsgFile = new File(MsgFilePath);
    private File mDingFile = new File(DingFilePath);

    private List<FileInfo> mList = new ArrayList<FileInfo>();

    private List<FileInfo> mCursorCacheList = new ArrayList<FileInfo>();
    private Set<String> mCursorCacheSet = new HashSet<String>();

    private List<FileInfo> mSearchCacheList = new ArrayList<FileInfo>();
    private List<String> mSearchCachePathList = new ArrayList<String>();

    private boolean mAddFile = false;
    private FileObserver mFileObserver;
    private FileClearDatabaseHelper mDatabaseHelper;

    private RecentFileManager(Context context) {
        mContext = context;
        mFileObserver =  new FileObserver(mHandler);
        mDatabaseHelper = new FileClearDatabaseHelper(mContext);
        HandlerThread thread = new HandlerThread(RecentPhotoManager.class.getName());
        thread.start();
        mHandler = new FileManagerHandler(thread.getLooper());
        mDatabaseHelper.initFileClearHelper(mHandler);
    }

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
        mContext.getContentResolver().registerContentObserver(fileUri, true, mFileObserver);
        mContext.getContentResolver().registerContentObserver(musicUri, true, mFileObserver);
        mContext.getContentResolver().registerContentObserver(videoUri, true, mFileObserver);
        mHandler.obtainMessage(MSG_UPDATE_FILE_LIST).sendToTarget();
    }

    public void stopFileObserver() {
        if (mHandler.hasMessages(MSG_SEARCH_FILE)) {
            mHandler.removeMessages(MSG_SEARCH_FILE);
        }
        mContext.getContentResolver().unregisterContentObserver(mFileObserver);
    }

    private void searchFile(){
        mAddFile = false;
        boolean deleteFile = false;
        List<String> qqList = searchDestinationFolder(mQQFile);
        List<String> msgList = searchDestinationFolder(mMsgFile);
        List<String> dingList = searchDestinationFolder(mDingFile);
        updateFilePath(qqList);
        updateFilePath(msgList);
        updateFilePath(dingList);
        for(int i = 0;i < mSearchCachePathList.size() ; i++){
            File file = new File(mSearchCachePathList.get(i));
            if(!file.exists()){
                deleteFile = true;
            }
        }
        if(deleteFile || mAddFile){
            sortRecentFileList();
        }
    }

    private void updateListContent(){
        mCursorCacheList.clear();
        mCursorCacheSet.clear();
        setListDataFromCursor(getContentCursor(MUSIC_TYPE));
        setListDataFromCursor(getContentCursor(VIDEO_TYPE));
        setListDataFromCursor(getContentCursor(FILE_TYPE));
        sortRecentFileList();
    }

    private void updateFilePath(List<String> list){
        for(int i = 0 ; i < list.size() ; i++){
            String filePath = list.get(i);
            File file = new File(filePath);
            String fileName = file.getName();
            String fileTitle = fileName.substring(0,fileName.lastIndexOf("."));
            String mimeType = getFileMimeType(file);
            Long time = file.lastModified();
            String filePathID = filePath + time ;
            if( !fileTitle.equals("") && !mSearchCachePathList.contains(filePath) && mimeType != null ){
                FileInfo info = new FileInfo();
                info.filePath = filePath;
                info.mimeType = mimeType;
                info.time = time;
                info.pathID = filePathID;
                mSearchCacheList.add(info);
                mSearchCachePathList.add(filePath);
                mAddFile = true;
            }
        }
    }

    private void sortRecentFileList(){
        Set<String> clearSet = mDatabaseHelper.getClearSet();
        for(int i = 0 ; i < mSearchCacheList.size() ; i++){
            if( !clearSet.contains(mSearchCacheList.get(i).pathID) ){
                File file = new File(mSearchCachePathList.get(i));
                if( file.exists() && !mCursorCacheSet.contains(mSearchCachePathList.get(i)) ){
                    mCursorCacheList.add(mSearchCacheList.get(i));
                    mCursorCacheSet.add(mSearchCacheList.get(i).filePath);
                }
            }
        }
        FileComparator comparator = new FileComparator();
        Collections.sort(mCursorCacheList,comparator);
        synchronized (RecentFileManager.class) {
            mList.clear();
            mList.addAll(mCursorCacheList);
        }
        notifyListener();
    }


    private class FileComparator implements Comparator<FileInfo> {
        public int compare(FileInfo fileInfo1, FileInfo fileInfo2) {
            if (fileInfo1.time == fileInfo2.time) {
                return 0;
            }
            if (fileInfo1.time < fileInfo2.time) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private void setListDataFromCursor(Cursor cursor){
        if (cursor != null) {
            Set<String> clearSet = mDatabaseHelper.getClearSet();
            if (cursor.moveToFirst()) {
                do {
                    FileInfo info = new FileInfo();
                    info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.DATA));
                    info.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.MIME_TYPE));
                    info.id = cursor.getInt(cursor.getColumnIndexOrThrow(FileColumns._ID));
                    info.size = cursor.getInt(cursor.getColumnIndexOrThrow(FileColumns.SIZE));
                    if (info.valid()) {
                        if(!clearSet.contains(info.pathID)){
                            mCursorCacheList.add(info);
                            mCursorCacheSet.add(info.filePath);
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
            mDatabaseHelper.insertTableData(mList);
            mHandler.obtainMessage(MSG_UPDATE_FILE_LIST).sendToTarget();
        }
    }

    private List<String> searchDestinationFolder(File file){
        List<String> filePathList = new ArrayList<String>();
        if(file.exists()){
            for(File nfile : file.listFiles()){
                if( nfile.exists() && nfile.isFile() ){
                    filePathList.add(nfile.getAbsolutePath());
                }
            }
        }
        return filePathList;
    }

    private String getFileMimeType(File file){
        String suffix = getSuffix(file);
        if (suffix == null) {
            return null;
        }
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (mimeType == null || mimeType.isEmpty()) {
            return null;
        }
        return mimeType;
    }

    private String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

    private class FileManagerHandler extends Handler {

        public FileManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_FILE_LIST:
                    updateListContent();
                    break;
                case MSG_SEARCH_FILE:
                    searchFile();
                    break;
            }
        }
    }

}
