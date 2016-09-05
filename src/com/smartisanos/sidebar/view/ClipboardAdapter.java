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
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
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
        mClipManager.addListener(new RecentUpdateListener(){
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

            holder = new ViewHolder();
            holder.view = view;
            holder.dateLabel = dateLabel;
            holder.dateContent = dateContent;
            holder.textItemView = textItemView;
            holder.textView = textView;
            holder.copyItemIcon = copyItemIcon;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object obj = mList.get(position);
        if (obj instanceof DataItem) {
            holder.showItem((DataItem) obj);
        } else {
            holder.showDate((String) obj);
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

        public void showItem(DataItem item) {
            if (dateLabel.getVisibility() == View.VISIBLE) {
                dateLabel.setVisibility(View.GONE);
            }
            if (textItemView.getVisibility() != View.VISIBLE) {
                textItemView.setVisibility(View.VISIBLE);
            }
            textView.setText(item.mText);
        }

        public void showDate(String date) {
            if (textItemView.getVisibility() == View.VISIBLE) {
                textItemView.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.VISIBLE) {
                dateLabel.setVisibility(View.VISIBLE);
            }
            dateContent.setText(date);
        }
    }

    public static class DataItem implements Comparable<DataItem> {
        public String mText;
        private long mTime;

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
        String preLabel = null;
        long now = System.currentTimeMillis();
        for (int i = 0; i < items.length; i++) {
            DataItem item = items[i];
            String label = Utils.convertDateToLabel(mContext, now, item.mTime);
            if (label != null && !label.equals(preLabel)) {
                preLabel = label;
                list.add(label);
            }
            list.add(item);
        }
        synchronized (mList) {
            mList.clear();
            mList.addAll(list);
        }
    }
}

