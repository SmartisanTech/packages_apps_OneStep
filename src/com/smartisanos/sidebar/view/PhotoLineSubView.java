package com.smartisanos.sidebar.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.ImageLoader;
import com.smartisanos.sidebar.util.Utils;

import java.io.File;

import smartisanos.util.SidebarUtils;

public class PhotoLineSubView extends FrameLayout {
    private static final int IMAGE_COLOR = Color.parseColor("#9a404040");

    ImageView photoImageView;
    TextView loadFailedText;
    RelativeLayout openGallery;
    RelativeLayout showMorePhoto;

    private ImageInfo imageInfo;
    private ImageLoader mImageLoader;
    private ImageLoaderCallBack mCallBack;

    public PhotoLineSubView(Context context) {
        this(context, null);
    }

    public PhotoLineSubView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoLineSubView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PhotoLineSubView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        photoImageView = (ImageView) findViewById(R.id.image);
        loadFailedText = (TextView) findViewById(R.id.load_fail);
        openGallery = (RelativeLayout) findViewById(R.id.open_gallery);
        showMorePhoto = (RelativeLayout) findViewById(R.id.show_more);
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openGallery(v.getContext());
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TextView openText = (TextView)findViewById(R.id.open_gallery_text);
        openText.setText(R.string.open_gallery);
        TextView moreText = (TextView)findViewById(R.id.show_more_text);
        moreText.setText(R.string.load_more);
        loadFailedText.setText(R.string.fail_to_load_image);
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    public void reset() {
        photoImageView.setVisibility(View.INVISIBLE);
        loadFailedText.setVisibility(View.INVISIBLE);
        openGallery.setVisibility(View.INVISIBLE);
        showMorePhoto.setVisibility(View.INVISIBLE);
    }

    public void showPhoto(ImageInfo info) {
        photoImageView.setVisibility(View.VISIBLE);
        if (imageInfo != null && imageInfo.filePath.equals(info.filePath)) {
            return ;
        }
        imageInfo = info;
        photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openPhotoWithGallery(v.getContext(), imageInfo);
            }
        });

        photoImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SidebarUtils.dragImage(v, mContext, new File(imageInfo.filePath), imageInfo.mimeType);
                return true;
            }
        });

        if(mCallBack != null) {
            mCallBack.setValid(false);
        }
        Drawable oldBg = photoImageView.getBackground();
        photoImageView.setBackgroundColor(IMAGE_COLOR);
        if (oldBg != null && (oldBg instanceof BitmapDrawable)) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) oldBg;
            bitmapDrawable.setCallback(null);
            Bitmap oldBitmap = bitmapDrawable.getBitmap();
            if (oldBitmap != null) {
                oldBitmap.recycle();
            }
        }
        mImageLoader.loadImage(imageInfo.filePath, mCallBack = new ImageLoaderCallBack());
    }

    public void showMorePhoto(View.OnClickListener listener) {
        reset();
        showMorePhoto.setVisibility(View.VISIBLE);
        showMorePhoto.setOnClickListener(listener);
    }

    public void showOpenGallery() {
        if (openGallery.getVisibility() != View.VISIBLE) {
            openGallery.setVisibility(View.VISIBLE);
        }
    }

    public void updateBitmap(Bitmap bmp) {
        if (photoImageView.getVisibility() != View.VISIBLE) {
            bmp.recycle();
            return;
        }
        String path = imageInfo.filePath;
        Drawable oldBg = photoImageView.getBackground();
        photoImageView.setBackground(new BitmapDrawable(mContext.getResources(), bmp));
        if (oldBg != null && (oldBg instanceof BitmapDrawable)) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) oldBg;
            bitmapDrawable.setCallback(null);
            Bitmap oldBitmap = bitmapDrawable.getBitmap();
            if (oldBitmap != null) {
                oldBitmap.recycle();
            }
        }
    }

    class ImageLoaderCallBack implements ImageLoader.Callback {

        private boolean mValid = true;

        public void setValid(boolean valid) {
            mValid = valid;
        }

        @Override
        public boolean valid() {
            return mValid;
        }

        @Override
        public void onLoadComplete(final String filePath, Bitmap bitmap) {
            if (imageInfo.filePath == null || !imageInfo.filePath.equals(filePath)) {
                return ;
            }
            Bitmap newBitmap = BitmapUtils.allNewBitmap(bitmap);
            if(newBitmap == null) {
                return ;
            }
            post(new SetBitmapTask(newBitmap, imageInfo.filePath));
        }
    }

    class SetBitmapTask implements Runnable {
        private Bitmap mBitmap;
        private String mFilePath;
        public SetBitmapTask(Bitmap newBitmap, String filePath) {
            mBitmap = newBitmap;
            mFilePath = filePath;
        }

        @Override
        public void run() {
            if (imageInfo.filePath != null && imageInfo.filePath.equals(mFilePath)) {
                updateBitmap(mBitmap);
            } else {
                mBitmap.recycle();
            }
        }
    }
}
