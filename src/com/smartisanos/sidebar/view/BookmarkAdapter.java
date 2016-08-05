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

import java.util.List;

import com.smartisanos.sidebar.R;

public class BookmarkAdapter extends BaseAdapter {

    private static final LOG log = LOG.getInstance(BookmarkAdapter.class);

    private IEmpty mEmpty;
    private Context mContext;
    private BookmarkManager mBookmarkManager;
    private Handler mHandler;

    private List<BookmarkManager.BookmarkItem> mList;

    public BookmarkAdapter(Context context, IEmpty empty) {
        mContext = context;
        mEmpty = empty;
        mHandler = new Handler(Looper.getMainLooper());
        mBookmarkManager = BookmarkManager.getInstance(mContext);
        mList = mBookmarkManager.getBookmarks();
        mBookmarkManager.addListener(mUpdateListener);
        notifyEmpty();
    }

    private RecentUpdateListener mUpdateListener = new RecentUpdateListener() {
        @Override
        public void onUpdate() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    log.error("mRecentUpdateListener post run");
                    mList = mBookmarkManager.getBookmarks();
                    BookmarkAdapter.this.notifyDataSetChanged();
                }
            });
        }
    };

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
    public BookmarkManager.BookmarkItem getItem(int position) {
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
            TextView title = (TextView) view.findViewById(R.id.title_text);
            TextView url = (TextView) view.findViewById(R.id.url_text);
            holder = new ViewHolder();
            holder.view = view;
            holder.title = title;
            holder.url = url;

            view.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BookmarkManager.BookmarkItem item = mList.get(position);
        holder.updateUI(item);
        return holder.view;
    }

    private class ViewHolder {
        public View view;

        public TextView title;
        public TextView url;

        public void updateUI(BookmarkManager.BookmarkItem item) {
            title.setText(item.title);
            url.setText(item.content_uri);
//            log.error("updateUI ["+item.title+"]["+item.content_uri+"]");
        }
    }
}