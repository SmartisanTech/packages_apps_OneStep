package com.smartisanos.sidebar.util;

import android.os.Environment;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import libcore.io.Libcore;

public class FileInfo {
    public static final String[] MIMETYPE_BLACKLIST = new String[] { "image/*" };

    private static final String[] BLACKLIST = new String[] {
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/smartisan/textboom",
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/OpenMaster/plugins/",
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/tencent/",
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Tencent/" };

    private static final String[] WHITELIST = new String[] {
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/tencent/QQfile_recv/",
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/tencent/MicroMsg/Download/",
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Tencent/QQfile_recv/",
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Tencent/MicroMsg/Download/" };

    private static final Set<String> PATH_MASK;

    static {
        PATH_MASK = new HashSet<String>();
        PATH_MASK.add("backup");
        PATH_MASK.add("crash");
        PATH_MASK.add("cache");
        PATH_MASK.add("textboom");
        PATH_MASK.add("config");
        PATH_MASK.add("install");
        PATH_MASK.add("applog_bak");
        PATH_MASK.add("map");
        PATH_MASK.add("manifest");// like this -> /storage/emulated0/0/smartisan/bak/manifest/manifest.txt
    }

    public String filePath = "";
    public String mimeType;
    public long lastTime;

    public FileInfo(String path){
        this(path, getFileMimeType(path));
    }

    public FileInfo(String path, String mimeType){
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = getFileMimeType(path);
            if (TextUtils.isEmpty(mimeType)) {
                mimeType = "application/*";
            }
        }
        this.filePath = path;
        this.mimeType =mimeType;
        this.lastTime = getLastTime(filePath);
    }

    public void refresh(){
        this.lastTime = getLastTime(filePath);
    }

    /*
     * do not modify this method !!!!!!
     * we mark fileinfo uselss by hashkey, if this is modified, the database will be invalid :(
     */
    public int getHashKey() {
        return (int) (filePath.hashCode() * 13 + lastTime);
    }

    public int getIconId() {
        String name = new File(filePath).getName();
        return MimeUtils.getResId(mimeType, getSuffix(name));
    }

    public boolean valid() {
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
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(mimeType)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.isFile() || isMaskFile(file.getParentFile())) {
            return false;
        }
        if (file.getName().toLowerCase().contains("log")) {
            return false;
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

    public static String getFileMimeType(String filePath){
        String suffix = getSuffix(new File(filePath).getName());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
    }

    public static long getLastTime(String path){
        long aTime = 0;
        long cTime = 0;
        long mTime = 0;
        try {
            aTime = Libcore.os.stat(path).st_atime * 1000L;
            cTime = Libcore.os.stat(path).st_ctime * 1000L;
            mTime = Libcore.os.stat(path).st_mtime * 1000L;
        } catch (ErrnoException e) {
            // NA;
        }
        return Math.max(Math.max(aTime, cTime), mTime);
    }
}
