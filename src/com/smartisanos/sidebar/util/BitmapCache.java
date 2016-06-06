package com.smartisanos.sidebar.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

public class BitmapCache {

    private int mSize = 0;
    private LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(400);

    public BitmapCache(int size) {
        if (size <= 0) {
            size = 1;
        }
        mSize = size;
    }

    public Bitmap getBitmapDirectly(String filepath){
        return mImageCache.get(filepath);
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
        if (mImageCache != null) {
            if (mImageCache.size() > 0) {
                mImageCache.evictAll();
            }
        }
    }

    public synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (mImageCache.get(key) == null) {
            if (key != null && bitmap != null) {
                mImageCache.put(key, bitmap);
            }
        }
    }

    public synchronized void removeImageCache(String filepath) {
        if (filepath != null) {
            if (mImageCache != null) {
                Bitmap bm = mImageCache.remove(filepath);
                if (bm != null){
                    bm.recycle();
                }
            }
        }
    }
}
