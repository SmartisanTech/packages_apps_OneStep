package com.smartisanos.sidebar.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.FileInfo;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import android.content.Context;
import android.content.CopyHistoryItem;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class TopView extends LinearLayout {
    private static final String TAG = TopView.class.getName();

    private SidebarController mController;

    private TopItemView mPhotos, mFile, mClipboard;

    private Map<TopItemView, ContentType> mViewToType;

    private RecentPhotoManager mPhotoManager;
    private RecentFileManager mFileManager;
    private RecentClipManager mClipManager;

    private int mTopbarPhotoIconContentSize ;
    private int mTopbarPhotoIconContentPaddingTop;
    private int mTopbarFileIconContentPaddingTop;

    private boolean mFinishInflated = false;

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

        this.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View v) {
                // NA
            }

            @Override
            public void onViewAttachedToWindow(View v) {
                if (mFinishInflated) {
                    updatePhotoIconContent();
                    updateFileIconContent();
                    updateClipIconContent();
                }
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mFinishInflated = true;

        mPhotos = (TopItemView) findViewById(R.id.photo);
        mPhotos.setText(R.string.topbar_photo);
        mFile = (TopItemView) findViewById(R.id.file);
        mFile.setText(R.string.topbar_file);
        mClipboard = (TopItemView) findViewById(R.id.clipboard);
        mClipboard.setText(R.string.topbar_clipboard);

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
                    } else {
                        if (mController.getCurrentContentType() == mViewToType
                                .get(itemView)) {
                            mController.dismissContent(true);
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
                post(new Runnable() {
                    @Override
                    public void run() {
                        updatePhotoIconContent();
                    }
                });
            }
        });

        mFile.setIconContentPaddingTop(mTopbarFileIconContentPaddingTop);
        mFileManager = RecentFileManager.getInstance(mContext);
        mFileManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        updateFileIconContent();
                    }
                });
            }
        });

        mClipManager = RecentClipManager.getInstance(mContext);
        mClipManager.addListener(new RecentUpdateListener() {
            @Override
            public void onUpdate() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        updateClipIconContent();
                    }
                });
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                updatePhotoIconContent();
                updateFileIconContent();
                updateClipIconContent();
            }
        });
    }

    private void updatePhotoIconContent() {
        List<ImageInfo> mList = mPhotoManager.getImageList();
        if (mList.size() > 0) {
            mPhotos.setIconBackground(R.drawable.topbar_photo);
            Bitmap bmp = BitmapUtils.getSquareBitmap(mList.get(0).filePath, mTopbarPhotoIconContentSize);
            if (bmp != null) {
                mPhotos.setIconContent(bmp);
            }
        } else {
            mPhotos.setIconBackground(R.drawable.topview_photo_default);
            mPhotos.resetIconContent();
        }
    }

    private void updateFileIconContent() {
        List<FileInfo> mList = mFileManager.getFileList();
        if (mList.size() > 0) {
            mFile.setIconBackground(R.drawable.topbar_file);
            mFile.setIconContent(mContext.getResources().getDrawable(mList.get(0).getIconId()));
        }else{
            mFile.setIconBackground(R.drawable.topview_file_default);
            mFile.resetIconContent();
        }
    }

    private void updateClipIconContent() {
        List<CopyHistoryItem> list = mClipManager.getCopyList();
        if (list != null && list.size() > 0) {
            mClipboard.setIconBackground(R.drawable.topbar_clipboard);
        } else {
            mClipboard.setIconBackground(R.drawable.topview_clipboard_default);
        }
    }

    public void dimAll(){
        for (TopItemView view : mViewToType.keySet()) {
            view.dim();
        }
    }

    public void resumeToNormal(){
        for (TopItemView view : mViewToType.keySet()) {
            view.resume();
        }
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
