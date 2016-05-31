package com.smartisanos.sidebar.util;

import java.util.List;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.CopyHistoryItem;

public class RecentClipManager extends DataManager implements IClear{

    private volatile static RecentClipManager sInstance;
    public synchronized static RecentClipManager getInstance(Context context){
        if(sInstance == null){
            synchronized(RecentClipManager.class){
                if(sInstance == null){
                    sInstance = new RecentClipManager(context);
                }
            }
        }
        return sInstance;
    }

    private Context mContext;
    private ClipboardManager mClipboard;

    private RecentClipManager(Context context){
        mContext = context;
        mClipboard = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public List<CopyHistoryItem> getCopyList(){
        return mClipboard.getCopyHistory();
    }

    @Override
    public void clear() {
        synchronized(RecentClipManager.class){
            mClipboard.clearCopyHistory();
            notifyListener();
        }
    }
}
