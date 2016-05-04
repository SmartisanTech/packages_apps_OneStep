package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.smartisanos.sidebar.util.ResolveInfoGroup.SameGroupComparator;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

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

    private ResolveInfoManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
        updateComponentList();
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

    public void addResolveInfo(ResolveInfoGroup rig){
        addResolveInfoGroup(rig, null);
    }

    public void addResolveInfoGroup(ResolveInfoGroup rig, SQLiteDatabase db){
        if(rig == null || rig.size() <= 0){
            return;
        }
        mList.add(0, rig);
        // add to database
        ContentValues cv = new ContentValues();
        cv.put(ResolveInfoColumns.PACKAGE_NAME, rig.getPackageName());
        cv.put(ResolveInfoColumns.COMPONENT_NAMES, rig.getComponentNames());
        cv.put(ResolveInfoColumns.WEIGHT, mList.size());
        if(db == null){
            db = getWritableDatabase();
        }
        db.insert(TABLE_RESOLVEINFO, null, cv);
        notifyUpdate();
    }

    private void updateComponentList(){
        mList.clear();
        TreeMap<Integer, ResolveInfoGroup> map = new TreeMap<Integer, ResolveInfoGroup>();
        Cursor cursor = getReadableDatabase().query(TABLE_RESOLVEINFO, null,null, null, null, null, null);
        while (cursor.moveToNext()) {
            String pkgName = cursor.getString(cursor.getColumnIndex(ResolveInfoColumns.PACKAGE_NAME));
            String componentNames = cursor.getString(cursor.getColumnIndex(ResolveInfoColumns.COMPONENT_NAMES));
            int weight = cursor.getInt(cursor.getColumnIndex(ResolveInfoColumns.WEIGHT));
            ResolveInfoGroup rig = ResolveInfoGroup.fromData(mContext, pkgName, componentNames);
            if(rig != null){
                map.put(weight, ResolveInfoGroup.fromData(mContext, pkgName, componentNames));
            }else{
                getWritableDatabase().delete(TABLE_RESOLVEINFO, "packagename=? and names=?", new String[]{pkgName, componentNames});
            }
        }
        mList.addAll(map.values());
        Collections.reverse(mList);
    }

    public List<ResolveInfoGroup> getAddedResolveInfoGroup(){
        return mList;
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
        ret.add(new ResolveInfoGroup());
        ret.get(0).add(allri.get(0));
        for(int i = 1; i < allri.size(); ++ i){
            if(sgc.compare(ret.get(ret.size() - 1).get(0), allri.get(i)) != 0){
                ret.add(new ResolveInfoGroup());
            }
            ret.get(ret.size() - 1).add(allri.get(i));
        }
        return ret;
    }

    private List<ResolveInfo> getAllResolveInfoByPackageName(String packageName) {
        List<ResolveInfo> ret = new ArrayList<ResolveInfo>();
        for (String action : ACTIONS) {
            Intent intent = new Intent(action);
            intent.setType("*/*");
            intent.setPackage(packageName);
            List<ResolveInfo> infos = mContext.getPackageManager().queryIntentActivities(intent, 0);
            if (infos != null) {
                ret.addAll(infos);
            }
        }
        ListUtils.getDistinctList(ret, new MyComparator());
        return ret;
    }

    public List<ComponentName> getAllComponent() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("*/*");
        List<ResolveInfo> send_infos = mContext.getPackageManager().queryIntentActivities(intent, 0);
        intent.setAction(Intent.ACTION_VIEW);
        List<ResolveInfo> view_infos = mContext.getPackageManager().queryIntentActivities(intent, 0);

        Set<ComponentName> set = new HashSet<ComponentName>();
        if (send_infos != null) {
            for(ResolveInfo ri : send_infos){
                set.add(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
            }
        }

        if (view_infos != null) {
            for(ResolveInfo ri : view_infos){
                set.add(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
            }
        }

        List<ComponentName> ret = new ArrayList<ComponentName>();
        for (ComponentName cn : set) {
            ret.add(cn);
        }
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
}
