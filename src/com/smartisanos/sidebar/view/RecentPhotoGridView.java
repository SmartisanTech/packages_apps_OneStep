package com.smartisanos.sidebar.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import smartisanos.util.SidebarUtils;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.BitmapCache;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;

public class RecentPhotoGridView extends GridView{

    public RecentPhotoGridView(Context context) {
        this(context, null);
    }

    public RecentPhotoGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentPhotoGridView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentPhotoGridView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setAdapter(new RecentPhotoAdapter(mContext));
    }

    private static final class RecentPhotoAdapter extends BaseAdapter {

        private Context mContext;
        private RecentPhotoManager mPhotoManager;
        private List<ImageInfo> mList = new ArrayList<ImageInfo>();

        private int mPhotoSize = 0;
        private BitmapCache mCache;
        public RecentPhotoAdapter(Context context) {
            mContext = context;
            mPhotoManager = RecentPhotoManager.getInstance(mContext);
            mPhotoSize = mContext.getResources().getDimensionPixelSize(R.dimen.recent_photo_size);
            mCache = new BitmapCache(mPhotoSize);
            mList = mPhotoManager.getImageList();
            mPhotoManager.addListener(new RecentUpdateListener() {
                @Override
                public void onUpdate() {
                    mList = mPhotoManager.getImageList();
                    RecentPhotoAdapter.this.notifyDataSetChanged();
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
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            View ret = convertView;
            if (ret == null) {
                ret = LayoutInflater.from(mContext).inflate(R.layout.recentphotoitem, null);
            }
            ImageView iv = (ImageView) ret.findViewById(R.id.image);
            Bitmap bm = mCache.getBitmap(mList.get(position).filePath);
            iv.setImageBitmap(bm);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        SidebarController.getInstance(mContext).resumeTopView();
                        SidebarController.getInstance(mContext).dismissContent();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setPackage("com.android.gallery3d");
                        intent.setDataAndType(Uri.fromFile(new File(mList.get(position).filePath)), mList.get(position).mimeType);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // NA
                    }
                }
            });

            iv.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    SidebarUtils.dragImage(v, mContext, new File(mList.get(position).filePath), mList.get(position).mimeType);
                    return true;
                }
            });
            return ret;
        }
    }
}
