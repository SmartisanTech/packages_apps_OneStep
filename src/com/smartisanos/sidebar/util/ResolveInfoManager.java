package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Pair;

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
        //Intent.ACTION_VIEW,
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

        // register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_ICON);
        mContext.registerReceiver(mReceiver, filter);
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
        synchronized (mList) {
            for (int i = 0; i < mList.size(); ++i) {
                if (mList.get(i).equals(rig)) {
                    return;
                }
            }
            if (mList.size() == 0) {
                rig.setIndex(0);
            } else {
                int maxIndex = mList.get(0).getIndex();
                for (int i = 1; i < mList.size(); ++i) {
                    if (mList.get(i).getIndex() > maxIndex) {
                        maxIndex = mList.get(i).getIndex();
                    }
                }
                rig.setIndex(maxIndex);
            }
            mList.add(0, rig);
        }
        mHandler.obtainMessage(MSG_SAVE, rig).sendToTarget();
        notifyUpdate();
    }


    public void updateOrder() {
        synchronized (mList) {
            Collections.sort(mList, new ResolveInfoGroup.IndexComparator());
        }
        notifyUpdate();
        mHandler.obtainMessage(MSG_SAVE_ORDER).sendToTarget();
    }

    private void saveOrderForList(){
        List<ResolveInfoGroup> list = getAddedResolveInfoGroup();
        for(ResolveInfoGroup rig : list){
            int id = getId(rig);
            if(id != 0){
                ContentValues cv = new ContentValues();
                cv.put(ResolveInfoColumns.PACKAGE_NAME, rig.getPackageName());
                cv.put(ResolveInfoColumns.COMPONENT_NAMES, rig.getComponentNames());
                cv.put(ResolveInfoColumns.WEIGHT, rig.getIndex());
                getWritableDatabase().update(TABLE_RESOLVEINFO, cv,
                        ResolveInfoColumns._ID + "=?", new String[] { id + "" });
            }
        }
    }

    private int getId(ResolveInfoGroup rig) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(
                    TABLE_RESOLVEINFO,
                    null,
                    ResolveInfoColumns.PACKAGE_NAME + "=? and "
                            + ResolveInfoColumns.COMPONENT_NAMES + "=?",
                    new String[] { rig.getPackageName(), rig.getComponentNames() },
                    null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(ResolveInfoColumns._ID));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private void deleteFromDatabase(ResolveInfoGroup rig){
        int id = getId(rig);
        if(id != 0){
            getWritableDatabase().delete(TABLE_RESOLVEINFO,
                    ResolveInfoColumns._ID + "=?", new String[] { id + "" });
        }
    }

    private void saveToDatabase(ResolveInfoGroup rig){
        ContentValues cv = new ContentValues();
        cv.put(ResolveInfoColumns.PACKAGE_NAME, rig.getPackageName());
        cv.put(ResolveInfoColumns.COMPONENT_NAMES, rig.getComponentNames());
        cv.put(ResolveInfoColumns.WEIGHT, rig.getIndex());
        getWritableDatabase().insert(TABLE_RESOLVEINFO, null, cv);
    }

    private void updateComponentList(){
        List<ResolveInfoGroup> list = new ArrayList<ResolveInfoGroup>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(TABLE_RESOLVEINFO, null,null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String pkgName = cursor.getString(cursor.getColumnIndex(ResolveInfoColumns.PACKAGE_NAME));
                    String componentNames = cursor.getString(cursor.getColumnIndex(ResolveInfoColumns.COMPONENT_NAMES));
                    int weight = cursor.getInt(cursor.getColumnIndex(ResolveInfoColumns.WEIGHT));
                    ResolveInfoGroup rig = ResolveInfoGroup.fromData(mContext, pkgName, componentNames);
                    if (rig != null) {
                        rig.setIndex(weight);
                        list.add(rig);
                    } else {
                        getWritableDatabase().delete(TABLE_RESOLVEINFO, "packagename=? and names=?", new String[] { pkgName, componentNames });
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Collections.sort(list, new ResolveInfoGroup.IndexComparator());
        synchronized (mList) {
            mList.clear();
            mList.addAll(list);
        }
        notifyUpdate();
    }

    public List<ResolveInfoGroup> getAddedResolveInfoGroup() {
        List<ResolveInfoGroup> ret = new ArrayList<ResolveInfoGroup>();
        synchronized (mList) {
            ret.addAll(mList);
        }
        return ret;
    }

    public List<ResolveInfoGroup> getUnAddedResolveInfoGroup(){
        List<ResolveInfoGroup> ret = getAllResolveInfoGroupByPackageName(null);
        synchronized (mList) {
            if (ret != null) {
                for (int i = 0; i < ret.size(); ++i) {
                    for (int j = 0; j < mList.size(); ++j)
                        if (ret.get(i).equals(mList.get(j))) {
                            ret.remove(i);
                            i--;
                            break;
                        }
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
        synchronized (mList) {
            for (int i = 0; i < mList.size(); ++i) {
                if (mList.get(i).getPackageName().equals(packageName)) {
                    mList.remove(i);
                    i--;
                    removed = true;
                }
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
    private static final int MSG_SAVE_ORDER = 3;
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
            case MSG_SAVE_ORDER:
                saveOrderForList();
                break;
            }
        }
    }

    private void onIconChanged(Set<String> packages){
        synchronized(mList){
            for(ResolveInfoGroup rig : mList){
                if(packages.contains(rig.getPackageName())){
                    rig.onIconChanged();
                }
            }
        }
        notifyUpdate();
    }

    private static final String ACTION_UPDATE_ICON = "com.smartisanos.launcher.update_icon";
    private static final String EXTRA_PACKAGENAME = "extra_packagename";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_UPDATE_ICON.equals(action)) {
                String packageNames = intent.getStringExtra(EXTRA_PACKAGENAME);
                if (packageNames != null) {
                    String[] packagearr = packageNames.split(",");
                    if (packagearr != null) {
                        Set<String> packages = new HashSet<String>();
                        for (String pkg : packagearr) {
                            packages.add(pkg);
                        }
                        onIconChanged(packages);
                    }
                }
            }
        }
    };
}
