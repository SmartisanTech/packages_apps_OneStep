package com.smartisanos.sidebar.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

public class BitmapCache {
    private static final LOG log = LOG.getInstance(BitmapCache.class);

    private int mSize = 0;

    private LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(100) {

        @Override
        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            if (oldValue != null) {
                oldValue.recycle();
            }
        }
    };

    public BitmapCache(int size) {
        if (size <= 0) {
            size = 1;
        }
        mSize = size;
    }

    public Bitmap getBitmapDirectly(String filepath){
        synchronized (mImageCache) {
            return mImageCache.get(filepath);
        }
    }

    public synchronized Bitmap getBitmap(String filepath) {
        Bitmap ret = mImageCache.get(filepath);
        if (ret != null) {
            return ret;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeFile(filepath, options);
        options.inSampleSize = options.outHeight > options.outWidth ? options.outHeight / mSize
                : options.outWidth / mSize;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(filepath, options);
        if(bitmap == null){
            //TODO remove this item
            return null;
        }
        if (bitmap.getWidth() != bitmap.getHeight()) {
            int size = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
            Bitmap newBp = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - size) / 2, (bitmap.getHeight() - size) / 2, size, size);
            bitmap.recycle();
            bitmap = newBp;
        }
        addBitmapToMemoryCache(filepath, bitmap);
        return bitmap;
    }

    public synchronized void clearCache() {
        synchronized (mImageCache) {
            if (mImageCache != null) {
                if (mImageCache.size() > 0) {
                    mImageCache.evictAll();
                }
            }
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key != null && bitmap != null) {
            synchronized (mImageCache) {
                if (mImageCache.get(key) == null) {
                    mImageCache.put(key, bitmap);
                }
            }
        }
    }
}
