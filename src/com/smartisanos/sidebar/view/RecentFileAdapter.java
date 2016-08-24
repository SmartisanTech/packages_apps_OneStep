package com.smartisanos.sidebar.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.FileInfo;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.Utils;

public class RecentFileAdapter extends BaseAdapter {
    private static final LOG log = LOG.getInstance(RecentFileAdapter.class);

    private Context mContext;
    private RecentFileManager mFileManager;
    private List<FileInfo> mFileInfoList = new ArrayList<FileInfo>();
    private List mList = new ArrayList();
    private Handler mHandler;
    private IEmpty mEmpty;

    public RecentFileAdapter(Context context, IEmpty empty) {
        mContext = context;
        mEmpty = empty;
        mFileManager = RecentFileManager.getInstance(mContext);
        mFileInfoList = mFileManager.getFileList();
        updateDataList();
        mHandler = new Handler(Looper.getMainLooper());
        mFileManager.addListener(new RecentFileManager.RecentUpdateListener() {
            @Override
            public void onUpdate() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFileInfoList = mFileManager.getFileList();
                        updateDataList();
                        RecentFileAdapter.this.notifyDataSetChanged();
                    }
                });
            }
        });
        notifyEmpty();
    }

    private static final int MAX_ITEM_COUNT = 7;

    private void updateDataList() {
        if (mFileInfoList.size() == 0) {
            mList.clear();
            return;
        }
        FileInfo[] infos = new FileInfo[mFileInfoList.size()];
        mFileInfoList.toArray(infos);
        Arrays.sort(infos);
        List list = new ArrayList();
        String preLabel = null;
        long now = System.currentTimeMillis();
        int count = 0;
        List<FileInfo> fileList = new ArrayList<FileInfo>();
        for (int i = 0; i < infos.length; i++) {
            FileInfo info = infos[i];
            String label = Utils.convertDateToLabel(mContext, now, info.lastTime);
            if (label != null && !label.equals(preLabel)) {
                if (count > MAX_ITEM_COUNT) {
                    list.add(fileList);
                }
                fileList = new ArrayList<FileInfo>();
                count = 0;
                preLabel = label;
                list.add(label);
            }
            count = count + 1;
            info.dateTag = label;
            if (count > MAX_ITEM_COUNT) {
                info.invisibleMode = true;
                fileList.add(info);
            } else {
                list.add(info);
            }
            if (i == infos.length - 1) {
                if (count > MAX_ITEM_COUNT) {
                    list.add(fileList);
                }
            }
        }
        synchronized (mList) {
            mList.clear();
            mList.addAll(list);
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
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void removeItem(Object item) {
        if (item == null) {
            return;
        }
        mList.remove(item);
    }

    public void addItems(int index, List<FileInfo> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        mList.addAll(index, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            View view = View.inflate(mContext,R.layout.recent_file_item, null);
            LinearLayout dateLabel = (LinearLayout) view.findViewById(R.id.date_label);
            TextView dateContent = (TextView) view.findViewById(R.id.date_content);
            LinearLayout recentFileItemView = (LinearLayout) view.findViewById(R.id.recent_file_item);
            TextView fileNameView = (TextView) view.findViewById(R.id.file_name);
            ImageView iconView = (ImageView) view.findViewById(R.id.file_icon);
            LinearLayout moreLabel = (LinearLayout) view.findViewById(R.id.more_label);

            holder = new ViewHolder();
            holder.view = view;
            holder.dateLabel = dateLabel;
            holder.dateContent = dateContent;
            holder.recentFileItemView = recentFileItemView;
            holder.fileNameView = fileNameView;
            holder.iconView = iconView;
            holder.moreLabel = moreLabel;

            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Object obj = mList.get(position);
        if (obj instanceof FileInfo) {
            holder.showItem((FileInfo) obj);
        } else if (obj instanceof String) {
            holder.showDate((String) obj);
        } else {
            holder.showMoreTag();
        }
        return holder.view;
    }

    private static class ViewHolder {
        public View view;

        public LinearLayout dateLabel;
        public TextView dateContent;

        public LinearLayout recentFileItemView;
        public ImageView iconView;
        public TextView fileNameView;

        public LinearLayout moreLabel;

        public void showItem(FileInfo info) {
            if (moreLabel.getVisibility() != View.GONE) {
                moreLabel.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.GONE) {
                dateLabel.setVisibility(View.GONE);
            }
            if (recentFileItemView.getVisibility() != View.VISIBLE) {
                recentFileItemView.setVisibility(View.VISIBLE);
            }
            fileNameView.setText(new File(info.filePath).getName());
            iconView.setImageResource(info.getIconId());
        }

        public void showDate(String date) {
            if (moreLabel.getVisibility() != View.GONE) {
                moreLabel.setVisibility(View.GONE);
            }
            if (recentFileItemView.getVisibility() != View.GONE) {
                recentFileItemView.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.VISIBLE) {
                dateLabel.setVisibility(View.VISIBLE);
            }
            dateContent.setText(date);
        }

        public void showMoreTag() {
            if (recentFileItemView.getVisibility() != View.GONE) {
                recentFileItemView.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.GONE) {
                dateLabel.setVisibility(View.GONE);
            }
            if (moreLabel.getVisibility() != View.VISIBLE) {
                moreLabel.setVisibility(View.VISIBLE);
            }
        }
    }
}
