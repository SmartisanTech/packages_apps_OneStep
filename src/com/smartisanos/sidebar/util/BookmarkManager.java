package com.smartisanos.sidebar.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.smartisanos.sidebar.util.net.NetworkHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarkManager extends DataManager implements IClear {
    private static final LOG log = LOG.getInstance(BookmarkManager.class);

    private volatile static BookmarkManager sInstance;

    public synchronized static BookmarkManager getInstance(Context context){
        if(sInstance == null) {
            synchronized(BookmarkManager.class) {
                if(sInstance == null) {
                    sInstance = new BookmarkManager(context);
                }
            }
        }
        return sInstance;
    }

    private Context mContext;
    private Handler mHandler;
    private List<BookmarkItem> mList = new ArrayList<BookmarkItem>();

    private BookmarkManager(Context context) {
        mContext = context;
        HandlerThread thread = new HandlerThread(BookmarkManager.class.getName());
        thread.start();
        mHandler = new BookmarkManagerHandler(thread.getLooper());
        mHandler.obtainMessage(MSG_LIST_BOOKMARK).sendToTarget();
    }

    public List<BookmarkItem> getBookmarks() {
        List<BookmarkItem> list = new ArrayList<BookmarkItem>();
        synchronized (mList) {
            list.addAll(mList);
        }
        return list;
    }

    public void addBookmark(BookmarkItem item) {
        if (item == null) {
            return;
        }
        mHandler.obtainMessage(MSG_ADD_BOOKMARK, item).sendToTarget();
    }

    public void updateBookmark(BookmarkItem item) {
        if (item == null) {
            return;
        }
        mHandler.obtainMessage(MSG_UPDATE_BOOKMARK, item).sendToTarget();
        notifyListener();
    }

    public void removeBookmark(BookmarkItem item) {
        if (item == null) {
            return;
        }
        mHandler.obtainMessage(MSG_REMOVE_BOOKMARK, item).sendToTarget();
        notifyListener();
    }

    @Override
    public void clear() {
        synchronized (mList) {
            mHandler.obtainMessage(MSG_REMOVE_ALL_BOOKMARK).sendToTarget();
            mList.clear();
        }
        notifyListener();
    }

    private static final int MSG_ADD_BOOKMARK        = 0;
    private static final int MSG_REMOVE_BOOKMARK     = 1;
    private static final int MSG_REMOVE_ALL_BOOKMARK = 2;
    private static final int MSG_UPDATE_BOOKMARK     = 3;
    private static final int MSG_LIST_BOOKMARK       = 4;

    private class BookmarkManagerHandler extends Handler {
        public BookmarkManagerHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_BOOKMARK : {
                    BookmarkItem item = (BookmarkItem) msg.obj;
                    long id = DatabaseHelper.getInstance(mContext).insert(item);
                    if (id <= 0) {
                        break;
                    }
                    if (item.title == null || item.title.trim().length() == 0) {
                        log.error("MSG_ADD_BOOKMARK request title !");
                        List params = new ArrayList();
                        params.add(item);
                        NetworkHandler.postTask(NetworkHandler.ACTION_LOAD_BOOKMARK_TITLE, params);
                    }
                    synchronized (mList) {
                        item.time = System.currentTimeMillis();
                        mList.add(0, item);
                    }
                    notifyListener();
                    break;
                }
                case MSG_REMOVE_BOOKMARK : {
                    BookmarkItem item = (BookmarkItem) msg.obj;
                    DatabaseHelper.getInstance(mContext).remove(item.id);
                    break;
                }
                case MSG_REMOVE_ALL_BOOKMARK : {
                    DatabaseHelper.getInstance(mContext).removeAll();
                    break;
                }
                case MSG_UPDATE_BOOKMARK : {
                    BookmarkItem item = (BookmarkItem) msg.obj;
                    DatabaseHelper.getInstance(mContext).update(item);
                    break;
                }
                case MSG_LIST_BOOKMARK : {
                    List<BookmarkItem> list = DatabaseHelper.getInstance(mContext).list();
                    if (list != null) {
                        synchronized (mList) {
                            mList.clear();
                            mList.addAll(list);
                        }
                    }
                    notifyListener();
                    break;
                }
            }
        }
    }

    public static class BookmarkItem implements Comparable<BookmarkItem> {
        public long id = -1;
        public String title;
        public String content_uri;
        public String source;
        public String fullText;
        public long time;

        @Override
        public int compareTo(BookmarkItem item) {
            if (item == null) {
                return -1;
            }
            if (time == item.time) {
                return 0;
            }
            if (item.time > time) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public static final class DatabaseHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "bookmark";

        private volatile static DatabaseHelper sInstance;

        public synchronized static DatabaseHelper getInstance(Context context){
            if(sInstance == null){
                synchronized(DatabaseHelper.class){
                    if(sInstance == null){
                        sInstance = new DatabaseHelper(context);
                    }
                }
            }
            return sInstance;
        }

        public static final String TABLE_NAME    = "bookmark";
        public static final String ID            = "_id";
        public static final String TITLE         = "title";
        public static final String WEIGHT        = "weight";
        public static final String CONTENT_URI   = "content_uri";
        public static final String SOURCE        = "source";
        public static final String FULL_TEXT     = "full_text";
        public static final String ADD_TIME      = "time";

        public static final String[] columns = new String[] {ID, WEIGHT, ADD_TIME, TITLE, CONTENT_URI, SOURCE, FULL_TEXT};
        private static final Map<String, String> columnProps = new HashMap<String, String>();
        static {
            columnProps.put(ID,                "INTEGER PRIMARY KEY");
            columnProps.put(WEIGHT,            "INTEGER");
            columnProps.put(ADD_TIME,          "LONG");
            columnProps.put(TITLE,             "TEXT");
            columnProps.put(CONTENT_URI,       "TEXT");
            columnProps.put(SOURCE,            "TEXT");
            columnProps.put(FULL_TEXT,         "TEXT");
        }

        private Context mContext;

        private DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = generateCreateSQL(TABLE_NAME, columns, columnProps);
            db.execSQL(sql);
        }

        private static String generateCreateSQL(String table, String[] columns, Map<String, String> props) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("CREATE TABLE IF NOT EXISTS " + table + " (");
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                String prop = props.get(column);
                buffer.append(column);
                buffer.append(" ");
                buffer.append(prop);
                if (i != (columns.length - 1)) {
                    buffer.append(", ");
                }
            }
            buffer.append(");");
            return buffer.toString();
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        }

        private boolean exist(String uri) {
            if (uri == null) {
                return false;
            }
            Cursor cursor = null;
            boolean exist = false;
            try {
                String[] args = new String[] {uri};
                cursor = getReadableDatabase().query(TABLE_NAME, null, CONTENT_URI + "=?", args, null, null, null);
                if (cursor.moveToFirst()) {
                    if (cursor.getCount() > 0) {
                        exist = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return exist;
        }

        private long getRecordId(BookmarkItem item) {
            if (item == null) {
                return -1;
            }
            String where = ID + "=" + item.id;
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_NAME, null, where, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(ID);
                    return cursor.getInt(index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return -1;
        }

        public long insert(BookmarkItem item) {
            if (item == null) {
                return -1;
            }
            ContentValues cv = new ContentValues();
            cv.put(TITLE, item.title);
            cv.put(CONTENT_URI, item.content_uri);
            cv.put(SOURCE, item.source);
            cv.put(FULL_TEXT, item.fullText);
            cv.put(ADD_TIME, item.time);
            boolean exist = exist(item.content_uri);
            if (!exist) {
                item.id = getWritableDatabase().insert(TABLE_NAME, null, cv);
            }
            return item.id;
        }

        public int update(BookmarkItem item) {
            if (item == null) {
                return -1;
            }
            int result = -1;
            ContentValues cv = new ContentValues();
            cv.put(TITLE, item.title);
            cv.put(CONTENT_URI, item.content_uri);
            cv.put(SOURCE, item.source);
            cv.put(FULL_TEXT, item.fullText);
            cv.put(ADD_TIME, item.time);
            boolean exist = exist(item.content_uri);
            if (exist) {
                //update
                result = getWritableDatabase().update(TABLE_NAME, cv, CONTENT_URI + "=?", new String[] {item.content_uri});
            }
            return result;
        }

        public boolean remove(long item) {
            if (item > 0) {
                String where = ID + "=" + item;
                int affectedRow = getWritableDatabase().delete(TABLE_NAME, where, null);
                if (affectedRow > 0) {
                    return true;
                }
            }
            return false;
        }

        public void removeAll() {
            SQLiteDatabase db = getWritableDatabase();
            try {
                db.delete(TABLE_NAME, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static final String DEFAULT_ORDER = ADD_TIME + " DESC";

        public List<BookmarkItem> list() {
            List<BookmarkItem> list = new ArrayList<BookmarkItem>();
            Cursor cursor = null;
            try {
                cursor = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, DEFAULT_ORDER);
                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(ID);
                    int titleIndex = cursor.getColumnIndex(TITLE);
                    int urlIndex = cursor.getColumnIndex(CONTENT_URI);
                    int sourceIndex = cursor.getColumnIndex(SOURCE);
                    int timeIndex = cursor.getColumnIndex(ADD_TIME);
                    do {
                        int id = cursor.getInt(idIndex);
                        String title = cursor.getString(titleIndex);
                        String content_uri = cursor.getString(urlIndex);
                        String source = cursor.getString(sourceIndex);
                        long time = cursor.getLong(timeIndex);
                        BookmarkItem item = new BookmarkItem();
                        item.id = id;
                        item.title = title;
                        item.content_uri = content_uri;
                        item.source = source;
                        item.time = time;
                        list.add(item);
                    } while (cursor.moveToNext());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return list;
        }
    }
}