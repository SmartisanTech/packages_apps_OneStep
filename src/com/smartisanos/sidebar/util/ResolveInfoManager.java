package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Pair;

import com.smartisanos.sidebar.util.DingDingContact.DatabaseHelper.ContactColumns;
import com.smartisanos.sidebar.util.ResolveInfoGroup.SameGroupComparator;

public class ResolveInfoManager extends SQLiteOpenHelper {
    private volatile static ResolveInfoManager sInstance;
    public static ResolveInfoManager getInstance(Context context){
        if(sInstance == null){
            synchronized(ResolveInfoManager.class){
                if(sInstance == null){
                    sInstance = new ResolveInfoManager(context);
                }
            }
        }
        return sInstance;
    }

    private static final String DB_NAME ="resolveinfo";
    private static final int DB_VERSION = 1;

    private static final Set<String> sBlackList;
    private static final Set<Pair<String, String>> sBlackCompList;
    static {
        sBlackList = new HashSet<String>();
        sBlackList.add("com.android.phone");
        sBlackList.add("com.android.contacts");
        sBlackList.add("com.android.settings");
        sBlackList.add("com.android.gallery3d");

        sBlackCompList = new HashSet<Pair<String, String>>();
        sBlackCompList.add(new Pair<String, String>("com.tencent.mobileqq", "com.tencent.mobileqq.activity.ContactSyncJumpActivity"));
        sBlackCompList.add(new Pair<String, String>("com.tencent.mm", "com.tencent.mm.plugin.accountsync.ui.ContactsSyncUI"));
    }

    private static final String[] sPrePackages= new String[]{
        "com.android.email",
        "com.smartisanos.notes",
        "com.android.mms",
        "com.android.calendar"
    };

    public static final String[] ACTIONS = new String[]{
        Intent.ACTION_VIEW,
        Intent.ACTION_SEND,
        Intent.ACTION_SEND_MULTIPLE
    };

    private Context mContext;
    private List<ResolveInfoGroup> mList = new ArrayList<ResolveInfoGroup>();
    private List<ResolveInfoUpdateListener> mListeners = new ArrayList<ResolveInfoUpdateListener>();
    private Handler mHandler;

    private ResolveInfoManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
        HandlerThread thread = new HandlerThread(ResolveInfoManager.class.getName());
        thread.start();
        mHandler = new ResolveInfoManagerHandler(thread.getLooper());
        mHandler.obtainMessage(MSG_UPDATE_LIST).sendToTarget();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_RESOLVEINFO
                + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "packagename TEXT," + "names TEXT, " + "weight INTEGER"
                + ");");

        int weight = 0;
        for (int i = sPrePackages.length - 1; i >= 0; --i) {
            List<ResolveInfoGroup> list = getAllResolveInfoGroupByPackageName(sPrePackages[i]);
            if (list != null) {
                for (ResolveInfoGroup rig : list) {
                    if (rig != null && rig.size() > 0) {
                        // add to database
                        ContentValues cv = new ContentValues();
                        cv.put(ResolveInfoColumns.PACKAGE_NAME,rig.getPackageName());
                        cv.put(ResolveInfoColumns.COMPONENT_NAMES,rig.getComponentNames());
                        cv.put(ResolveInfoColumns.WEIGHT, weight ++);
                        db.insert(TABLE_RESOLVEINFO, null, cv);
                    }
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // NA
    }

    public void addListener(ResolveInfoUpdateListener listener){
        mListeners.add(listener);
    }

    public void removeListener(ResolveInfoUpdateListener listener){
        mListeners.remove(listener);
    }

    private void notifyUpdate(){
        for(ResolveInfoUpdateListener li : mListeners){
            li.onUpdate();
        }
    }

    public void delete(ResolveInfoGroup rig){
        for(int i = 0; i < mList.size(); ++ i){
            if(mList.get(i).equals(rig)){
                mList.remove(i);
                notifyUpdate();
                mHandler.obtainMessage(MSG_DELETE, rig).sendToTarget();
                return;
            }
        }
    }

    public void addResolveInfoGroup(final ResolveInfoGroup rig){
        if(rig == null || rig.size() <= 0){
            return;
        }
        for(int i = 0; i < mList.size(); ++ i){
            if(mList.get(i).equals(rig)){
                return ;
            }
        }

        mList.add(0, rig);
        mHandler.obtainMessage(MSG_SAVE, rig).sendToTarget();
        notifyUpdate();
    }

    private int getId(ResolveInfoGroup rig) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_RESOLVEINFO,
                null,
                ResolveInfoColumns.PACKAGE_NAME + "=? and "
                        + ResolveInfoColumns.COMPONENT_NAMES + "=?",
                new String[] { rig.getPackageName(), rig.getComponentNames() },
                null, null, null);

        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex(ContactColumns._ID));
        }
        return 0;
    }

    private void deleteFromDatabase(ResolveInfoGroup rig){
        int id = getId(rig);
        if(id != 0){
            getWritableDatabase().delete(TABLE_RESOLVEINFO,
                    ContactColumns._ID + "=?", new String[] { id + "" });
        }
    }

    private void saveToDatabase(ResolveInfoGroup rig){
        ContentValues cv = new ContentValues();
        cv.put(ResolveInfoColumns.PACKAGE_NAME, rig.getPackageName());
        cv.put(ResolveInfoColumns.COMPONENT_NAMES, rig.getComponentNames());
        cv.put(ResolveInfoColumns.WEIGHT, mList.size());
        getWritableDatabase().insert(TABLE_RESOLVEINFO, null, cv);
    }

    private void updateComponentList(){
        TreeMap<Integer, ResolveInfoGroup> map = new TreeMap<Integer, ResolveInfoGroup>();
        Cursor cursor = getReadableDatabase().query(TABLE_RESOLVEINFO, null,null, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String pkgName = cursor.getString(cursor
                                        .getColumnIndex(ResolveInfoColumns.PACKAGE_NAME));
                        String componentNames = cursor.getString(cursor
                                        .getColumnIndex(ResolveInfoColumns.COMPONENT_NAMES));
                        int weight = cursor.getInt(cursor
                                .getColumnIndex(ResolveInfoColumns.WEIGHT));
                        ResolveInfoGroup rig = ResolveInfoGroup.fromData(mContext, pkgName, componentNames);
                        if (rig != null) {
                            map.put(weight, ResolveInfoGroup.fromData(mContext, pkgName, componentNames));
                        } else {
                            getWritableDatabase().delete(TABLE_RESOLVEINFO,
                                    "packagename=? and names=?", new String[] { pkgName, componentNames });
                        }
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            } finally {
                cursor.close();
            }
        }

        synchronized (mList) {
            mList.clear();
            mList.addAll(map.values());
            Collections.reverse(mList);
        }
        notifyUpdate();
    }

    public List<ResolveInfoGroup> getAddedResolveInfoGroup(){
        List<ResolveInfoGroup> ret = new ArrayList<ResolveInfoGroup>();
        ret.addAll(mList);
        return ret;
    }

    public List<ResolveInfoGroup> getUnAddedResolveInfoGroup(){
        List<ResolveInfoGroup> ret = getAllResolveInfoGroupByPackageName(null);
        if(ret != null){
            for(int i = 0; i < ret.size(); ++ i){
                for(int j = 0; j < mList.size(); ++ j)
                    if(ret.get(i).equals(mList.get(j))){
                        ret.remove(i);
                        i -- ;
                        break;
                    }
            }
        }
        return ret;
    }

    public List<ResolveInfoGroup> getAllResolveInfoGroupByPackageName(String pkgName){
        List<ResolveInfo> allri = getAllResolveInfoByPackageName(pkgName);
        if(allri == null || allri.size() <= 0){
            return null;
        }
        SameGroupComparator sgc = new SameGroupComparator();
        Collections.sort(allri, sgc);
        List<ResolveInfoGroup> ret = new ArrayList<ResolveInfoGroup>();
        ret.add(new ResolveInfoGroup(mContext));
        ret.get(0).add(allri.get(0));
        for(int i = 1; i < allri.size(); ++ i){
            if(sgc.compare(ret.get(ret.size() - 1).get(0), allri.get(i)) != 0){
                ret.add(new ResolveInfoGroup(mContext));
            }
            ret.get(ret.size() - 1).add(allri.get(i));
        }
        return ret;
    }

    private List<ResolveInfo> getAllResolveInfoByPackageName(String packageName) {
        List<ResolveInfo> ret = new ArrayList<ResolveInfo>();
        for (String action : ACTIONS) {
            Intent intent = new Intent(action);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setType("*/*");
            intent.setPackage(packageName);
            List<ResolveInfo> infos = mContext.getPackageManager().queryIntentActivities(intent, 0);
            for (ResolveInfo ri : infos) {
                if (sBlackList.contains(ri.activityInfo.packageName)) {
                    // NA
                } else {
                    Pair<String, String> comp = new Pair<String, String>(ri.activityInfo.packageName, ri.activityInfo.name);
                    if (sBlackCompList.contains(comp)) {
                        // NA
                    } else {
                        ret.add(ri);
                    }
                }
            }
        }
        ListUtils.getDistinctList(ret, new MyComparator());
        return ret;
    }

    public void onPackageRemoved(String packageName){
        boolean removed = false;
        for(int i = 0; i < mList.size(); ++ i){
            if(mList.get(i).getPackageName().equals(packageName)){
                mList.remove(i);
                i --;
                removed = true;
            }
        }
        if(removed){
            notifyUpdate();
        }
        getWritableDatabase().delete(TABLE_RESOLVEINFO, "packagename=?", new String[]{packageName});
    }

    public void onPackageAdded(String packageName){
        notifyUpdate();
    }

    public static class MyComparator implements Comparator<ResolveInfo> {
        public final int compare(ResolveInfo a, ResolveInfo b) {
            String pkgA = a.activityInfo.packageName;
            String pkgB = b.activityInfo.packageName;
            if (!pkgA.equals(pkgB)) {
                return pkgA.compareTo(pkgB);
            }
            String nameA = a.activityInfo.name;
            String nameB = b.activityInfo.name;
            if (!nameA.equals(nameB)) {
                return nameA.compareTo(nameB);
            }
            int iconA = a.getIconResource();
            int iconB = b.getIconResource();
            if (iconA != iconB){
                if(iconA < iconB){
                    return -1;
                }else{
                    return 1;
                }
            }else{
                return 0;
            }
        }
    }

    private static final String TABLE_RESOLVEINFO = "resolveinfo";
    static class ResolveInfoColumns implements BaseColumns{
        static final String PACKAGE_NAME = "packagename";
        static final String COMPONENT_NAMES = "names";
        static final String WEIGHT = "weight";
    }

    public interface ResolveInfoUpdateListener{
        void onUpdate();
    }

    private static final int MSG_SAVE = 0;
    private static final int MSG_DELETE = 1;
    private static final int MSG_UPDATE_LIST = 2;
    private class ResolveInfoManagerHandler extends Handler {
        public ResolveInfoManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SAVE:
                saveToDatabase((ResolveInfoGroup) msg.obj);
                break;
            case MSG_DELETE:
                deleteFromDatabase((ResolveInfoGroup) msg.obj);
                break;
            case MSG_UPDATE_LIST:
                updateComponentList();
                break;
            }
        }
    }
}
