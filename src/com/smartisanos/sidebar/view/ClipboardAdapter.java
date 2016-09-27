package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.CopyHistoryItem;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.Utils;

public class ClipboardAdapter extends BaseAdapter{
    private static LOG log = LOG.getInstance(ClipboardAdapter.class);
    private static final int MAX_ITEM_COUNT = 7;

    private Context mContext;
    private RecentClipManager mClipManager;

    private List<CopyHistoryItem> mCopyHistoryItemList = new ArrayList<CopyHistoryItem>();
    private boolean[] mExpand = new boolean[Utils.Interval.DAY_INTERVAL.length];
    private Map<Integer, List<CopyHistoryItem>> mIntervals = new HashMap<Integer, List<CopyHistoryItem>>();

    private Handler mHandler;
    private IEmpty mEmpty;
    public ClipboardAdapter(Context context, IEmpty empty){
        mContext = context;
        mEmpty = empty;
        mHandler = new Handler(Looper.getMainLooper());
        mClipManager = RecentClipManager.getInstance(mContext);
        mClipManager.addListener(new RecentClipManager.RecentUpdateListener(){
            @Override
            public void onUpdate() {
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        updateDataList();
                    }
                });
            }
        });
        updateDataList();
        notifyEmpty();
    }

    public void shrink() {
        Arrays.fill(mExpand, false);
        notifyDataSetChanged();
    }

    private void updateDataList() {
        mCopyHistoryItemList = mClipManager.getCopyList();
        mIntervals.clear();
        long now = System.currentTimeMillis();
        for (int i = 0; i < mCopyHistoryItemList.size(); i++) {
            CopyHistoryItem item = mCopyHistoryItemList.get(i);
            int interval = Utils.Interval.getInterval(now, item.mTimeStamp);
            List<CopyHistoryItem> list = mIntervals.get(interval);
            if (list == null) {
                list = new ArrayList<CopyHistoryItem>();
                mIntervals.put(interval, list);
            }
            list.add(item);
        }
        notifyDataSetChanged();
    }

    private int getIntervalCount(int i) {
        List<CopyHistoryItem> list = mIntervals.get(i);
        if(list != null) {
            int line = list.size();
            if(mExpand[i]) {
                return line;
            } else {
                return Math.min(line, MAX_ITEM_COUNT);
            }
        }
        return 0;
    }

    private void notifyEmpty() {
        if (mEmpty != null) {
            mEmpty.setEmpty(getCount() == 0);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        notifyEmpty();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int total = 0;
        for(int i = 0; i < Utils.Interval.DAY_INTERVAL.length; ++ i) {
            total += getIntervalCount(i);
        }
        return total;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new ClipboardItemView(mContext);
        }
        ClipboardItemView civ = (ClipboardItemView) convertView;
        civ.reset();

        int now = 0;
        int our_interval = 0;
        int interval_line = 0;
        for(int i = 0; i < Utils.Interval.DAY_INTERVAL.length; ++ i) {
            if(now + getIntervalCount(i) > position) {
                our_interval = i;
                interval_line = position - now;
                break;
            }
            now += getIntervalCount(i);
        }
        if(interval_line == 0) {
            civ.showDate(Utils.Interval.LABEL_INTERVAL[our_interval]);
        }

        List<CopyHistoryItem> intervalInfos = mIntervals.get(our_interval);
        if(interval_line == MAX_ITEM_COUNT - 1 && !mExpand[our_interval] && interval_line != intervalInfos.size() - 1) {
            civ.showMoreTag(new showMoreListener(our_interval, civ, intervalInfos.get(interval_line)));
        } else {
            civ.showItem(intervalInfos.get(interval_line));
        }
        return civ;
    }

    class showMoreListener implements View.OnClickListener {
        private int mInterval;
        private ClipboardItemView mView;
        private CopyHistoryItem mItem;
        public showMoreListener(int interval, ClipboardItemView view, CopyHistoryItem item) {
            mInterval = interval;
            mView = view;
            mItem = item;
        }

        @Override
        public void onClick(View v) {
            mView.reset();
            mView.showItem(mItem);
            mExpand[mInterval] = true;
            notifyDataSetChanged();
        }
    }
}

