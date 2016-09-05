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
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;

public class RecentFileAdapter extends BaseAdapter {

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
        mFileManager.addListener(new RecentUpdateListener() {
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
        for (int i = 0; i < infos.length; i++) {
            FileInfo info = infos[i];
            String label = Utils.convertDateToLabel(mContext, now, info.lastTime);
            if (label != null && !label.equals(preLabel)) {
                preLabel = label;
                list.add(label);
            }
            list.add(info);
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

            holder = new ViewHolder();
            holder.view = view;
            holder.dateLabel = dateLabel;
            holder.dateContent = dateContent;
            holder.recentFileItemView = recentFileItemView;
            holder.fileNameView = fileNameView;
            holder.iconView = iconView;

            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Object obj = mList.get(position);
        if (obj instanceof FileInfo) {
            holder.showItem((FileInfo) obj);
        } else {
            holder.showDate((String) obj);
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

        public void showItem(FileInfo info) {
            if (dateLabel.getVisibility() == View.VISIBLE) {
                dateLabel.setVisibility(View.GONE);
            }
            if (recentFileItemView.getVisibility() != View.VISIBLE) {
                recentFileItemView.setVisibility(View.VISIBLE);
            }
            fileNameView.setText(new File(info.filePath).getName());
            iconView.setImageResource(info.getIconId());
        }

        public void showDate(String date) {
            if (recentFileItemView.getVisibility() == View.VISIBLE) {
                recentFileItemView.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.VISIBLE) {
                dateLabel.setVisibility(View.VISIBLE);
            }
            dateContent.setText(date);
        }
    }
}
