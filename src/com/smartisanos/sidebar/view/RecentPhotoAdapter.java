package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.DataManager.RecentUpdateListener;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.ImageLoader;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import smartisanos.util.SidebarUtils;

public class RecentPhotoAdapter extends BaseAdapter {
    private static final LOG log = LOG.getInstance(RecentPhotoAdapter.class);

    private static final int[] sNeedExpandNumber = new int[] { 30, 30, 60, 60,
            60 };

    private Context mContext;
    private RecentPhotoManager mPhotoManager;
    private List<ImageInfo> mImageInfoList = new ArrayList<ImageInfo>();
    private boolean[] mExpand = new boolean[Utils.Interval.DAY_INTERVAL.length];
    private Map<Integer, List<ImageInfo>> mIntervals = new HashMap<Integer, List<ImageInfo>>();
    private int mFirstInterval;

    private ImageLoader mImageLoader;
    private Handler mHandler;
    private IEmpty mEmpty;

    public RecentPhotoAdapter(Context context, IEmpty empty) {
        mContext = context;
        mEmpty = empty;
        mHandler = new Handler(Looper.getMainLooper());
        mPhotoManager = RecentPhotoManager.getInstance(mContext);
        int maxPhotoSize = mContext.getResources().getDimensionPixelSize(R.dimen.recent_photo_size);
        mImageLoader = new ImageLoader(maxPhotoSize);
        mImageInfoList = mPhotoManager.getImageList();
        updateDataList();
        mPhotoManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        mImageInfoList = mPhotoManager.getImageList();
                        updateDataList();
                        notifyDataSetChanged();
                    }
                });
            }
        });
        notifyEmpty();
    }

    public void shrink() {
        Arrays.fill(mExpand, false);
        notifyDataSetChanged();
    }

    private void updateDataList() {
        mIntervals.clear();
        mFirstInterval = Integer.MAX_VALUE;
        long now = System.currentTimeMillis();
        for (int i = 0; i < mImageInfoList.size(); i++) {
            ImageInfo info = mImageInfoList.get(i);
            int interval = Utils.Interval.getInterval(now, info.time);
            List<ImageInfo> list = mIntervals.get(interval);
            if (list == null) {
                list = new ArrayList<ImageInfo>();
                mIntervals.put(interval, list);
                mFirstInterval = Math.min(mFirstInterval, interval);
            }
            list.add(info);
        }
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
        View view = convertView;
        ViewHolder vh = null;
        if(view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.photo_line, null);
            vh = new ViewHolder();
            vh.dateView = (TextView)view.findViewById(R.id.date);
            vh.subView[0] = (PhotoLineSubView)view.findViewById(R.id.photo_line_sub_view_1);
            vh.subView[1] = (PhotoLineSubView)view.findViewById(R.id.photo_line_sub_view_2);
            vh.subView[2] = (PhotoLineSubView)view.findViewById(R.id.photo_line_sub_view_3);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }
        vh.reset();

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
            // show date
            vh.dateView.setText(Utils.Interval.LABEL_INTERVAL[our_interval]);
            vh.dateView.setVisibility(View.VISIBLE);
        }

        List<ImageInfo> intervalInfos = mIntervals.get(our_interval);
        boolean expand = mExpand[our_interval];
        int startIndex = interval_line * 3;
        int needExpandNumber = sNeedExpandNumber[our_interval];
        if(our_interval == mFirstInterval) {
            startIndex -= 1;
            needExpandNumber -- ;
        }

        int starti = 0;
        if(position == 0) {
            vh.subView[0].showOpenGallery();
            starti ++ ;
        }

        int size = intervalInfos.size();
        if (!expand) {
            size = Math.min(needExpandNumber, intervalInfos.size());
        }
        for(int i = starti; i < 3; ++ i) {
            int index = startIndex + i;
            if(index < size) {
                if(interval_line * 3 + i == sNeedExpandNumber[our_interval] - 1) {
                    if(intervalInfos.size() > needExpandNumber && !expand) {
                        // show expand button;
                        vh.subView[i].showMorePhoto(new showMoreListener(our_interval, vh.subView[i], intervalInfos.get(index)));
                        continue;
                    }
                }
                // set Image
                vh.subView[i].setImageLoader(mImageLoader);
                vh.subView[i].showPhoto(intervalInfos.get(index));
            } else {
                // NA;
            }
        }
        return view;
    }

    public void clearCache() {
        if (mImageLoader != null) {
            mImageLoader.clearCache();
        }
    }

    private int getIntervalCount(int i) {
        List<ImageInfo> list = mIntervals.get(i);
        if(list != null) {
            // consider expand later ..
            int line = (list.size() + (i == mFirstInterval ? 1 : 0) + 2) / 3;
            if(mExpand[i]) {
                return line;
            } else {
                return Math.min(line, (sNeedExpandNumber[i] + 2) / 3);
            }
        }
        return 0;
    }

    class ViewHolder {
        public TextView dateView;
        public PhotoLineSubView[] subView;
        public ViewHolder() {
            subView = new PhotoLineSubView[3];
        }
        public void reset() {
            dateView.setVisibility(View.GONE);
            for(PhotoLineSubView view : subView) {
                view.reset();
            }
        }
    }

    class showMoreListener implements View.OnClickListener {
        private int mInterval;
        private PhotoLineSubView mView;
        private ImageInfo mInfo;
        public showMoreListener(int interval, PhotoLineSubView view, ImageInfo imageinfo) {
            mInterval = interval;
            mView = view;
            mInfo = imageinfo;
        }

        @Override
        public void onClick(View v) {
            mExpand[mInterval] = true;
            notifyDataSetChanged();
            mView.reset();
            mView.setImageLoader(mImageLoader);
            mView.showPhoto(mInfo);
        }
    }
}