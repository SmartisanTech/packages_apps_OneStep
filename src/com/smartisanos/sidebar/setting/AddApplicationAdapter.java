package com.smartisanos.sidebar.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.AppItem;
import com.smartisanos.sidebar.util.AppManager;

public final class AddApplicationAdapter extends BaseAdapter {

    private List<ViewItem> mItems = new ArrayList<ViewItem>();
    private Context mContext;

    public AddApplicationAdapter(Context context) {
        mContext = context;
        refreshData();
    }

    private AppManager.RecentUpdateListener mUpdateListener = new AppManager.RecentUpdateListener() {
        @Override
        public void onUpdate() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    onDataChange();
                }
            });
        }
    };

    private void onDataChange() {
        for (ViewItem vi : mItems) {
            boolean added = AppManager.getInstance(mContext).isAppItemAdded(vi.appItem);
            vi.selected = added;
        }
        notifyDataSetChanged();
    }

    public void onStart() {
        refreshData();
        notifyDataSetChanged();
        AppManager.getInstance(mContext).addListener(mUpdateListener);
    }

    public void onStop() {
        AppManager.getInstance(mContext).removeListener(mUpdateListener);
    }

    public void refreshData() {
        mItems.clear();
        for(AppItem ai: AppManager.getInstance(mContext).getAddedAppItem()) {
            mItems.add(new ViewItem(ai, true));
        }
        for(AppItem ai: AppManager.getInstance(mContext).getUnAddedAppItem()) {
            mItems.add(new ViewItem(ai, false));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mItems.size() + 2) / 3;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder mHolder = null;
        if (view == null) {
            mHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.app_picker_item, null);
            mHolder.spaceViewTop = view.findViewById(R.id.space_top);
            mHolder.subViews[0] = (AppPickerSubView) view.findViewById(R.id.app_picker_sub_view_1);
            mHolder.subViews[1] = (AppPickerSubView) view.findViewById(R.id.app_picker_sub_view_2);
            mHolder.subViews[2] = (AppPickerSubView) view.findViewById(R.id.app_picker_sub_view_3);
            mHolder.view = view;
            view.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) view.getTag();
        }
        int index = position * 3;
        for (int i = 0; i < 3; ++i) {
            final AppPickerSubView apsv = mHolder.subViews[i];
            final int pos = index + i;
            if (pos < mItems.size()) {
                final ViewItem ai = mItems.get(pos);
                apsv.setVisibility(View.VISIBLE);
                apsv.setImageDrawable(ai.appItem.getAvatar());
                apsv.setText(ai.appItem.getDisplayName());
                apsv.setSelected(ai.selected);
                apsv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!apsv.isSelected()) {
                            if (AppManager.getInstance(mContext).addAppItem(ai.appItem)) {
                                mItems.get(pos).selected = true;
                                apsv.setSelected(true);
                            }
                        } else {
                            AppManager.getInstance(mContext).removeAppItem(ai.appItem);
                            mItems.get(pos).selected = false;
                            apsv.setSelected(false);
                        }
                    }
                });
            } else {
                apsv.setVisibility(View.INVISIBLE);
            }
        }

        mHolder.updateSpace(position, getCount());
        int bgRes = getBackgrondResId(position, getCount());
        if (bgRes > 0) {
            mHolder.view.setBackgroundResource(bgRes);
        }

        return view;
    }

    private int getBackgrondResId(int position, int count) {
        if (count <= 0 || position < 0 || position >= count) {
            return 0;
        }
        int bgRes = 0;
        if (count == 1) {
            bgRes = R.drawable.common_icon_picker_bg_single;
        } else {
            if (position == 0) {
                bgRes = R.drawable.common_icon_picker_bg_top;
            } else if (position == count - 1) {
                bgRes = R.drawable.common_icon_picker_bg_bottom;
            } else {
                bgRes = R.drawable.common_icon_picker_bg_middle;
            }
        }
        return bgRes;
    }

    class ViewHolder {
        View view;
        View spaceViewTop;
        AppPickerSubView[] subViews = new AppPickerSubView[3];

        public void updateSpace(int postion, int all) {
            spaceViewTop.setVisibility(postion == 0 ? View.VISIBLE
                    : View.GONE);
        }
    }

    class ViewItem {
        public AppItem appItem;
        public boolean selected;
        public ViewItem(AppItem ai, boolean selected) {
            this.appItem = ai;
            this.selected = selected;
        }
    }
}