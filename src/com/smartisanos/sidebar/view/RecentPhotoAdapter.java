package com.smartisanos.sidebar.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.BitmapCache;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.IEmpty;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.ImageLoader;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.BitmapDrawable;

import smartisanos.util.SidebarUtils;

public class RecentPhotoAdapter extends BaseAdapter {
    private static final LOG log = LOG.getInstance(RecentPhotoAdapter.class);

    private Context mContext;
    private RecentPhotoManager mPhotoManager;
    private List<ImageInfo> mList = new ArrayList<ImageInfo>();

    private ImageLoader mImageLoader;
    private Handler mHandler;
    private View mOpenGalleryView;
    private IEmpty mEmpty;
    public RecentPhotoAdapter(Context context, IEmpty empty) {
        mContext = context;
        mEmpty = empty;
        mHandler = new Handler(Looper.getMainLooper());
        mPhotoManager = RecentPhotoManager.getInstance(mContext);
        int maxPhotoSize = mContext.getResources().getDimensionPixelSize(R.dimen.recent_photo_size);
        mImageLoader = new ImageLoader(maxPhotoSize);
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

    private View.OnClickListener mOpenGalleryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setPackage("com.android.gallery3d");
                intent.putExtra("package_name", "com.smartisanos.sidebar");
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                Utils.dismissAllDialog(mContext);
            } catch (ActivityNotFoundException e) {
                // NA
            }
        }
    };

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
        View ret = null;
        ViewHolder vh = null;
        if (convertView != null) {
            ret = convertView;
            vh = (ViewHolder) convertView.getTag();
        } else {
            ret = LayoutInflater.from(mContext).inflate(R.layout.recent_photo_item, null);
            vh = new ViewHolder();
            vh.imageView = (ImageView) ret.findViewById(R.id.image);
            vh.openGalleryViewGroup = ret.findViewById(R.id.open_gallery);
            vh.openGalleryViewGroup.setOnClickListener(mOpenGalleryListener);
            ret.setTag(vh);
        }
        vh.updateUIByPostion(position);
        if (position <= 0) {
            return ret;
        }

        final ImageInfo ii = mList.get(position - 1);
        vh.filePath = ii.filePath;

        mImageLoader.loadImage(ii.filePath, new ImageLoaderCallback(vh));

        vh.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.dismissAllDialog(mContext);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.android.gallery3d");
                    intent.putExtra("package_name", "com.smartisanos.sidebar");
                    intent.setDataAndType(ii.getContentUri(mContext), ii.mimeType);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // NA
                }
            }
        });

        vh.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SidebarUtils.dragImage(v, mContext, new File(ii.filePath), ii.mimeType);
                return true;
            }
        });
        return ret;
    }

    class ImageLoaderCallback implements ImageLoader.Callback {
        private ViewHolder mViewHolder;

        public ImageLoaderCallback(ViewHolder vh) {
            mViewHolder = vh;
        }

        @Override
        public void onLoadComplete(final String filePath, Bitmap bitmap) {
            final Bitmap newBitmap = BitmapUtils.allNewBitmap(bitmap);
            mViewHolder.imageView.post(new Runnable() {
                @Override
                public void run() {
                    if (mViewHolder.filePath != null && mViewHolder.filePath.equals(filePath)) {
                        Drawable oldBg = mViewHolder.imageView.getBackground();
                        mViewHolder.imageView.setBackground(new BitmapDrawable(mContext.getResources(), newBitmap));
                        if (oldBg != null) {
                            if (oldBg instanceof BitmapDrawable) {
                                Bitmap oldBitmap = ((BitmapDrawable) oldBg).getBitmap();
                                if(oldBitmap != null) {
                                    oldBitmap.recycle();
                                }
                            }
                        }
                    }
                }
            });
        }

        private volatile boolean mCancelled = false;

        @Override
        public void setCancel() {
            mCancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return mCancelled;
        }
    }

    class ViewHolder {
        public ImageView imageView;
        public View openGalleryViewGroup;
        public String filePath;

        public void updateUIByPostion(int position) {
            if (position <= 0) {
                imageView.setVisibility(View.GONE);
                openGalleryViewGroup.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.VISIBLE);
                openGalleryViewGroup.setVisibility(View.GONE);
            }
        }
    }

    public void clearCache() {
        if (mImageLoader != null) {
            mImageLoader.clearCache();
        }
    }
}