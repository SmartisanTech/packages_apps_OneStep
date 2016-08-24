package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.OngoingItem;
import com.smartisanos.sidebar.util.OngoingManager;
import com.smartisanos.sidebar.util.Utils;

public class OngoingAdapter extends DragEventAdapter {
    private static final float SCALE_SIZE = 1.4f;
    private Context mContext;
    private OngoingManager mManager;
    private List<OngoingItem> mList;
    private List<OngoingItem> mAccpetableList = new ArrayList<OngoingItem>();
    private Handler mHandler;
    private DragEvent mDragEvent;
    public OngoingAdapter(Context context) {
        mContext = context;
        mManager = OngoingManager.getInstance(mContext);
        mList = mManager.getList();
        mAccpetableList.addAll(mList);
        mHandler = new Handler(Looper.getMainLooper());
        mManager.addListener(new OngoingManager.RecentUpdateListener() {
            @Override
            public void onUpdate() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mList = mManager.getList();
                        updateAcceptableList();
                    }
                });
            }
        });
    }

    @Override
    public int getCount() {
        return mAccpetableList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAccpetableList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View ret = convertView;
        if (ret == null) {
            ret = LayoutInflater.from(mContext).inflate(R.layout.ongoing_item, null);
            ViewHolder vh = new ViewHolder();
            vh.pendingNumbers = (TextView) ret.findViewById(R.id.pending_numbers);
            ret.setTag(vh);
        }
        ViewHolder vh = (ViewHolder) ret.getTag();
        vh.item = mAccpetableList.get(position);
        vh.updateUI();
        Utils.setAlwaysCanAcceptDrag(ret, true);
        ret.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.animate().scaleX(SCALE_SIZE).scaleY(SCALE_SIZE)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setStartDelay(0).setDuration(100).start();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DROP:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    ViewHolder vh = (ViewHolder) v.getTag();
                    return vh.item.handleDragEvent(mContext, event);
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                }
                return false;
            }
        });
        ret.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ViewHolder vh = (ViewHolder) v.getTag();
                vh.item.openUI(mContext);
            }
        });
        return ret;
    }


    private void updateAcceptableList() {
        mAccpetableList.clear();
        for (OngoingItem item : mList) {
            if (mDragEvent == null || item.acceptDragEvent(mContext, mDragEvent)) {
                mAccpetableList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onDragStart(DragEvent event) {
        if (mDragEvent != null) {
            mDragEvent.recycle();
            mDragEvent = null;
        }
        mDragEvent = DragEvent.obtain(event);
        updateAcceptableList();
    }

    @Override
    public void onDragEnd() {
        if (mDragEvent == null) {
            return;
        }
        mDragEvent.recycle();
        mDragEvent = null;
        updateAcceptableList();
    }

    public void moveItemPostion(Object object, int index) {
        // NA
    }

    class ViewHolder {
        public TextView pendingNumbers;
        public OngoingItem item;

        public void updateUI() {
            if (item.getPendingNumbers() <= 0) {
                pendingNumbers.setVisibility(View.INVISIBLE);
            } else {
                pendingNumbers.setVisibility(View.VISIBLE);
                pendingNumbers.setText("" + item.getPendingNumbers());
            }
        }
    }
}
