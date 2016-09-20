package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.Arrays;
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
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import smartisanos.util.SidebarUtils;

public class RecentPhotoAdapter extends BaseAdapter {
    private static final LOG log = LOG.getInstance(RecentPhotoAdapter.class);

    private Context mContext;
    private RecentPhotoManager mPhotoManager;
    private List<ImageInfo> mImageInfoList = new ArrayList<ImageInfo>();
    private List mList = new ArrayList();

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

    public void setPhotoExpandedFalse() {
        //create a new PhotoGridView.PhotoData, adapter will reset
        updateDataList();
    }

    private void updateDataList() {
        if (mImageInfoList.size() == 0) {
            mList.clear();
            return;
        }
        ImageInfo[] images = new ImageInfo[mImageInfoList.size()];
        mImageInfoList.toArray(images);
        Arrays.sort(images);

        long now = System.currentTimeMillis();
        List<String> labelOrder = new ArrayList<String>();
        Map<String, List<ImageInfo>> map = new HashMap<String, List<ImageInfo>>();
        for (int i = 0; i < images.length; i++) {
            ImageInfo info = images[i];
            String label = Utils.convertDateToLabel(mContext, now, info.time);
            List<ImageInfo> list = map.get(label);
            if (list == null) {
                list = new ArrayList<ImageInfo>();
            }
            list.add(info);
            map.put(label, list);
            if (!labelOrder.contains(label)) {
                labelOrder.add(label);
            }
        }

        List list = new ArrayList();
        for (int i = 0; i < labelOrder.size(); i++) {
            String label = labelOrder.get(i);
            List<ImageInfo> photoList = map.get(label);
            if (label == null || photoList == null) {
                continue;
            }
            list.add(label);
            boolean showGallery = false;
            if (i == 0) {
                showGallery = true;
            }
            PhotoGridView.PhotoData data = new PhotoGridView.PhotoData(photoList);
            data.showGallery(showGallery);
            list.add(data);
        }
        map.clear();
        synchronized (mList) {
            mList.clear();
            mList.addAll(list);
        }
    }

    private void notifyEmpty() {
        if (mEmpty != null) {
            mEmpty.setEmpty(mList.size() == 0);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        notifyEmpty();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.photo_block, null);
            LinearLayout dateLabel = (LinearLayout) view.findViewById(R.id.date_label);
            TextView textContent = (TextView) view.findViewById(R.id.date_content);
            PhotoGridView photoGridView = (PhotoGridView) view.findViewById(R.id.photo_grid_view);

            holder = new ViewHolder();
            holder.view = view;
            holder.dateLabel = dateLabel;
            holder.textContent = textContent;
            holder.photoGridView = photoGridView;
            holder.view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Object obj = mList.get(position);
        if (obj instanceof PhotoGridView.PhotoData) {
            PhotoGridView.PhotoData data = (PhotoGridView.PhotoData) obj;
            holder.showPhotos(data);
        } else if (obj instanceof String) {
            holder.showDate((String) obj);
        }
        return holder.view;
    }

    private class ViewHolder {
        public View view;
        public LinearLayout dateLabel;
        public TextView textContent;
        public PhotoGridView photoGridView;

        public void showPhotos(PhotoGridView.PhotoData data) {
            if (dateLabel.getVisibility() != View.GONE) {
                dateLabel.setVisibility(View.GONE);
            }
            if (photoGridView.getVisibility() != View.VISIBLE) {
                photoGridView.setVisibility(View.VISIBLE);
            }
            photoGridView.setPhotoData(data, mImageLoader);
        }

        public void showDate(String date) {
            if (photoGridView.getVisibility() != View.GONE) {
                photoGridView.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.VISIBLE) {
                dateLabel.setVisibility(View.VISIBLE);
            }
            textContent.setText(date);
        }
    }

    public void clearCache() {
        if (mImageLoader != null) {
            mImageLoader.clearCache();
        }
    }
}