package com.smartisanos.sidebar.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FileInfo {
    public static final String[] MIMETYPE_BLACKLIST = new String[] { "image/*" };

    private static final String[] BLACKLIST = new String[]{
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/OpenMaster/plugins/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/Tencent/"
    };

    private static final String[] WHITELIST = new String[]{
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/QQfile_recv/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MicroMsg/Download/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/Tencent/QQfile_recv/",
        Environment.getExternalStorageDirectory().getAbsolutePath()+"/Tencent/MicroMsg/Download/"
    };

    private static final Set<String> SUFFIXSET;
    private static final Set<String> PATH_MASK;

    static {
        SUFFIXSET = new HashSet<String>();
        SUFFIXSET.add("xlsx");
        SUFFIXSET.add("key");
        SUFFIXSET.add("pptx");
        SUFFIXSET.add("numbers");
        SUFFIXSET.add("rar");
        SUFFIXSET.add("apk");
        SUFFIXSET.add("7z");
        SUFFIXSET.add("docx");
        SUFFIXSET.add("pages");
        PATH_MASK = new HashSet<String>();
        PATH_MASK.add("backup");
        PATH_MASK.add("crash");
    }

    public String filePath = "";
    public String mimeType;
    public long time = 0;
    public String hashKey;

    public FileInfo(String path){
        this(path, getFileMimeType(new File(path)));
    }

    public FileInfo(String path, String mimeType){
        filePath = path;
        this.mimeType =mimeType;
        time = new File(path).lastModified();
        hashKey = path + time;
    }

    public int getIconId() {
        String name = new File(filePath).getName();
        return MimeUtils.getResId(mimeType, getSuffix(name));
    }

    public boolean valid() {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        if (!isMimeTypeAndFilePathValid(mimeType, filePath)) {
            return false;
        }

        for (int i = 0; i < BLACKLIST.length; i++) {
            if (filePath.startsWith(BLACKLIST[i])) {
                boolean ok = false;
                for (int j = 0; j < WHITELIST.length; j++) {
                    if (filePath.startsWith(WHITELIST[j])) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isMaskFile(File file){
        if(file == null){
            return false;
        }

        String name = file.getName();
        if(name.startsWith(".") || PATH_MASK.contains(name.toLowerCase())){
            return true;
        }
        return isMaskFile(file.getParentFile());
    }

    public static boolean isMimeTypeAndFilePathValid(String mimeType, String filePath) {
        File file = new File(filePath);
        if (!file.isFile() || isMaskFile(file.getParentFile())) {
            return false;
        }
        if (file.getName().toLowerCase().contains("log")) {
            return false;
        }
        if (TextUtils.isEmpty(mimeType)) {
            if (!SUFFIXSET.contains(getSuffix(file.getName()))) {
                return false;
            }
        }
        return true;
    }

    public static String getSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return "";
        }
    }

    public static String getFileMimeType(File file){
        String suffix = getSuffix(file.getName());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
    }
}
