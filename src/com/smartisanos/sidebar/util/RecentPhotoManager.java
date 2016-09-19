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
    private static final LOG log = LOG.getInstance(RecentPhotoManager.class);

    private static final String[] DO_NOT_SUPPORT_TYPE = new String[] {
            "dng"
    };

    public static boolean isSupportedType(String path) {
        if (path == null) {
            return false;
        }
        String imgPath = path.toLowerCase();
        for (String type : DO_NOT_SUPPORT_TYPE) {
            if (imgPath.endsWith(type)) {
                return false;
            }
        }
        return true;
    }

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
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Images.ImageColumns._ID,
    };

    private static final String DATABASE_NAME = "UselessPhoto";

    private Context mContext;
    private List<ImageInfo> mList = new ArrayList<ImageInfo>();
    private ClearDatabaseHelper mDatabaseHelper;
    private Handler mHandler;
    private ImageObserver mImageObserver;
    private boolean mRegistered;
    private RecentPhotoManager(Context context) {
        mContext = context;
        HandlerThread thread = new HandlerThread(RecentPhotoManager.class.getName());
        thread.start();
        mHandler = new PhotoManagerHandler(thread.getLooper());
        mDatabaseHelper = new ClearDatabaseHelper(mContext, DATABASE_NAME, mCallback);
        mImageObserver = new ImageObserver(mHandler);
    }

    public void startObserver() {
        synchronized (mImageObserver) {
            if (!mRegistered) {
                mRegistered = true;
                mContext.getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mImageObserver);
                sendMessageIfNotExist(MSG_UPDATE_IMAGE_LIST);
            }
        }
    }

    public void stopObserver() {
        synchronized (mImageObserver) {
            if (mRegistered) {
                mRegistered = false;
                mContext.getContentResolver().unregisterContentObserver(mImageObserver);
                mHandler.removeMessages(MSG_UPDATE_IMAGE_LIST);
            }
        }
    }

    private ClearDatabaseHelper.Callback mCallback = new ClearDatabaseHelper.Callback(){
        @Override
        public void onInitComplete() {
            sendMessageIfNotExist(MSG_UPDATE_IMAGE_LIST);
        }
    };

    public List<ImageInfo> getImageList(){
        List<ImageInfo> list =new ArrayList<ImageInfo>();
        synchronized(RecentPhotoManager.class){
            list.addAll(mList);
        }
        return list;
    }

    private void updateImageList() {
        ThreadVerify.verify(false);
        if (!mDatabaseHelper.isDataSetOk()) {
            return;
        }
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
                    info.time = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN));
                    info.id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
                    if (!TextUtils.isEmpty(info.filePath)&& !TextUtils.isEmpty(info.mimeType)) {
                        if (!useless.contains(info.id) && isSupportedType(info.filePath)) {
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
            sendMessageIfNotExist(MSG_UPDATE_IMAGE_LIST);
        }
    }

    @Override
    public void clear() {
        List<Integer> clearList = new ArrayList<Integer>();
        synchronized (RecentPhotoManager.class) {
            for(ImageInfo fi : mList){
                clearList.add(fi.id);
            }
            mList.clear();
        }
        notifyListener();
        mDatabaseHelper.addUselessId(clearList);
    }

    private void sendMessageIfNotExist(int msgId) {
        if (!mHandler.hasMessages(msgId)) {
            mHandler.obtainMessage(msgId).sendToTarget();
        }
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
