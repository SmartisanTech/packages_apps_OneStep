package com.smartisanos.sidebar.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.FileInfo;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.Utils;

public class RecentFileAdapter extends BaseAdapter {
    private static final LOG log = LOG.getInstance(RecentFileAdapter.class);

    private static final int[] sNeedExpandNumber = new int[] { 15, 15, 30, 30,
            60 };

    private Context mContext;
    private RecentFileManager mFileManager;
    private List<FileInfo> mFileInfoList = new ArrayList<FileInfo>();
    private boolean[] mExpand = new boolean[Utils.Interval.DAY_INTERVAL.length];
    private Map<Integer, List<FileInfo>> mIntervals = new HashMap<Integer, List<FileInfo>>();

    private Handler mHandler;
    private IEmpty mEmpty;

    public RecentFileAdapter(Context context, IEmpty empty) {
        mContext = context;
        mEmpty = empty;
        mHandler = new Handler(Looper.getMainLooper());
        mFileManager = RecentFileManager.getInstance(mContext);
        mFileManager.addListener(new RecentFileManager.RecentUpdateListener() {
            @Override
            public void onUpdate() {
                mHandler.post(new Runnable() {
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

    private void updateDataList() {
        mFileInfoList = mFileManager.getFileList();
        Collections.sort(mFileInfoList);
        mIntervals.clear();
        long now = System.currentTimeMillis();
        for (int i = 0; i < mFileInfoList.size(); i++) {
            FileInfo info = mFileInfoList.get(i);
            int interval = Utils.Interval.getInterval(now, info.lastTime);
            List<FileInfo> list = mIntervals.get(interval);
            if (list == null) {
                list = new ArrayList<FileInfo>();
                mIntervals.put(interval, list);
            }
            list.add(info);
        }
        notifyDataSetChanged();
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

    public void shrink() {
        Arrays.fill(mExpand, false);
        notifyDataSetChanged();
    }

    private int getIntervalCount(int i) {
        List<FileInfo> list = mIntervals.get(i);
        if(list != null) {
            int line = list.size();
            if(mExpand[i]) {
                return line;
            } else {
                return Math.min(line, sNeedExpandNumber[i]);
            }
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new RecentFileItemView(mContext);
        }
        RecentFileItemView rfiv = (RecentFileItemView) convertView;
        rfiv.reset();

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
            rfiv.showDate(Utils.Interval.LABEL_INTERVAL[our_interval]);
        }

        List<FileInfo> intervalInfos = mIntervals.get(our_interval);
        if(interval_line == sNeedExpandNumber[our_interval] - 1 && !mExpand[our_interval] && interval_line != intervalInfos.size() - 1) {
            rfiv.showMoreTag(new showMoreListener(our_interval, rfiv, intervalInfos.get(interval_line)));
        } else {
            rfiv.showItem(intervalInfos.get(interval_line));
        }
        return rfiv;
    }

    class showMoreListener implements View.OnClickListener {
        private int mInterval;
        private RecentFileItemView mView;
        private FileInfo mInfo;
        public showMoreListener(int interval, RecentFileItemView view, FileInfo fileInfo) {
            mInterval = interval;
            mView = view;
            mInfo = fileInfo;
        }

        @Override
        public void onClick(View v) {
            mView.reset();
            mView.showItem(mInfo);
            mExpand[mInterval] = true;
            notifyDataSetChanged();
        }
    }
}
