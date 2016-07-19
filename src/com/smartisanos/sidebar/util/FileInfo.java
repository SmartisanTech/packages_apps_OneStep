package com.smartisanos.sidebar.util;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

public class FileInfo {
    public String filePath = "";
    public String mimeType = null;
    public int id = 0;
    public String title;
    public long time = 0;
    public int size = 0;
    public String pathID;
    private final String log = "log";
    public static final String[] MIMETYPE_BLACKLIST = new String[] { "image/*" };

    private static final String[] mFilePathBlackList = new String[]{
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/OpenMaster/plugins/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MobileQQ/.apollo/role/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MobileQQ/qbiz/html5/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MobileQQ/PhotoPlus/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MobileQQ/ar_model/"
    };

    public boolean valid() {
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(mimeType)) {
            return false;
        }

        File file = new File(filePath);
        if (!file.isFile() || !file.exists()) {
            return false;
        }

        title = file.getName();
        if(title.toLowerCase().contains(log) || (size == 0) ){
            return false;
        }

        for(int i = 0; i <mFilePathBlackList.length ; i++ ){
            if(filePath.contains(mFilePathBlackList[i])){
                return false;
            }
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

}
