package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public final class ImageLoader {
    private static final int THREAD_NUM = 3;
    private static final int MSG_IMAGE_LOAD = 1;
    private BitmapCache mCache;
    private List<Handler> mHandlers;
    public ImageLoader(int photoSize) {
        mCache = new BitmapCache(photoSize);
        mHandlers = new ArrayList<Handler>();
        for(int i = 0; i < THREAD_NUM; ++ i){
            HandlerThread handlerthread = new HandlerThread("imageloader" + i, android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
            handlerthread.start();
            mHandlers.add(new ImageHandler(handlerthread.getLooper()));
        }
    }

    public void loadImage(String filepath, Callback callback) {
        Bitmap cur = mCache.getBitmapDirectly(filepath);
        if(cur != null){
            callback.onLoadComplete(filepath, cur);
            return;
        }
        LoadItem item = new LoadItem();
        item.filePath = filepath;
        item.callback = callback;
        Handler handler = mHandlers.get(((filepath.hashCode() % mHandlers.size()) + mHandlers.size()) % mHandlers.size());
        Message msg = handler.obtainMessage(MSG_IMAGE_LOAD, item);
        handler.sendMessageAtFrontOfQueue(msg);
    }

    private final class ImageHandler extends Handler {
        public ImageHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_IMAGE_LOAD:
                LoadItem item = (LoadItem) msg.obj;
                Bitmap bm = mCache.getBitmap(item.filePath);
                item.callback.onLoadComplete(item.filePath, bm);
            }
        }
    }

    private final class LoadItem {
        String filePath;
        Callback callback;
    }

    public interface Callback {
        void onLoadComplete(String filePath, Bitmap bitmap);
    }

    public void clearCache() {
        mCache.clearCache();
    }
}