package com.smartisanos.sidebar.util;

import java.io.File;

import android.content.ClipDescription;
import android.text.TextUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Log;
import com.smartisanos.sidebar.R;

public class FileInfo {
    public String filePath;
    public String mimeType;
    public int id;
    public String title;
    public long time;
    public int size;
    public String pathID;
    private final String log = "log";

    public boolean valid() {
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(mimeType)) {
            return false;
        }

        File file = new File(filePath);
        if (!file.isFile() || !file.exists()) {
            return false;
        }

        for (String str : MIMETYPE_BLACKLIST) {
            if (ClipDescription.compareMimeTypes(mimeType, str)) {
                return false;
            }
        }

        int start=filePath.lastIndexOf("/");
        title = filePath.substring(start+1,filePath.length());
        if(title.toLowerCase().contains(log) || (size == 0) ){
            return false;
        }

        time = file.lastModified();
        pathID = filePath + time;
        return true;
    }

    public int getIconId() {
        String suffix = null;
        String name = new File(filePath).getName();
        if (!TextUtils.isEmpty(name)) {
            String[] sp = name.split("\\.");
            if (sp.length > 1) {
                suffix = sp[sp.length - 1];
            }
        }
        return MimeUtils.getResId(mimeType, suffix);
    }

    public static final String[] MIMETYPE_BLACKLIST = new String[] { "image/*" };
}
