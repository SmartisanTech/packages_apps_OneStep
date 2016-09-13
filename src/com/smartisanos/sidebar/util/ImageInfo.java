package com.smartisanos.sidebar.util;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

public class ImageInfo implements Comparable<ImageInfo> {
    private static final LOG log = LOG.getInstance(ImageInfo.class);

    public String filePath;
    public String mimeType;
    public int id;
    public long time;

    public Uri getContentUri(Context context) {
        if (id != 0) {
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            File file = new File(filePath);
            if (file.isFile()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        }
        return null;
    }

    public void debug() {
        log.error("id ["+id+"], time ["+time+"], mimeType ["+mimeType+"], path ["+filePath+"]");
    }

    @Override
    public int compareTo(ImageInfo info) {
        if (info == null) {
            return -1;
        }
        if (time == info.time) {
            return 0;
        }
        if (info.time > time) {
            return 1;
        } else {
            return -1;
        }
    }
}
