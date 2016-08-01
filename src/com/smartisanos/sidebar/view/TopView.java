package com.smartisanos.sidebar.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.BitmapUtils;
import com.smartisanos.sidebar.util.FileInfo;
import com.smartisanos.sidebar.util.ImageInfo;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.RecentClipManager;
import com.smartisanos.sidebar.util.RecentFileManager;
import com.smartisanos.sidebar.util.RecentPhotoManager;
import com.smartisanos.sidebar.util.RecentUpdateListener;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.view.ContentView.ContentType;

import android.content.Context;
import android.content.CopyHistoryItem;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class TopView extends FrameLayout {
    private static final LOG log = LOG.getInstance(TopView.class);

    private SidebarController mController;

    private DimSpaceView mLeft, mRight;
    private TopItemView mPhotos, mFile, mClipboard;

    private Map<ITopItem, ContentType> mViewToType;

    private RecentPhotoManager mPhotoManager;
    private RecentFileManager mFileManager;
    private RecentClipManager mClipManager;

    private int mTopbarPhotoIconContentSize ;
    private int mTopbarPhotoIconContentPaddingTop;
    private int mTopbarFileIconContentPaddingTop;

    private View mShadowLine;

    private boolean mFinishInflated = false;
    private Context mContext;

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
        mContext = context;
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

        mLeft = (DimSpaceView) findViewById(R.id.top_dim_view_left);
        mRight = (DimSpaceView) findViewById(R.id.top_dim_view_right);

        mPhotos = (TopItemView) findViewById(R.id.photo);
        mPhotos.setText(R.string.topbar_photo);
        mFile = (TopItemView) findViewById(R.id.file);
        mFile.setText(R.string.topbar_file);
        mClipboard = (TopItemView) findViewById(R.id.clipboard);
        mClipboard.setText(R.string.topbar_clipboard);

        mViewToType = new HashMap<ITopItem, ContentType>();
        mViewToType.put(mLeft, ContentType.NONE);
        mViewToType.put(mPhotos, ContentType.PHOTO);
        mViewToType.put(mFile, ContentType.FILE);
        mViewToType.put(mClipboard, ContentType.CLIPBOARD);
        mViewToType.put(mRight, ContentType.NONE);

        mPhotos.setOnClickListener(mItemOnClickListener);
        mFile.setOnClickListener(mItemOnClickListener);
        mClipboard.setOnClickListener(mItemOnClickListener);

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
        mShadowLine = findViewById(R.id.top_view_shadow_line);
    }

    private View.OnClickListener mItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!(v instanceof TopItemView)) {
                // error !
                return;
            }
            if (!AnimStatusManager.getInstance().canShowContentView()) {
                AnimStatusManager.getInstance().dumpStatus();
                return;
            }

            TopItemView itemView = (TopItemView) v;
            if (mController.getCurrentContentType() == ContentType.NONE) {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_TOP_VIEW_CLICK, true);
                AnimTimeLine animTimeLine = new AnimTimeLine();
                mController.showContent(mViewToType.get(itemView));
                for (ITopItem view : mViewToType.keySet()) {
                    if (view == itemView) {
                        animTimeLine.addTimeLine(view.highlight());
                    } else {
                        animTimeLine.addTimeLine(view.dim());
                    }
                }
                animTimeLine.setAnimListener(new AnimListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(int type) {
                        AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_TOP_VIEW_CLICK, false);
                    }
                });
                animTimeLine.start();
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
    };

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
        AnimTimeLine timeLine = new AnimTimeLine();
        for (ITopItem view : mViewToType.keySet()) {
            timeLine.addTimeLine(view.dim());
        }
        timeLine.start();
    }

    public void resumeToNormal() {
        AnimTimeLine timeLine = new AnimTimeLine();
        for (ITopItem view : mViewToType.keySet()) {
            timeLine.addTimeLine(view.resume());
        }
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_TOP_VIEW_RESUME, true);
            }

            @Override
            public void onComplete(int type) {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_TOP_VIEW_RESUME, false);
            }
        });
        timeLine.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (mController.getCurrentContentType() != ContentType.NONE) {
                if (AnimStatusManager.getInstance().canShowContentView()) {
                    Utils.resumeSidebar(mContext);
                    log.error("content not none ! resume sidebar...");
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void doAnimWhenEnter() {
        mShadowLine.setVisibility(View.INVISIBLE);
        int time = 150;
        int height = getHeight() / 4;
        int fromY = -height;
        mPhotos.setAlpha(0);
        mFile.setAlpha(0);
        mClipboard.setAlpha(0);
        Anim photoMove = new Anim(mPhotos, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(0, fromY), new Vector3f());
        Anim fileMove = new Anim(mFile, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(0, fromY), new Vector3f());
        Anim clipboardMove = new Anim(mClipboard, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(0, fromY), new Vector3f());

        Anim photoAlpha = new Anim(mPhotos, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
        Anim fileAlpha = new Anim(mFile, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
        Anim clipboardAlpha = new Anim(mClipboard, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));

        AnimTimeLine timeLine = new AnimTimeLine();
        timeLine.addAnim(photoMove);
        timeLine.addAnim(fileMove);
        timeLine.addAnim(clipboardMove);
        timeLine.addAnim(photoAlpha);
        timeLine.addAnim(fileAlpha);
        timeLine.addAnim(clipboardAlpha);
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_TOP_VIEW_ENTER, true);
            }

            @Override
            public void onComplete(int type) {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_TOP_VIEW_ENTER, false);
                TopView.this.setBackgroundResource(R.drawable.background);
                mPhotos.setAlpha(1);
                mFile.setAlpha(1);
                mClipboard.setAlpha(1);

                mPhotos.setTranslationY(0);
                mFile.setTranslationY(0);
                mClipboard.setTranslationY(0);

                Anim showShadowLine = new Anim(mShadowLine, Anim.TRANSPARENT, 50, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
                showShadowLine.setListener(new AnimListener() {
                    @Override
                    public void onStart() {
                        mShadowLine.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onComplete(int type) {
                        mShadowLine.setAlpha(1);
                    }
                });
                showShadowLine.start();
            }
        });
        timeLine.start();
    }

    private void doAnimWhenExit() {
        TopView.this.setBackgroundResource(android.R.color.transparent);
        mShadowLine.setVisibility(View.INVISIBLE);
        int time = 150;
        int toY = -67;
        Anim photoMove = new Anim(mPhotos, Anim.MOVE, time, 0, new Vector3f(), new Vector3f(0, toY));
        Anim fileMove = new Anim(mFile, Anim.MOVE, time, 0, new Vector3f(), new Vector3f(0, toY));
        Anim clipboardMove = new Anim(mClipboard, Anim.MOVE, time, 0, new Vector3f(), new Vector3f(0, toY));

        Anim photoAlpha = new Anim(mPhotos, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
        Anim fileAlpha = new Anim(mFile, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
        Anim clipboardAlpha = new Anim(mClipboard, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());

        AnimTimeLine timeLine = new AnimTimeLine();
        timeLine.addAnim(photoMove);
        timeLine.addAnim(fileMove);
        timeLine.addAnim(clipboardMove);
        timeLine.addAnim(photoAlpha);
        timeLine.addAnim(fileAlpha);
        timeLine.addAnim(clipboardAlpha);
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_TOP_VIEW_EXIT, true);
            }

            @Override
            public void onComplete(int type) {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_TOP_VIEW_EXIT, false);
                mPhotos.setAlpha(1);
                mFile.setAlpha(1);
                mClipboard.setAlpha(1);

                mPhotos.setTranslationY(0);
                mFile.setTranslationY(0);
                mClipboard.setTranslationY(0);

                setVisibility(View.GONE);
                resumeToNormal();
            }
        });
        timeLine.start();
    }

    public void show(boolean show) {
        if (show) {
            setVisibility(View.VISIBLE);
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    observer.removeOnGlobalLayoutListener(this);
                    doAnimWhenEnter();
                }
            });
        } else {
            doAnimWhenExit();
        }
    }
}
