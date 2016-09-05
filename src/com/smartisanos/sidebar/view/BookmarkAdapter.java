package com.smartisanos.sidebar.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartisanos.sidebar.util.BookmarkManager;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentUpdateListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.Utils;

public class BookmarkAdapter extends BaseAdapter {

    private static final LOG log = LOG.getInstance(BookmarkAdapter.class);

    private IEmpty mEmpty;
    private Context mContext;
    private BookmarkManager mBookmarkManager;
    private Handler mHandler;

    private List<BookmarkManager.BookmarkItem> mBookmarkList;
    private List mList = new ArrayList();

    public BookmarkAdapter(Context context, IEmpty empty) {
        mContext = context;
        mEmpty = empty;
        mHandler = new Handler(Looper.getMainLooper());
        mBookmarkManager = BookmarkManager.getInstance(mContext);
        mBookmarkList = mBookmarkManager.getBookmarks();
        updateDataList();
        mBookmarkManager.addListener(mUpdateListener);
        notifyEmpty();
    }

    private RecentUpdateListener mUpdateListener = new RecentUpdateListener() {
        @Override
        public void onUpdate() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBookmarkList = mBookmarkManager.getBookmarks();
                    updateDataList();
                    BookmarkAdapter.this.notifyDataSetChanged();
                }
            });
        }
    };

    private void updateDataList() {
        if (mBookmarkList == null || mBookmarkList.size() == 0) {
            mList.clear();
            return;
        }
        BookmarkManager.BookmarkItem[] items = new BookmarkManager.BookmarkItem[mBookmarkList.size()];
        mBookmarkList.toArray(items);
        Arrays.sort(items);
        List list = new ArrayList();
        String preLabel = null;
        long now = System.currentTimeMillis();
        for (int i = 0; i < items.length; i++) {
            BookmarkManager.BookmarkItem item = items[i];
            String label = Utils.convertDateToLabel(mContext, now, item.time);
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
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null || !(convertView instanceof LinearLayout)) {
            View view = View.inflate(mContext, R.layout.bookmark_item, null);
            LinearLayout dateLabel = (LinearLayout) view.findViewById(R.id.date_label);
            TextView dateContent = (TextView) view.findViewById(R.id.date_content);
            LinearLayout itemContent = (LinearLayout) view.findViewById(R.id.item_content);
            TextView title = (TextView) view.findViewById(R.id.title_text);
            TextView url = (TextView) view.findViewById(R.id.url_text);
            holder = new ViewHolder();
            holder.view = view;
            holder.dateLabel = dateLabel;
            holder.dateContent = dateContent;

            holder.itemContent = itemContent;
            holder.title = title;
            holder.url = url;

            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Object obj = mList.get(position);
        if (obj instanceof BookmarkManager.BookmarkItem) {
            holder.showItem((BookmarkManager.BookmarkItem) obj);
        } else {
            holder.showDate((String) obj);
        }
        return holder.view;
    }

    private class ViewHolder {
        public View view;

        public LinearLayout dateLabel;
        public TextView dateContent;

        public LinearLayout itemContent;
        public TextView title;
        public TextView url;

        public void showItem(BookmarkManager.BookmarkItem item) {
            if (dateLabel.getVisibility() == View.VISIBLE) {
                dateLabel.setVisibility(View.GONE);
            }
            if (itemContent.getVisibility() != View.VISIBLE) {
                itemContent.setVisibility(View.VISIBLE);
            }
            title.setText(item.title);
            url.setText(item.content_uri);
        }

        public void showDate(String date) {
            if (itemContent.getVisibility() == View.VISIBLE) {
                itemContent.setVisibility(View.GONE);
            }
            if (dateLabel.getVisibility() != View.VISIBLE) {
                dateLabel.setVisibility(View.VISIBLE);
            }
            dateContent.setText(date);
        }
    }
}