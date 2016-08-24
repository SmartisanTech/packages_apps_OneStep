package com.smartisanos.sidebar.view;

import android.content.Context;
import android.content.CopyHistoryItem;
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
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import smartisanos.util.SidebarUtils;

public class ClipboardAdapter extends BaseAdapter{
    private static LOG log = LOG.getInstance(ClipboardAdapter.class);

    private Context mContext;
    private RecentClipManager mClipManager;
    private List<CopyHistoryItem> mCopyHistoryItemList = new ArrayList<CopyHistoryItem>();
    private List mList = new ArrayList();
    private Handler mHandler;
    private IEmpty mEmpty;
    public ClipboardAdapter(Context context, IEmpty empty){
        mContext = context;
        mEmpty = empty;
        mClipManager = RecentClipManager.getInstance(mContext);
        mCopyHistoryItemList = mClipManager.getCopyList();
        updateDataList();
        mHandler = new Handler(Looper.getMainLooper());
        mClipManager.addListener(new RecentClipManager.RecentUpdateListener(){
            @Override
            public void onUpdate() {
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        mCopyHistoryItemList = mClipManager.getCopyList();
                        updateDataList();
                        notifyDataSetChanged();
                    }
                });
            }
        });
        notifyEmpty();
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
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
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

    public void addItems(int index, List<DataItem> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        mList.addAll(index, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;
        if(convertView == null) {
            View view =  View.inflate(mContext,R.layout.copyhistoryitem, null);
            LinearLayout dateLabel = (LinearLayout) view.findViewById(R.id.date_label);
            TextView dateContent = (TextView) view.findViewById(R.id.date_content);
            LinearLayout textItemView = (LinearLayout) view.findViewById(R.id.text_item);
            TextView textView = (TextView) view.findViewById(R.id.text);
            ImageView copyItemIcon = (ImageView) view.findViewById(R.id.copy_item_icon);
            LinearLayout moreLabel = (LinearLayout) view.findViewById(R.id.more_label);

            holder = new ViewHolder();
            holder.view = view;
            holder.dateLabel = dateLabel;
            holder.dateContent = dateContent;
            holder.textItemView = textItemView;
            holder.textView = textView;
            holder.copyItemIcon = copyItemIcon;
            holder.moreLabel = moreLabel;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object obj = mList.get(position);
        if (obj instanceof DataItem) {
            holder.showItem((DataItem) obj);
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

        public LinearLayout textItemView;
        public TextView textView;
        public ImageView copyItemIcon;

        public LinearLayout moreLabel;

        public void showItem(DataItem item) {
            if (moreLabel.getVisibility() != View.GONE) {
                moreLabel.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.GONE) {
                dateLabel.setVisibility(View.GONE);
            }
            if (textItemView.getVisibility() != View.VISIBLE) {
                textItemView.setVisibility(View.VISIBLE);
            }
            textView.setText(item.mText);
        }

        public void showDate(String date) {
            if (moreLabel.getVisibility() != View.GONE) {
                moreLabel.setVisibility(View.GONE);
            }
            if (textItemView.getVisibility() != View.GONE) {
                textItemView.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.VISIBLE) {
                dateLabel.setVisibility(View.VISIBLE);
            }
            dateContent.setText(date);
        }

        public void showMoreTag() {
            if (textItemView.getVisibility() != View.GONE) {
                textItemView.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.GONE) {
                dateLabel.setVisibility(View.GONE);
            }
            if (moreLabel.getVisibility() != View.VISIBLE) {
                moreLabel.setVisibility(View.VISIBLE);
            }
        }
    }

    public static class DataItem implements Comparable<DataItem> {
        public String mText;
        private long mTime;
        public String dateTag;
        public boolean invisibleMode = false;

        public DataItem(String content, long time) {
            mText = content;
            mTime = time;
        }

        @Override
        public int compareTo(DataItem item) {
            if (item == null) {
                return -1;
            }
            if (mTime == item.mTime) {
                return 0;
            }
            if (item.mTime > mTime) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private static final int MAX_ITEM_COUNT = 7;

    private void updateDataList() {
        if (mCopyHistoryItemList.size() == 0) {
            mList.clear();
            return;
        }
        int size = mCopyHistoryItemList.size();
        DataItem[] items = new DataItem[size];
        for (int i = 0; i < size; i++) {
            //convert CopyHistoryItem to DataItem
            CopyHistoryItem item = mCopyHistoryItemList.get(i);
            items[i] = new DataItem(item.mContent, item.mTimeStamp);
        }
        Arrays.sort(items);
        List list = new ArrayList();
        String currentLabel = null;
        long now = System.currentTimeMillis();
        int count = 0;
        List<DataItem> dataList = new ArrayList<DataItem>();
        for (int i = 0; i < items.length; i++) {
            DataItem item = items[i];
            String label = Utils.convertDateToLabel(mContext, now, item.mTime);
            if (label != null && !label.equals(currentLabel)) {
                if (count > MAX_ITEM_COUNT) {
                    list.add(dataList);
                }
                dataList = new ArrayList<DataItem>();
                count = 0;
                currentLabel = label;
                list.add(label);
            }

            count = count + 1;
            item.dateTag = label;
            if (count > MAX_ITEM_COUNT) {
                item.invisibleMode = true;
                dataList.add(item);
            } else {
                list.add(item);
            }
            if (i == items.length - 1) {
                if (count > MAX_ITEM_COUNT) {
                    list.add(dataList);
                }
            }
        }
        synchronized (mList) {
            mList.clear();
            mList.addAll(list);
        }
    }
}

