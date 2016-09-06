package com.smartisanos.sidebar.util;

import java.util.List;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.CopyHistoryItem;
import android.content.IClipboardListener;
import android.os.RemoteException;

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
        mClipboard.registerListener(mListener);
    }

    public List<CopyHistoryItem> getCopyList(){
        return mClipboard.getCopyHistory();
    }

    @Override
    public void clear() {
        synchronized(RecentClipManager.class){
            mClipboard.clearCopyHistory();
        }
    }

    private IClipboardListener mListener = new IClipboardListener.Stub() {

        @Override
        public void onCopyHistoryChanged() throws RemoteException {
            notifyListener();
        }
    };

    public void refresh() {
        notifyListener();
    }
}
