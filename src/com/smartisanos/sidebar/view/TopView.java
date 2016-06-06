package com.smartisanos.sidebar.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.FileInfo;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class TopView extends LinearLayout {
    private static final String TAG = TopView.class.getName();

    private SidebarController mController;

    private View mDimView;
    private TopItemView mPhotos, mFile, mClipboard;

    private Map<TopItemView, ContentType> mViewToType;

    private RecentPhotoManager mPhotoManager;
    private RecentFileManager mFileManager;

    private int mTopbarPhotoIconContentSize ;
    private int mTopbarPhotoIconContentPaddingTop;
    private int mTopbarFileIconContentPaddingTop;

    public TopView(Context context) {
        this(context, null);
    }

    public TopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TopView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mController = SidebarController.getInstance(mContext);
        mTopbarPhotoIconContentSize = context.getResources().getDimensionPixelSize(R.dimen.topbar_photo_icon_content_size);
        mTopbarPhotoIconContentPaddingTop = context.getResources().getDimensionPixelSize(R.dimen.topbar_photo_icon_content_paddingTop);
        mTopbarFileIconContentPaddingTop = context.getResources().getDimensionPixelSize(R.dimen.topbar_file_icon_content_paddingTop);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDimView = findViewById(R.id.dim_view);
        mPhotos = (TopItemView) findViewById(R.id.photo);
        mPhotos.setText(R.string.topbar_photo);
        mPhotos.setIconBackground(R.drawable.topbar_photo);
        mFile = (TopItemView) findViewById(R.id.file);
        mFile.setText(R.string.topbar_file);
        mFile.setIconBackground(R.drawable.topbar_file);
        mClipboard = (TopItemView) findViewById(R.id.clipboard);
        mClipboard.setText(R.string.topbar_clipboard);
        mClipboard.setIconBackground(R.drawable.topbar_clipboard);

        mViewToType = new HashMap<TopItemView, ContentType>();
        mViewToType.put(mPhotos, ContentType.PHOTO);
        mViewToType.put(mFile, ContentType.FILE);
        mViewToType.put(mClipboard, ContentType.CLIPBOARD);

        View.OnClickListener mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(v instanceof TopItemView)) {
                    // error !
                } else {
                    TopItemView itemView = (TopItemView) v;
                    if (mController.getCurrentContentType() == ContentType.NONE) {
                        mController.showContent(mViewToType.get(itemView));
                        for (TopItemView view : mViewToType.keySet()) {
                            if (view == itemView) {
                                view.highlight();
                            } else {
                                view.dim();
                            }
                        }
                        mDimView.setVisibility(View.VISIBLE);
                    } else {
                        if (mController.getCurrentContentType() == mViewToType
                                .get(itemView)) {
                            mController.dismissContent();
                            resumeToNormal();
                        } else {
                            // never happen !
                        }
                    }
                }
            }
        };

        mPhotos.setOnClickListener(mListener);
        mFile.setOnClickListener(mListener);
        mClipboard.setOnClickListener(mListener);

        // update icon content
        mPhotos.setIconContentPaddingTop(mTopbarPhotoIconContentPaddingTop);
        mPhotoManager = RecentPhotoManager.getInstance(mContext);
        mPhotoManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                updatePhotoIconContent();
            }
        });

        mFile.setIconContentPaddingTop(mTopbarFileIconContentPaddingTop);
        mFileManager = RecentFileManager.getInstance(mContext);
        mFileManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                updateFileIconContent();
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                updatePhotoIconContent();
                updateFileIconContent();
            }
        });
    }

    private void updatePhotoIconContent() {
        List<ImageInfo> mList = mPhotoManager.getImageList();
        if (mList.size() > 0) {
            Bitmap bmp = BitmapUtils.getBitmap(mList.get(0).filePath, mTopbarPhotoIconContentSize);
            if (bmp != null) {
                mPhotos.setIconContent(bmp);
            }
        }else{
            mPhotos.resetIconContent();
        }
    }

    private void updateFileIconContent() {
        List<FileInfo> mList = mFileManager.getFileList();
        if (mList.size() > 0) {
            mFile.setIconContent(mContext.getResources().getDrawable(mList.get(0).getIconId()));
        }else{
            mFile.resetIconContent();
        }
    }

    public void dimAll(){
        for (TopItemView view : mViewToType.keySet()) {
            view.dim();
        }
        mDimView.setVisibility(View.VISIBLE);
    }

    public void resumeToNormal(){
        for (TopItemView view : mViewToType.keySet()) {
            view.resume();
        }
        mDimView.setVisibility(View.GONE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (mController.getCurrentContentType() != ContentType.NONE) {
                Utils.resumeSidebar(mContext);
                Log.d(TAG, "content not none ! resume sidebar...");
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
