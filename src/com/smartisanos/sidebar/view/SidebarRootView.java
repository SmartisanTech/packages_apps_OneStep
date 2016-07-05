package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        removeView(mDragView.mView);
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

        private Drawable iconOrig;

        private int itemType;
        private ResolveInfoGroup resolveInfoGroup;
        private ContactItem contactItem;
        public int floatUpIndex;
        public int viewIndex;
        public String mDisplayName;

        private boolean isSystemApp = false;

        public DragItem(Context context, int type, Drawable icon, Object data, int initIndex) {
            mContext = context;
            itemType = type;
            iconOrig = icon;
            floatUpIndex = initIndex;
            viewIndex = initIndex;
            if (itemType == TYPE_APPLICATION) {
                resolveInfoGroup = (ResolveInfoGroup) data;
                String pkg = resolveInfoGroup.getPackageName();
                isSystemApp = isSystemAppByPackageName(context, pkg);
                PackageManager pm = context.getPackageManager();
                mDisplayName = resolveInfoGroup.loadLabel(pm).toString();
            } else if (itemType == TYPE_SHORTCUT) {
                contactItem = (ContactItem) data;
                mDisplayName = contactItem.getDisplayName().toString();
            }
        }

        public int getItemType() {
            return itemType;
        }

        public boolean isSystemApp() {
            return isSystemApp;
        }

        public String getDisplayName() {
            if (itemType == TYPE_APPLICATION) {
                PackageManager pm = mContext.getPackageManager();
                return resolveInfoGroup.loadLabel(pm).toString();
            } else if (itemType == TYPE_SHORTCUT) {
                return contactItem.getDisplayName().toString();
            } else {
                return null;
            }
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

    public static class DragView {

        ValueAnimator mAnim;
        private float mOffsetX = 0.0f;
        private float mOffsetY = 0.0f;
        private float mInitialScale = 1f;

        private Drawable mIcon;
        private Paint mPaint;
        private DragItem mItem;

        public int iconWidth;
        public int iconHeight;

        public View mView;
        public ImageView mDragViewIcon;
        public TextView mBubbleText;

        private int bubbleTextWidth;
        private int bubbleTextHeight;

        public DragView(Context context, DragItem item) {
            mView = LayoutInflater.from(context).inflate(R.layout.drag_view, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mView.setLayoutParams(params);
            mItem = item;
            mIcon = item.iconOrig;
            mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
            final float offsetX = 0;
            final float offsetY = 0;
            final float initialScale = 1;
            iconWidth = mIcon.getIntrinsicWidth();
            iconHeight = mIcon.getIntrinsicHeight();
            Resources resources = context.getResources();
            final float scaleDps = resources.getDimensionPixelSize(R.dimen.dragViewScale);
            final float scale = (iconWidth + scaleDps) / iconWidth;

            mDragViewIcon = (ImageView) mView.findViewById(R.id.drag_view_icon);
            mDragViewIcon.setBackground(mIcon);
            log.error("mView size ["+mView.getWidth()+", "+mView.getHeight()+"]");
            log.error("mDragViewIcon size ["+mDragViewIcon.getWidth()+", "+mDragViewIcon.getHeight()+"]");

            mInitialScale = initialScale;
            mAnim = new ValueAnimator();
            mAnim.setFloatValues(0f, 1f);
            mAnim.setDuration(150);
            mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float value = (Float) animation.getAnimatedValue();
                    log.error("anim value ==> " + value);
                    final int deltaX = (int) ((value * offsetX) - mOffsetX);
                    final int deltaY = (int) ((value * offsetY) - mOffsetY);

                    mOffsetX += deltaX;
                    mOffsetY += deltaY;
                    float scaleX = initialScale + (value * (scale - initialScale));
                    float scaleY = initialScale + (value * (scale - initialScale));
                    mView.setScaleX(scaleX);
                    mView.setScaleY(scaleY);

                    if (mView.getParent() == null) {
                        animation.cancel();
                    } else {
                        float translateX = mView.getTranslationX() + deltaX;
                        float translateY = mView.getTranslationY() + deltaY;
                        mView.setTranslationX(translateX);
                        mView.setTranslationY(translateY);
                    }
                }
            });
            mAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mBubbleText = (TextView) mView.findViewById(R.id.drag_view_bubble_text);
                    mBubbleText.setText(mItem.mDisplayName);
                    mBubbleText.setVisibility(View.VISIBLE);
                    bubbleTextWidth = mBubbleText.getWidth();
                    bubbleTextHeight = mBubbleText.getHeight();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });

            // Force a measure, because Workspace uses getMeasuredHeight() before the layout pass
//            int ms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//            measure(ms, ms);
        }

        public void hideBubble() {
            if (mBubbleText != null) {
                mBubbleText.setVisibility(View.GONE);
            }
        }

        public void setVisibility(int visibility) {
            mView.setVisibility(visibility);
        }

        public DragItem getDragItem() {
            return mItem;
        }

        public void move(float touchX, float touchY) {
            int bubbleHeight = 0;
            if (mBubbleText != null) {
                bubbleHeight = mBubbleText.getHeight();
            }
            mView.setTranslationX(touchX - iconWidth / 2);
            mView.setTranslationY(touchY - iconHeight / 2 - bubbleHeight);
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
                addView(mDragView.mView);
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
        DragItem item = null;
        if (mDragView != null && mDragView.mView != null) {
            item = mDragView.getDragItem();
            mDragView.setVisibility(View.INVISIBLE);
            removeView(mDragView.mView);
        }
        if (mSideView != null) {
            if (item != null) {
                int index = item.viewIndex;
                if (item.itemType == DragItem.TYPE_APPLICATION) {
                    ResolveInfoGroup data = item.resolveInfoGroup;
                    log.error("A set item to index ["+index+"]");
                    ResolveInfoListAdapter adapter = mSideView.getAppListAdapter();
                    adapter.setItem(index, data);
                } else if (item.itemType == DragItem.TYPE_SHORTCUT) {
                    ContactItem contactItem = item.contactItem;
                    log.error("B set item to index ["+index+"]");
                    ContactListAdapter adapter = mSideView.getContactListAdapter();
                    adapter.setItem(index, contactItem);
                }
            }
            mSideView.notifyAppListDataSetChanged();
            mSideView.notifyContactListDataSetChanged();
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
        long eventTime = event.getEventTime();
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
                    //handle uninstall
                } else {
                    dropDrag();
//                    mTrash.trashDisappearWithoutAnim();
                    mTrash.trashDisappearWithAnim();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE : {
                mDragView.move(x, y);
                mSideView.dragObjectMove(x, y, eventTime);
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