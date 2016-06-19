package com.smartisanos.sidebar.view;

import android.animation.ValueAnimator;
import android.content.Context;
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

    private Trash mTrash;
    public void setTrashView() {
        mTrash = new Trash(mContext, (FrameLayout) findViewById(R.id.trash));
    }

    public Trash getTrash() {
        return mTrash;
    }

    public static class DragItem {
        public static final int TYPE_APPLICATION = 1;
        public static final int TYPE_SHORTCUT = 2;

        private Bitmap iconOrig;
        private Bitmap iconFloatUp;

        private int itemType;
        private ResolveInfoGroup resolveInfoGroup;
        private ContactItem contactItem;
        private int oldIndex;

        public DragItem(int type, Bitmap icon, Object data, int initIndex) {
            itemType = type;
            iconOrig = icon;
            oldIndex = initIndex;
            if (itemType == TYPE_APPLICATION) {
                resolveInfoGroup = (ResolveInfoGroup) data;
            } else if (itemType == TYPE_SHORTCUT) {
                contactItem = (ContactItem) data;
            }
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

    private final float[] pointDownLoc = new float[2];

    private final boolean ENABLE_TOUCH_LOG = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            pointDownLoc[0] = event.getRawX();
            pointDownLoc[1] = event.getRawY();
            log.error("dispatchTouchEvent ACTION_DOWN ["+pointDownLoc[0]+"]["+pointDownLoc[1]+"]");
        }
        if (mDragView == null) {
            if (action == MotionEvent.ACTION_DOWN) {
                SidebarController controller = SidebarController.getInstance(mContext);
                if (controller != null) {
                    if (controller.getCurrentContentType() != ContentView.ContentType.NONE) {
                        Utils.resumeSidebar(mContext);
                        return true;
                    }
                }
            }
        } else {
            switch (action) {
                case MotionEvent.ACTION_DOWN : {
                    if (ENABLE_TOUCH_LOG) log.error("ACTION_DOWN");
                    pointDownLoc[0] = event.getX();
                    pointDownLoc[1] = event.getY();
                    break;
                }
                case MotionEvent.ACTION_UP : {
                    if (ENABLE_TOUCH_LOG) log.error("ACTION_UP");
                    mTrash.trashDisappearWithAnim(this);
                    break;
                }
                case MotionEvent.ACTION_MOVE : {
                    float x = event.getX();
                    float y = event.getY();
                    mDragView.move(x, y);
                    if (mTrash.inTrashReactArea(x, y)) {
                        //in trash area
                        mTrash.trashFloatUpWithAnim();
                    } else {
                        //out trash area
                        mTrash.trashFallDownWithAnim();
                    }
                    if (ENABLE_TOUCH_LOG) log.error("ACTION_MOVE");
                    break;
                }
                case MotionEvent.ACTION_CANCEL : {
                    if (ENABLE_TOUCH_LOG) log.error("ACTION_CANCEL");
                    mTrash.trashDisappearWithAnim(this);
                    break;
                }
                case MotionEvent.ACTION_SCROLL : {
                    if (ENABLE_TOUCH_LOG) log.error("ACTION_SCROLL");
                    break;
                }
            }
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    public void resetSidebarWindow() {
        removeView(mDragView);
        mDragView = null;
        SidebarController.getInstance(mContext).updateDragWindow(false);
    }
}