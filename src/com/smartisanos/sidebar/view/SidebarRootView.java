package com.smartisanos.sidebar.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.R;

public class SidebarRootView extends FrameLayout {

    private static final LOG log = LOG.getInstance(SidebarRootView.class);

    private Context mContext;
    private DragView mDragView;
    private SideView mSideView;

    public SidebarRootView(Context context) {
        super(context);
        mContext = context;
    }

    public SidebarRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public SidebarRootView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void resetSidebarWindow() {
        removeView(mDragView);
        mDragView = null;
        SidebarController.getInstance(mContext).updateDragWindow(false);
    }

    public void setSideView(SideView sideView) {
        mSideView = sideView;
    }

    private Trash mTrash;
    public void setTrashView() {
        mTrash = new Trash(mContext, (FrameLayout) findViewById(R.id.trash));
        mTrash.mRootView = this;
    }

    public Trash getTrash() {
        return mTrash;
    }

    public static class DragItem {
        public static final int TYPE_APPLICATION = 1;
        public static final int TYPE_SHORTCUT = 2;

        private Context mContext;

        private Bitmap iconOrig;
        private Bitmap iconFloatUp;

        private int itemType;
        private ResolveInfoGroup resolveInfoGroup;
        private ContactItem contactItem;
        public int floatUpIndex;
        public int switchIndex;

        private boolean isSystemApp = false;

        public DragItem(Context context, int type, Bitmap icon, Object data, int initIndex) {
            mContext = context;
            itemType = type;
            iconOrig = icon;
            floatUpIndex = initIndex;
            switchIndex = initIndex;
            if (itemType == TYPE_APPLICATION) {
                resolveInfoGroup = (ResolveInfoGroup) data;
                String pkg = resolveInfoGroup.getPackageName();
                isSystemApp = isSystemAppByPackageName(context, pkg);
            } else if (itemType == TYPE_SHORTCUT) {
                contactItem = (ContactItem) data;
            }
        }

        public int getItemType() {
            return itemType;
        }

        public boolean isSystemApp() {
            return isSystemApp;
        }

        public static final int FLAG_PRIVILEGED = 1<<30;
        public static final int PRIVATE_FLAG_PRIVILEGED = 1<<3;

        public static boolean isSystemAppByPackageName(Context context, final String pkgName) {
            PackageManager pm = context.getPackageManager();
            int appFlags = 0;
            try {
                appFlags = pm.getApplicationInfo(pkgName, 0).flags;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    || (appFlags & FLAG_PRIVILEGED) != 0
                    || (appFlags & PRIVATE_FLAG_PRIVILEGED) != 0) {
                try {
                    PackageInfo pinfo = pm.getPackageInfo(pkgName, 0);
                    if(pinfo != null) {
                        String path = pinfo.applicationInfo.sourceDir;
                        if(path != null && path.startsWith("/system")) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    public static class DragView extends View {

        ValueAnimator mAnim;
        private float mOffsetX = 0.0f;
        private float mOffsetY = 0.0f;
        private float mInitialScale = 1f;

        private Bitmap mBitmap;
        private Paint mPaint;
        private DragItem mItem;

        private int viewWidth;
        private int viewHeight;

        public DragView(Context context, DragItem item) {
            super(context);
            mItem = item;
            mBitmap = item.iconOrig;
            mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
            final float offsetX = 0;
            final float offsetY = 0;
            final float initialScale = 1;
            viewWidth = mBitmap.getWidth();
            viewHeight = mBitmap.getHeight();
            Resources resources = context.getResources();
            final float scaleDps = resources.getDimensionPixelSize(R.dimen.dragViewScale);
            final float scale = (viewWidth + scaleDps) / viewWidth;

            mInitialScale = initialScale;
            mAnim = new ValueAnimator();
            mAnim.setFloatValues(0f, 1f);
            mAnim.setDuration(150);
            mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float value = (Float) animation.getAnimatedValue();
                    final int deltaX = (int) ((value * offsetX) - mOffsetX);
                    final int deltaY = (int) ((value * offsetY) - mOffsetY);

                    mOffsetX += deltaX;
                    mOffsetY += deltaY;
                    float scaleX = initialScale + (value * (scale - initialScale));
                    float scaleY = initialScale + (value * (scale - initialScale));
                    setScaleX(scaleX);
                    setScaleY(scaleY);

                    if (getParent() == null) {
                        animation.cancel();
                    } else {
                        float translateX = getTranslationX() + deltaX;
                        float translateY = getTranslationY() + deltaY;
//                        log.error("DragView translate ["+translateX+"] ["+translateY+"]");
                        setTranslationX(translateX);
                        setTranslationY(translateY);
                    }
                }
            });

            // Force a measure, because Workspace uses getMeasuredHeight() before the layout pass
            int ms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            measure(ms, ms);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
        }

        public DragItem getDragItem() {
            return mItem;
        }

        public void move(float touchX, float touchY) {
            setTranslationX(touchX - viewWidth / 2);
            setTranslationY(touchY - viewHeight / 2);
        }

        public void cancel() {

        }

        public void destroy() {
        }

        public void startAnim() {
            mAnim.start();
        }

        public void cancelAnimation() {
            if (mAnim != null && mAnim.isRunning()) {
                mAnim.cancel();
            }
        }
    }

    public void startDrag(DragItem item) {
        if (mDragView != null) {
            return;
        }
        //set sidebar to full screen
        SidebarController.getInstance(mContext).updateDragWindow(true);
        mDragView = new DragView(mContext, item);
        mDragView.clearFocus();
        post(new Runnable() {
            public void run() {
                mTrash.trashAppearWithAnim();
                mDragView.setVisibility(View.INVISIBLE);
                addView(mDragView);
                final float touchX = pointDownLoc[0];
                final float touchY = pointDownLoc[1];
                log.error("startDrag ["+touchX+"]["+touchY+"]");
                mDragView.move(touchX, touchY);
                mDragView.setVisibility(View.VISIBLE);
                post(new Runnable() {
                    @Override
                    public void run() {
                        mDragView.startAnim();
                    }
                });
            }
        });
    }

    public void dropDrag() {
        log.error("dropDrag !");
        if (mDragView != null) {
            mDragView.clearFocus();
            mDragView.setVisibility(View.INVISIBLE);
            removeView(mDragView);
        }
        if (mSideView != null) {
            mSideView.notifyAppListDataSetChanged();
        }
    }

    public DragView getDraggedView() {
        return mDragView;
    }

    private final float[] pointDownLoc = new float[2];

    private final boolean ENABLE_TOUCH_LOG = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            pointDownLoc[0] = event.getRawX();
            pointDownLoc[1] = event.getRawY();
            log.error("dispatchTouchEvent ACTION_DOWN ["+pointDownLoc[0]+"]["+pointDownLoc[1]+"]");
        }
        if (mDragView == null) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (processResumeSidebar()) {
                    return true;
                }
            }
        } else {
            precessTouch(event);
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mDragView != null) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    private boolean processResumeSidebar() {
        SidebarController controller = SidebarController.getInstance(mContext);
        if (controller != null) {
            if (controller.getCurrentContentType() != ContentView.ContentType.NONE) {
                log.error("resumeSidebar !");
                Utils.resumeSidebar(mContext);
                return true;
            }
        }
        return false;
    }

    private void precessTouch(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_DOWN");
                pointDownLoc[0] = x;
                pointDownLoc[1] = y;
                break;
            }
            case MotionEvent.ACTION_UP : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_UP");
                if (mTrash.dragObjectUpOnUp(x, y)) {
                    mTrash.trashDisappearWithAnim(this);
                    break;
                }
                dropDrag();
                mTrash.trashDisappearWithAnim(this);
                break;
            }
            case MotionEvent.ACTION_MOVE : {
                mDragView.move(x, y);
                mSideView.dragObjectMove(x, y);
                mTrash.dragObjectMoveTo(x, y);
                if (ENABLE_TOUCH_LOG) log.error("ACTION_MOVE");
                break;
            }
            case MotionEvent.ACTION_CANCEL : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_CANCEL");
                break;
            }
            case MotionEvent.ACTION_SCROLL : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_SCROLL");
                break;
            }
        }
    }
}