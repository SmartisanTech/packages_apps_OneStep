package com.smartisanos.sidebar.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.BitmapCache;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.ImageLoader;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import smartisanos.util.SidebarUtils;

public class RecentPhotoAdapter extends BaseAdapter {

    private Context mContext;
    private RecentPhotoManager mPhotoManager;
    private List<ImageInfo> mList = new ArrayList<ImageInfo>();

    private ImageLoader mImageLoader;
    private Handler mHandler;
    private IEmpty mEmpty;
    public RecentPhotoAdapter(Context context, IEmpty empty) {
        mContext = context;
        mEmpty = empty;
        mHandler = new Handler(Looper.getMainLooper());
        mPhotoManager = RecentPhotoManager.getInstance(mContext);
        mImageLoader = new ImageLoader(mContext.getResources().getDimensionPixelSize(R.dimen.recent_photo_size));
        mList = mPhotoManager.getImageList();
        mPhotoManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        mList = mPhotoManager.getImageList();
                        notifyDataSetChanged();
                    }
                });
            }
        });
        notifyEmpty();
    }

    private void notifyEmpty() {
        if (mEmpty != null) {
            mEmpty.setEmpty(mList.size() == 0);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        notifyEmpty();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
            ViewGroup parent) {
        if(position == 0){
            View ret = LayoutInflater.from(mContext).inflate(R.layout.open_gallery_item, null);
            ret.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setPackage("com.android.gallery3d");
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        Utils.dismissAllDialog(mContext);
                    }catch (ActivityNotFoundException e) {
                        // NA
                    }
                }
            });
            return ret;
        }
        final ImageInfo ii = mList.get(position - 1);
        View ret = null;
        final ImageView iv;
        if (convertView != null && convertView.getTag() != null) {
            ret = convertView;
            iv = (ImageView) convertView.getTag();
        } else {
            ret = LayoutInflater.from(mContext).inflate(R.layout.recentphotoitem, null);
            iv = (ImageView) ret.findViewById(R.id.image);
            ret.setTag(iv);
        }
        iv.setImageBitmap(null);
        iv.setTag(ii.filePath);
        mImageLoader.loadImage(ii.filePath, iv, new ImageLoader.Callback() {
            @Override
            public void onLoadComplete(final Bitmap bitmap) {
                if(ii.filePath != null && ii.filePath.equals(iv.getTag())){
                    iv.post(new Runnable(){
                        @Override
                        public void run() {
                            if(ii.filePath != null && ii.filePath.equals(iv.getTag())){
                                iv.setImageBitmap(bitmap);
                            }
                        }
                    });
                }
            }
        });

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.dismissAllDialog(mContext);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.android.gallery3d");
                    intent.setDataAndType(Uri.fromFile(new File(ii.filePath)), ii.mimeType);
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
                SidebarUtils.dragImage(v, mContext, new File(ii.filePath), ii.mimeType);
                return true;
            }
        });
        return ret;
    }
}