package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;

public class ToAddContactGridView extends GridView {
    private static final String TAG = ToAddContactGridView.class.getName();

    public ToAddContactGridView(Context context) {
        super(context, null);
    }

    public ToAddContactGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToAddContactGridView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ToAddContactGridView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setAdapter(new AddContactAdapter(mContext));
    }

    private class AddContactAdapter extends BaseAdapter{
        private Context mContext;
        private List<AddContactItem> mItemList = new ArrayList<AddContactItem>();
        public AddContactAdapter(Context context){
            mContext = context;
            initData();
        }

        private void initData(){
            mItemList.clear();
            mItemList.add(new AddContactItem(R.drawable.icon_dingding, R.string.add_contact_dingding, mDingDingListener));
            mItemList.add(new AddContactItem(R.drawable.icon_wechat, R.string.add_contact_wechat, mMmsContactListener));
            mItemList.add(new AddContactItem(R.drawable.icon_mail, R.string.add_contact_mail, null));
        }
        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View ret = convertView;
            if (ret == null) {
                ret = LayoutInflater.from(mContext).inflate(R.layout.add_contact_item, null);
            }

            ImageView iv = (ImageView) ret.findViewById(R.id.icon);
            iv.setImageResource(mItemList.get(position).iconId);
            TextView tv = (TextView) ret.findViewById(R.id.label);
            tv.setText(mItemList.get(position).labelId);
            ret.setOnClickListener(mItemList.get(position).mListener);
            return ret;
        }

        private class AddContactItem{
            int iconId;
            int labelId;
            View.OnClickListener mListener;
            public AddContactItem(int iconId, int labelId, View.OnClickListener listener){
                this.iconId = iconId;
                this.labelId = labelId;
                this.mListener = listener;
            }
        }

        private View.OnClickListener mDingDingListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.alibaba.android.rimet.ShortCutSelect");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try{
                    SidebarController.getInstance(mContext).resumeTopView();
                    SidebarController.getInstance(mContext).dismissContent();
                    mContext.startActivity(intent);
                }catch(ActivityNotFoundException e){
                    // TODO show dialog
                }
            }
        };

        private View.OnClickListener mMmsContactListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.smartisanos.sidebar", "com.smartisanos.sidebar.SelectContactActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try{
                    SidebarController.getInstance(mContext).resumeTopView();
                    SidebarController.getInstance(mContext).dismissContent();
                    mContext.startActivity(intent);
                }catch(ActivityNotFoundException e){
                    // NA
                    Log.d(TAG, "not found !", e);
                }
            }
        };
    }
}
