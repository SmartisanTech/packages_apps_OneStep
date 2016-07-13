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
import android.text.TextUtils;

public class RecentPhotoManager extends DataManager implements IClear{

    private volatile static RecentPhotoManager sInstance;
    public synchronized static RecentPhotoManager getInstance(Context context){
        if(sInstance == null){
            synchronized(RecentPhotoManager.class){
                if(sInstance == null){
                    sInstance = new RecentPhotoManager(context);
                }
            }
        }
        return sInstance;
    }

    private static final String[] thumbCols = new String[] {
        MediaStore.Images.ImageColumns.DATA,
        MediaStore.Images.ImageColumns.MIME_TYPE,
        MediaStore.Images.ImageColumns._ID };

    private static final String DATABASE_NAME = "recent_photo";

    private Context mContext;
    private List<ImageInfo> mList = new ArrayList<ImageInfo>();
    private ClearDatabaseHelper mDatabaseHelper;
    private Handler mHandler;
    private RecentPhotoManager(Context context) {
        mContext = context;
        mHandler = new Handler();
        mDatabaseHelper = new ClearDatabaseHelper(mContext, DATABASE_NAME);
        HandlerThread thread = new HandlerThread(RecentPhotoManager.class.getName());
        thread.start();
        mHandler = new PhotoManagerHandler(thread.getLooper());
        mContext.getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,true, new ImageObserver(mHandler));
        mHandler.obtainMessage(MSG_UPDATE_IMAGE_LIST).sendToTarget();
    }

    public List<ImageInfo> getImageList(){
        synchronized(RecentPhotoManager.class){
            return mList;
        }
    }

    private void updateImageList() {
        List<ImageInfo> imageList = new ArrayList<ImageInfo>();
        Set<Integer> useless = mDatabaseHelper.getSet();
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, thumbCols, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    ImageInfo info = new ImageInfo();
                    info.filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                    info.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE));
                    info.id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
                    if (!TextUtils.isEmpty(info.filePath)&& !TextUtils.isEmpty(info.mimeType)) {
                        if (!useless.contains(info.id)) {
                            imageList.add(info);
                        }
                    }
                } while (cursor.moveToNext());
            }
            Collections.reverse(imageList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        synchronized (RecentPhotoManager.class) {
            mList = imageList;
        }
        notifyListener();
    }

    private class ImageObserver extends ContentObserver{
        public ImageObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.obtainMessage(MSG_UPDATE_IMAGE_LIST).sendToTarget();
        }
    }

    @Override
    public void clear() {
        synchronized (RecentPhotoManager.class) {
            for(ImageInfo fi : mList){
                mDatabaseHelper.addUselessId(fi.id);
            }
        }
        mHandler.obtainMessage(MSG_UPDATE_IMAGE_LIST).sendToTarget();
    }

    private static final int MSG_UPDATE_IMAGE_LIST = 0;
    private class PhotoManagerHandler extends Handler {
        public PhotoManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_IMAGE_LIST:
                updateImageList();
                break;
            }
        }
    }
}
