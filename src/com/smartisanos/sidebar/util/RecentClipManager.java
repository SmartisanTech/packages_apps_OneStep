package com.smartisanos.sidebar.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.CopyHistoryItem;

public class RecentClipManager implements IClear{

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

    private List<RecentUpdateListener> mListeners = new ArrayList<RecentUpdateListener>();
    private RecentClipManager(Context context){
        mContext = context;
        mClipboard = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public List<CopyHistoryItem> getCopyList(){
        return mClipboard.getCopyHistory();
    }

    public void addListener(RecentUpdateListener listener){
        mListeners.add(listener);
    }

    public void removeListener(RecentUpdateListener listener){
        mListeners.remove(listener);
    }

    private void notifyListener(){
        for(RecentUpdateListener lis : mListeners){
            lis.onUpdate();
        }
    }

    @Override
    public void clear() {
        synchronized(RecentClipManager.class){
            mClipboard.clearCopyHistory();
            notifyListener();
        }
    }
}
