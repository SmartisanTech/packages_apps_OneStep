package com.smartisanos.sidebar.view;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.ImageLoader;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import smartisanos.util.SidebarUtils;

public class PhotoGridView extends GridView {
    private static final LOG log = LOG.getInstance(PhotoGridView.class);

    private DataAdapter mAdapter;
    private Context mContext;
    private ImageLoader mImageLoader;
    private PhotoData mPhotoData;

    public PhotoGridView(Context context) {
        this(context, null);
    }

    public PhotoGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PhotoGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnItemClickListener(mOnItemClickListener);
        setOnItemLongClickListener(mOnItemLongClickListener);
        setSelector(R.drawable.grid_view_selector);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Object obj = mAdapter.getItem(position);
            if (obj == null) {
                openGallery();
            } else if (obj instanceof ImageInfo) {
                ImageInfo info = (ImageInfo) obj;
//                log.error("onItemClick date ["+Utils.toDate(info.time)+"], path ["+info.filePath+"]");
                openPhotoWithGallery(info);
            } else if (obj instanceof ArrayList) {
                if (!mPhotoData.expanded) {
                    mPhotoData.setToExpanded();
                    buildListData();
                }
            }
        }
    };

    private void openGallery() {
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

    private void openPhotoWithGallery(ImageInfo info) {
        if (info == null) {
            return;
        }
        Utils.dismissAllDialog(mContext);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.android.gallery3d");
            intent.putExtra("package_name", "com.smartisanos.sidebar");
            Uri uri = info.getContentUri(mContext);
            log.error("openPhotoWithGallery uri ["+uri.toString()+"]");
            intent.setDataAndType(uri, info.mimeType);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // NA
        }
    }

    private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            Object obj = mAdapter.getItem(position);
            if (obj != null && obj instanceof ImageInfo) {
                ImageInfo info = (ImageInfo) obj;
//                log.error("mOnItemLongClickListener position ["+position+"] ["+info.filePath+"]");
                SidebarUtils.dragImage(view, mContext, new File(info.filePath), info.mimeType);
                return true;
            }
            return false;
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public void setPhotoData(PhotoData data, ImageLoader loader) {
        mImageLoader = loader;
        if (mPhotoData != data) {
            mPhotoData = data;
            mAdapter = new DataAdapter();
            setAdapter(mAdapter);
        }
        buildListData();
    }

    private void buildListData() {
        List list = mPhotoData.getDataList();
        mAdapter.setDataList(list);
        mAdapter.notifyDataSetChanged();
    }

    public static class PhotoData {
        private static final int MAX_ITEM_COUNT = 9;

        public boolean expanded = false;
        public boolean showGallery = false;
        public List<ImageInfo> mImages = new ArrayList<ImageInfo>();

        public PhotoData(List<ImageInfo> images) {
            mImages.clear();
            if (images != null) {
                mImages.addAll(images);
            }
        }

        public void showGallery(boolean show) {
            showGallery = show;
        }

        public List getDataList() {
            List list = new ArrayList();
            if (showGallery) {
                list.add(null);
            }
            if (!expanded && (mImages.size() + list.size()) > MAX_ITEM_COUNT) {
                int index = MAX_ITEM_COUNT - list.size() - 1;
                list.addAll(mImages.subList(0, index));
                List<ImageInfo> hideList = new ArrayList<ImageInfo>();
                hideList.addAll(mImages.subList(index, mImages.size()));
                list.add(hideList);
            } else {
                list.addAll(mImages);
            }
            return list;
        }

        public void setToExpanded() {
            expanded = true;
        }
    }

    class ImageLoaderCallback implements ImageLoader.Callback {
        private ViewHolder mViewHolder;

        public ImageLoaderCallback(ViewHolder holder) {
            mViewHolder = holder;
        }

        @Override
        public void onLoadComplete(final String filePath, Bitmap bitmap) {
            final Bitmap newBitmap = BitmapUtils.allNewBitmap(bitmap);
            final ImageLoaderCallback callback = this;
            mViewHolder.view.post(new Runnable() {
                @Override
                public void run() {
                    mImageLoader.removeLoadingTask(callback);
                    if (mViewHolder.imageInfo == null) {
                        log.error("onLoadCompleteImageImi return by imageInfo is null, path ["+filePath+"]");
                        return;
                    }
                    String path = mViewHolder.imageInfo.filePath;
                    if (path != null && path.equals(filePath)) {
                        mViewHolder.updateBitmap(newBitmap);
                    }
                }
            });
        }
    }

    private class DataAdapter extends BaseAdapter {

        private List mList = new ArrayList();

        public void setDataList(List list) {
            synchronized (mList) {
                mList.clear();
                if (list != null) {
                    mList.addAll(list);
                }
            }
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            if (position >= mList.size()) {
                return null;
            }
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.photo_item, null);
                ImageView photoImageView = (ImageView) view.findViewById(R.id.image);
                TextView loadFailedText = (TextView) view.findViewById(R.id.load_fail);
                RelativeLayout openGallery = (RelativeLayout) view.findViewById(R.id.open_gallery);
                RelativeLayout showMorePhoto = (RelativeLayout) view.findViewById(R.id.show_more);

                holder = new ViewHolder();
                holder.view = view;
                holder.photoImageView = photoImageView;
                holder.loadFailedText = loadFailedText;
                holder.openGallery = openGallery;
                holder.showMorePhoto = showMorePhoto;
                holder.view.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Object obj = getItem(position);
            if (obj == null) {
                //open gallery
                holder.showOpenGallery();
            } else if (obj instanceof ImageInfo) {
                //show photo
                ImageInfo info = (ImageInfo) obj;
                holder.showPhoto(info);
            } else if (obj instanceof ArrayList) {
                //show more
                holder.showMorePhoto();
            }
            return holder.view;
        }
    }

    private class ViewHolder {
        public View view;
        public ImageView photoImageView;
        public TextView loadFailedText;
        public RelativeLayout openGallery;
        public RelativeLayout showMorePhoto;

        public ImageInfo imageInfo;
        private ImageLoaderCallback mCallback = null;

        public void showPhoto(ImageInfo info) {
            if (openGallery.getVisibility() != View.GONE) {
                openGallery.setVisibility(View.GONE);
            }
            if (showMorePhoto.getVisibility() != View.GONE) {
                showMorePhoto.setVisibility(View.GONE);
            }
            if (photoImageView.getVisibility() != View.VISIBLE) {
                photoImageView.setVisibility(View.VISIBLE);
            }
            Object tag = photoImageView.getTag();
            if (tag != null && tag instanceof String) {
                if (((String) tag).equals(info.filePath)) {
                    //already loaded
                    return;
                }
            }
            boolean resetCallback = false;
            if (imageInfo == null) {
                imageInfo = info;
                resetCallback = true;
            } else if (imageInfo != info) {
                imageInfo = info;
                resetCallback = true;
            }
            if (resetCallback) {
                mCallback = new ImageLoaderCallback(this);
            }
            mImageLoader.loadImage(imageInfo.filePath, mCallback);
        }

        public void showMorePhoto() {
            if (photoImageView.getVisibility() != View.GONE) {
                photoImageView.setVisibility(View.GONE);
            }
            if (loadFailedText.getVisibility() != View.GONE) {
                loadFailedText.setVisibility(View.GONE);
            }
            if (openGallery.getVisibility() != View.GONE) {
                openGallery.setVisibility(View.GONE);
            }
            if (showMorePhoto.getVisibility() != View.VISIBLE) {
                showMorePhoto.setVisibility(View.VISIBLE);
            }
        }

        public void showOpenGallery() {
            if (photoImageView.getVisibility() != View.GONE) {
                photoImageView.setVisibility(View.GONE);
            }
            if (loadFailedText.getVisibility() != View.GONE) {
                loadFailedText.setVisibility(View.GONE);
            }
            if (showMorePhoto.getVisibility() != View.GONE) {
                showMorePhoto.setVisibility(View.GONE);
            }
            if (openGallery.getVisibility() != View.VISIBLE) {
                openGallery.setVisibility(View.VISIBLE);
            }
        }

        public void updateBitmap(Bitmap bmp) {
            String path = imageInfo.filePath;
            if (bmp == null) {
                loadFailedText.setVisibility(View.VISIBLE);
                photoImageView.setVisibility(View.GONE);
            } else {
                loadFailedText.setVisibility(View.GONE);
                photoImageView.setVisibility(View.VISIBLE);
                photoImageView.setTag(path);
                Drawable oldBg = photoImageView.getBackground();
                photoImageView.setBackground(new BitmapDrawable(mContext.getResources(),bmp));
                if (oldBg != null && (oldBg instanceof BitmapDrawable)) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) oldBg;
                    bitmapDrawable.setCallback(null);
                    Bitmap oldBitmap = bitmapDrawable.getBitmap();
                    if (oldBitmap != null) {
                        oldBitmap.recycle();
                    }
                }
            }
        }
    }
}