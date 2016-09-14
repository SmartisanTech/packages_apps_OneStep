package com.smartisanos.sidebar.util;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

public class BitmapCache {
    private int mSize = 0;
    private LruCache<String, SoftReference<Bitmap>> mImageCache = new LruCache<String, SoftReference<Bitmap>>(100) {
        @Override
        protected void entryRemoved(boolean evicted, String key, SoftReference<Bitmap> oldValue, SoftReference<Bitmap> newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            if (oldValue != null) {
                Bitmap old = oldValue.get();
                if(old != null){
                    old.recycle();
                }
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
            SoftReference<Bitmap> softBp = mImageCache.get(filepath);
            if(softBp != null){
                return softBp.get();
            }
        }
        return null;
    }

    public Bitmap getBitmap(String filepath) {
        Bitmap ret = getBitmapDirectly(filepath);
        if (ret != null) {
            return ret;
        }
        BitmapFactory.Options boundOptions = new BitmapFactory.Options();
        boundOptions.inJustDecodeBounds = true;
        //Just Decode Bounds
        BitmapFactory.decodeFile(filepath, boundOptions);
        int inSampleSize = boundOptions.outHeight > boundOptions.outWidth ? boundOptions.outHeight / mSize
                : boundOptions.outWidth / mSize;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
        if(bitmap == null){
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

    public void clearCache() {
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
                    mImageCache.put(key, new SoftReference<Bitmap>(bitmap));
                }
            }
        }
    }
}
