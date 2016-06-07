package com.smartisanos.sidebar.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.FileInfo;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;

import smartisanos.util.SidebarUtils;
public class RecentFileAdapter extends BaseAdapter {

    private Context mContext;
    private RecentFileManager mFileManager;
    private List<FileInfo> mList = new ArrayList<FileInfo>();
    private Handler mHandler;

    public RecentFileAdapter(Context context) {
        mContext = context;
        mFileManager = RecentFileManager.getInstance(mContext);
        mList = mFileManager.getFileList();
        mHandler = new Handler(Looper.getMainLooper());
        mFileManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                mList = mFileManager.getFileList();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        RecentFileAdapter.this.notifyDataSetChanged();
                    }
                });
            }
        });
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View res = convertView;
        if(res == null){
            res =  View.inflate(mContext,R.layout.recent_file_item, null);
        }

        res.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.dismissAllDialog(mContext);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setDataAndType(Uri.fromFile(new File(mList.get(position).filePath)), mList.get(position).mimeType);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // NA
                }
            }
        });

        res.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SidebarUtils.dragFile(v, mContext, new File(mList.get(position).filePath), mList.get(position).mimeType);
                return true;
            }
        });
        TextView fileName = (TextView)res.findViewById(R.id.file_name);
        fileName.setText(new File(mList.get(position).filePath).getName());
        ImageView fileIcon = (ImageView)res.findViewById(R.id.file_icon);
        fileIcon.setImageResource(mList.get(position).getIconId());
        return res;
    }
}
