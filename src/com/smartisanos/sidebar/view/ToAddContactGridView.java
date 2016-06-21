package com.smartisanos.sidebar.view;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.Utils;

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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
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
            mItemList.add(new AddContactItem(R.drawable.icon_wechat, R.string.add_contact_wechat, mWechatListener));
            mItemList.add(new AddContactItem(R.drawable.icon_mms, R.string.add_contact_mms, mMmsListener));
            mItemList.add(new AddContactItem(R.drawable.icon_mail, R.string.add_contact_mail, mMailListener));
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
                    Utils.dismissAllDialog(mContext);
                    mContext.startActivity(intent);
                }catch(ActivityNotFoundException e){
                    // TODO show dialog
                }
            }
        };

        private View.OnClickListener mWechatListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                builder.setMessage(R.string.add_wechat_hint)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_MAIN);
                                            ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
                                            intent.setComponent(cmp);
                                            //intent.setPackage("com.tencent.mm");
                                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mContext.startActivity(intent);
                                            Utils.dismissAllDialog(mContext);
                                        } catch (ActivityNotFoundException e) {
                                            Log.d(TAG, "wechat not installed !", e);
                                        }
                                    }
                                });
                AlertDialog dialog = builder.create();
                dialog.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
                dialog.getWindow().getAttributes().token = getWindowToken();
                dialog.show();
            }
        };

        private View.OnClickListener mMmsListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.smartisanos.sidebar", "com.smartisanos.sidebar.SelectContactActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try{
                    Utils.dismissAllDialog(mContext);
                    mContext.startActivity(intent);
                }catch(ActivityNotFoundException e){
                    // NA
                    Log.d(TAG, "not found !", e);
                }
            }
        };

        private View.OnClickListener mMailListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.smartisanos.sidebar", "com.smartisanos.sidebar.SelectMailContactActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try{
                    Utils.dismissAllDialog(mContext);
                    mContext.startActivity(intent);
                }catch(ActivityNotFoundException e){
                    // NA
                    Log.d(TAG, "not found !", e);
                }
            }
        };
    }
}
