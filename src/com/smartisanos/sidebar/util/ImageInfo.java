package com.smartisanos.sidebar.util;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

public class ImageInfo {
    public String filePath;
    public String mimeType;
    public int id;

    public ImageInfo(String filePath, int id) {
        this.filePath = filePath;
        this.id = id;
        mimeType = FileInfo.getFileMimeType(filePath);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "image/*";
        }
    }

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
}
