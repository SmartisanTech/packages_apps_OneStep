package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class AppManager extends DataManager {
    private volatile static AppManager sInstance;

    public static AppManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AppManager.class) {
                if (sInstance == null) {
                    sInstance = new AppManager(context);
                }
            }
        }
        return sInstance;
    }

    private static final Set<String> sAutoAddPackageSet;
    static {
        // package
        sAutoAddPackageSet = new HashSet<String>();
        sAutoAddPackageSet.add("com.tencent.mm");
        sAutoAddPackageSet.add("com.sina.weibo");
        sAutoAddPackageSet.add("com.tencent.mobileqq");
        sAutoAddPackageSet.add("com.tencent.mobileqqi");
        sAutoAddPackageSet.add("com.smartisanos.notes");
        sAutoAddPackageSet.add("com.android.email");
        sAutoAddPackageSet.add("com.android.calendar");
        sAutoAddPackageSet.add("com.taobao.taobao");
        sAutoAddPackageSet.add("com.evernote");
        sAutoAddPackageSet.add("com.wunderkinder.wunderlistandroid");
        sAutoAddPackageSet.add("com.alibaba.android.rimet");
        sAutoAddPackageSet.add("com.meitu.meiyancamera");
        sAutoAddPackageSet.add("com.netease.cloudmusic");
        sAutoAddPackageSet.add("com.eg.android.AlipayGphone");
        sAutoAddPackageSet.add("com.zhihu.android");
        sAutoAddPackageSet.add("com.zhihu.daily.android");
        sAutoAddPackageSet.add("com.youku.phone");
        sAutoAddPackageSet.add("com.sdu.didi.psnger");
        sAutoAddPackageSet.add("com.ubercab");
        sAutoAddPackageSet.add("com.homelink.android");
        sAutoAddPackageSet.add("com.baidu.BaiduMap");
        sAutoAddPackageSet.add("com.autonavi.minimap");
        sAutoAddPackageSet.add("com.smartisanos.appstore");
        sAutoAddPackageSet.add("com.neuralprisma");
        sAutoAddPackageSet.add("com.twitter.android");
        sAutoAddPackageSet.add("com.instagram.android");
        sAutoAddPackageSet.add("com.whatsapp");
        sAutoAddPackageSet.add("com.facebook.katana");
        sAutoAddPackageSet.add("com.google.android.youtube");
        sAutoAddPackageSet.add("com.android.chrome");
        sAutoAddPackageSet.add("com.android.browser");
        sAutoAddPackageSet.add("com.android.vending");
    }

    private Context mContext;
    private Handler mHandler;
    private List<AppItem> mAddedAppItems = new ArrayList<AppItem>();
    private AppDatabase mDatabase;

    private AppManager(Context context) {
        mContext = context;
        mDatabase = new AppDatabase(mContext);
        HandlerThread thread = new HandlerThread(ResolveInfoManager.class.getName());
        thread.start();
        mHandler = new AppManagerHandler(thread.getLooper());
        mHandler.obtainMessage(MSG_INIT_LIST).sendToTarget();
    }

    public void addAppItem(AppItem item) {
        synchronized (mAddedAppItems) {
            for (int i = 0; i < mAddedAppItems.size(); ++i) {
                if (mAddedAppItems.get(i).equals(item)) {
                    return;
                }
            }
            if (mAddedAppItems.size() == 0) {
                item.setIndex(0);
            } else {
                int maxIndex = mAddedAppItems.get(0).getIndex();
                for (int i = 1; i < mAddedAppItems.size(); ++i) {
                    if (mAddedAppItems.get(i).getIndex() > maxIndex) {
                        maxIndex = mAddedAppItems.get(i).getIndex();
                    }
                }
                item.setIndex(maxIndex + 1);
            }
            mAddedAppItems.add(0, item);
        }
        mHandler.obtainMessage(MSG_SAVE, item).sendToTarget();
        notifyListener();
    }

    public void removeAppItem(AppItem item) {
        synchronized (mAddedAppItems) {
            for (int i = 0; i < mAddedAppItems.size(); ++i) {
                if (mAddedAppItems.get(i).equals(item)) {
                    mAddedAppItems.remove(i);
                    notifyListener();
                    mHandler.obtainMessage(MSG_DELETE, item).sendToTarget();
                    return;
                }
            }
        }
    }

    public boolean isAppItemAdded(AppItem ai) {
        synchronized (mAddedAppItems) {
            for (AppItem addedItem : mAddedAppItems) {
                if (addedItem == ai || addedItem.equals(ai)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<AppItem> getAddedAppItem() {
        List<AppItem> ret = new ArrayList<AppItem>();
        synchronized (mAddedAppItems) {
            ret.addAll(mAddedAppItems);
        }
        return ret;
    }

    public List<AppItem> getUnAddedAppItem() {
        List<ResolveInfo> allInfos = Utils.getAllAppsInfo(mContext);
        List<AppItem> list = new ArrayList<AppItem>();
        synchronized (mAddedAppItems) {
            for (ResolveInfo ri : allInfos) {
                if (mContext.getPackageName().equals(
                        ri.activityInfo.packageName)) {
                    // pass sidebar ourself
                    continue;
                }
                AppItem item = new AppItem(mContext, ri);
                boolean added = false;
                for (AppItem addedItem : mAddedAppItems) {
                    if (addedItem.equals(item)) {
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    list.add(new AppItem(mContext, ri));
                }
            }
        }
        return list;
    }

    public void updateOrder() {
        synchronized (mAddedAppItems) {
            Collections.sort(mAddedAppItems, new AppItem.IndexComparator());
        }
        notifyListener();
        mHandler.obtainMessage(MSG_SAVE_ORDER).sendToTarget();
    }

    public void onPackageRemoved(String packageName){
        synchronized (mAddedAppItems) {
            for (int i = 0; i < mAddedAppItems.size(); ++i) {
                if (mAddedAppItems.get(i).getPackageName().equals(packageName)) {
                    mHandler.obtainMessage(MSG_DELETE, mAddedAppItems.get(i)).sendToTarget();
                    mAddedAppItems.remove(i);
                    i--;
                }
            }
        }
        notifyListener();
    }

    public void onPackageAdded(String packageName) {
        if (sAutoAddPackageSet.contains(packageName)) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setPackage(packageName);
            List<ResolveInfo> ris = mContext.getPackageManager().queryIntentActivities(intent, 0);
            if (ris != null && ris.size() > 0) {
                for (ResolveInfo ri : ris) {
                    addAppItem(new AppItem(mContext, ri));
                }
            }
        }
    }

    private void initList() {
        List<AppItem> list = mDatabase.getAddedAppItem();
        synchronized(mAddedAppItems) {
            mAddedAppItems.clear();
            mAddedAppItems.addAll(list);
        }
        notifyListener();
    }

    private static final int MSG_INIT_LIST = 0;
    private static final int MSG_SAVE = 1;
    private static final int MSG_DELETE = 2;
    private static final int MSG_SAVE_ORDER = 3;

    private class AppManagerHandler extends Handler {
        public AppManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_INIT_LIST:
                initList();
                break;
            case MSG_SAVE:
                mDatabase.saveToDatabase((AppItem) msg.obj);
                break;
            case MSG_DELETE:
                mDatabase.deleteFromDatabase((AppItem) msg.obj);
                break;
            case MSG_SAVE_ORDER:
                mDatabase.saveOrderForList(getAddedAppItem());
                break;
            }
        }
    }

    private final class AppDatabase extends SQLiteOpenHelper {
        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "apps";

        //tables
        private static final String TABLE_APPS = "apps";
        class AppsColumns implements BaseColumns{
            static final String PACKAGE_NAME = "packagename";
            static final String COMPONENT_NAME = "componentname";
            static final String WEIGHT = "weight";
        }

        public AppDatabase(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_APPS
                    + " ( _id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "packagename TEXT," + "componentname TEXT, " + "weight INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // NA
        }

        public List<AppItem> getAddedAppItem() {
            List<AppItem> list = new ArrayList<AppItem>();
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_APPS, null,null, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String pkgName = cursor.getString(cursor.getColumnIndex(AppsColumns.PACKAGE_NAME));
                        String componentName = cursor.getString(cursor.getColumnIndex(AppsColumns.COMPONENT_NAME));
                        int weight = cursor.getInt(cursor.getColumnIndex(AppsColumns.WEIGHT));
                        AppItem ai = AppItem.fromData(mContext, pkgName, componentName);
                        if (ai != null) {
                            ai.setIndex(weight);
                            list.add(ai);
                        } else {
                            getWritableDatabase().delete(TABLE_APPS, "packagename=? and componentname=?", new String[] { pkgName, componentName });
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
            Collections.sort(list, new AppItem.IndexComparator());
            return list;
        }

        private int getId(AppItem item) {
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(
                        TABLE_APPS,
                        null,
                        AppsColumns.PACKAGE_NAME + "=? and "
                                + AppsColumns.COMPONENT_NAME + "=?",
                        new String[] { item.mResolveInfo.activityInfo.packageName,
                                item.mResolveInfo.activityInfo.name }, null, null, null);
                if (cursor.moveToFirst()) {
                    return cursor.getInt(cursor.getColumnIndex(AppsColumns._ID));
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

        public void deleteFromDatabase(AppItem item) {
            int id = getId(item);
            if (id != 0) {
                getWritableDatabase().delete(TABLE_APPS, AppsColumns._ID + "=?", new String[] { id + "" });
            }
        }

        public void saveToDatabase(AppItem item) {
            ContentValues cv = new ContentValues();
            cv.put(AppsColumns.PACKAGE_NAME, item.getPackageName());
            cv.put(AppsColumns.COMPONENT_NAME, item.getComponentName());
            cv.put(AppsColumns.WEIGHT, item.getIndex());
            getWritableDatabase().insert(TABLE_APPS, null, cv);
        }

        public void saveOrderForList(List<AppItem> list) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            try {
                for (AppItem ai : list) {
                    int id = getId(ai);
                    if (id != 0) {
                        ContentValues cv = new ContentValues();
                        cv.put(AppsColumns.PACKAGE_NAME, ai.getPackageName());
                        cv.put(AppsColumns.COMPONENT_NAME,ai.getComponentName());
                        cv.put(AppsColumns.WEIGHT, ai.getIndex());
                        db.update(TABLE_APPS, cv, AppsColumns._ID + "=?",new String[] { id + "" });
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
            }
        }
    }
}
