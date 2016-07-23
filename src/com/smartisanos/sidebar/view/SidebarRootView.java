package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
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
import com.smartisanos.sidebar.action.UninstallAction;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.SidebarItem;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.R;

public class SidebarRootView extends FrameLayout {

    private static final LOG log = LOG.getInstance(SidebarRootView.class);

    private Context mContext;
    private DragView mDragView;
    private SideView mSideView;

    public SidebarRootView(Context context) {
        this(context, null);
    }

    public SidebarRootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SidebarRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SidebarRootView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public void resetSidebarWindow() {
        if (mDragView != null) {
            if (mDragView.mView != null) {
                removeView(mDragView.mView);
            }
        }
        mDragView = null;
        SidebarController.getInstance(mContext).updateDragWindow(false);
    }

    public void setSideView(SideView sideView) {
        mSideView = sideView;
    }

    private Trash mTrash;
    public void setTrashView() {
        mTrash = new Trash(mContext, (ImageView) findViewById(R.id.trash_with_shadow), (ImageView) findViewById(R.id.trash_foreground));
        mTrash.mRootView = this;
    }

    public Trash getTrash() {
        return mTrash;
    }

    public static class DragItem {
        public static final int TYPE_APPLICATION = 1;
        public static final int TYPE_SHORTCUT = 2;
        private Drawable iconOrig;
        private int itemType;
        public SidebarItem sidebarItem;
        public int floatUpIndex;
        public int viewIndex;
        public String displayName;
        public View mListItemView;

        public DragItem(int type, Drawable icon, SidebarItem item, int initIndex) {
            itemType = type;
            iconOrig = icon;
            floatUpIndex = initIndex;
            viewIndex = initIndex;
            sidebarItem = item;
            displayName = sidebarItem.getDisplayName().toString();
        }

        public void delelte(){
            sidebarItem.delete();
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

        private int[] initLoc;

        private boolean mAlreadyInit = false;

        public DragView(Context context, DragItem item, int[] loc) {
            mView = LayoutInflater.from(context).inflate(R.layout.drag_view, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mView.setLayoutParams(params);
            mItem = item;
            mIcon = item.iconOrig;
            mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

            mDragViewIcon = (ImageView) mView.findViewById(R.id.drag_view_icon);
            mDragViewIcon.setBackground(mIcon);
            mBubbleText = (TextView) mView.findViewById(R.id.drag_view_bubble_text);
            mBubbleText.setText(mItem.displayName);
            initLoc = loc;
            log.error("init loc ==> ("+initLoc[0]+", "+initLoc[1]+")");

//            mInitialScale = initialScale;
            mAnim = new ValueAnimator();
            mAnim.setFloatValues(0f, 1f);
            mAnim.setDuration(100);
            mAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    Anim bubbleAlpha = new Anim(mBubbleText, Anim.TRANSPARENT, 100, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
                    bubbleAlpha.start();
                    mView.setVisibility(View.VISIBLE);
                    iconWidth = mDragViewIcon.getWidth();
                    iconHeight = mDragViewIcon.getHeight();
                    mAlreadyInit = true;
                    move(initLoc[0], initLoc[1]);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
        }

        public void setBubbleVisibleStatus(int visible) {
            if (mBubbleText != null) {
                mBubbleText.setVisibility(visible);
            }
        }

        public void hideBubble() {
            if (mBubbleText != null) {
                mBubbleText.setVisibility(View.GONE);
            }
        }

        public DragItem getDragItem() {
            return mItem;
        }

        public void move(float touchX, float touchY) {
            if (!mAlreadyInit) {
                log.error("not ready, update loc");
                initLoc = new int[] {(int)touchX, (int)touchY};
                return;
            }
            int bubbleHeight = 0;
            if (mBubbleText != null) {
                bubbleHeight = mBubbleText.getHeight();
            }
            int width = mView.getWidth();
            int x = (int) (touchX - width / 2);
            int y = (int) (touchY - iconHeight / 2 - bubbleHeight);
            mView.setTranslationX(x);
            mView.setTranslationY(y);
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

    private boolean mDragging = false;
    public void startDrag(DragItem item, int[] loc) {
        if(mDragging){
            return ;
        }
        mDragging = true;
        //set sidebar to full screen
        SidebarController.getInstance(mContext).updateDragWindow(true);
        mDragView = new DragView(mContext, item, loc);
        mDragView.mView.setVisibility(View.INVISIBLE);
        addView(mDragView.mView);
        post(new Runnable() {
            public void run() {
                mTrash.trashAppearWithAnim();
                mDragView.startAnim();
            }
        });
    }

    private boolean mDragDeleting = false;
    public void deleteDrag(){
        if (!mDragging) {
            return;
        }
        int trashLocX = (int) mTrash.mTrashView.getTranslationX();
        int trashLocY = (int) mTrash.mTrashView.getTranslationY();
        mTrash.mTrashForegroundView.setTranslationX(trashLocX);
        mTrash.mTrashForegroundView.setTranslationY(trashLocY);
        mTrash.mTrashForegroundView.setVisibility(View.VISIBLE);

        setChildrenDrawingOrderEnabled(true);
        mEnableUninstallAnim = true;
        requestLayout();
        final View view = mDragView.mView;
        Vector3f from = new Vector3f(0, view.getY());
        Vector3f to = new Vector3f(0, mTrash.mTrashView.getY() + 20);
        Anim anim = new Anim(view, Anim.TRANSLATE, 150, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
                mDragDeleting = true;
            }

            @Override
            public void onComplete(int type) {
                mDragDeleting = false;
                setChildrenDrawingOrderEnabled(false);
                mEnableUninstallAnim = false;
                view.setVisibility(View.INVISIBLE);
                removeView(view);
                mTrash.trashDisappearWithAnim(new Runnable() {
                    @Override
                    public void run() {
                        SidebarRootView rootView = SidebarController.getInstance(mContext).getSidebarRootView();
                        rootView.resetSidebarWindow();
                    }
                });
                mDragging = false;
            }
        });
        anim.start();
    }

    private boolean mDragDroping = false;
    public void dropDrag() {
        if (!mDragging) {
            return;
        }
        log.error("dropDrag !");
        final DragItem item = mDragView.getDragItem();
        final View view = mDragView.mView;
        ImageView icon = mDragView.mDragViewIcon;
        mDragView.hideBubble();
        int time = 200;
        int[] from = new int[2];
        int[] to = new int[2];
        icon.getLocationOnScreen(from);
        item.mListItemView.getLocationOnScreen(to);
        int dragViewSize = mContext.getResources().getDimensionPixelSize(R.dimen.drag_view_icon_size);
        int itemViewSize = mContext.getResources().getDimensionPixelSize(R.dimen.sidebar_list_item_img_size);
        float scale = (float) ((1.0 * itemViewSize) / (1.0 * dragViewSize));
//        log.error("dropDrag from ("+from[0]+", "+from[1]+"), to ("+to[0]+", "+to[1]+")");
        Anim moveAnim = new Anim(view, Anim.TRANSLATE, time, Anim.CUBIC_IN, new Vector3f(from[0], from[1]), new Vector3f(to[0], to[1]));
        Anim scaleAnim = new Anim(view, Anim.SCALE, time, Anim.CUBIC_OUT, new Vector3f(1, 1), new Vector3f(scale, scale));
        AnimTimeLine timeLine = new AnimTimeLine();
        timeLine.addAnim(moveAnim);
        timeLine.addAnim(scaleAnim);
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                mDragDroping = true;
            }

            @Override
            public void onComplete(int type) {
                mDragDroping = false;
                view.setVisibility(View.INVISIBLE);
                removeView(view);
                if (mSideView != null) {
                    if (item != null) {
                        item.mListItemView.setVisibility(View.VISIBLE);
                        if (item.itemType == DragItem.TYPE_APPLICATION) {
                            mSideView.getAppListAdapter().moveItemPostion((ResolveInfoGroup) item.sidebarItem, item.viewIndex);
                        } else if (item.itemType == DragItem.TYPE_SHORTCUT) {
                            mSideView.getContactListAdapter().moveItemPostion((ContactItem) item.sidebarItem, item.viewIndex);
                        }
                    }
                }

                SidebarController controller = SidebarController.getInstance(mContext);
                SidebarRootView rootView = controller.getSidebarRootView();
                rootView.resetSidebarWindow();
                mDragging = false;
            }
        });
        timeLine.start();
    }

    public DragView getDraggedView() {
        if(!mDragging){
            return null;
        }
        return mDragView;
    }

    private final boolean ENABLE_TOUCH_LOG = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mDragDroping || mDragDeleting) {
            // ignore !
            return true;
        }

        if (mDragging) {
            precessTouch(ev);
            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (processResumeSidebar()) {
                return true;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean processResumeSidebar() {
        if (SidebarController.getInstance(mContext).getCurrentContentType() != ContentView.ContentType.NONE) {
            log.error("processResumeSidebar !");
            Utils.resumeSidebar(mContext);
            return true;
        }
        return false;
    }

    private void precessTouch(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        long eventTime = event.getEventTime();
        switch (action) {
            case MotionEvent.ACTION_DOWN : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_DOWN");
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_UP");
                if (mTrash.dragObjectUpOnUp(x, y)) {
                    //handle uninstall
                } else {
                    dropDrag();
                    mTrash.trashDisappearWithAnim(null);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_MOVE");
                mDragView.move(x, y);
                mSideView.dragObjectMove(x, y, eventTime);
                mTrash.dragObjectMoveTo(x, y);
                break;
            }
            case MotionEvent.ACTION_SCROLL : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_SCROLL");
                break;
            }
        }
    }

    private void onDismiss() {
        if (mDragView != null) {
            DragItem item = mDragView.mItem;
            if (item != null) {
                item.mListItemView.setVisibility(View.VISIBLE);
                if (item.itemType == DragItem.TYPE_APPLICATION) {
                    mSideView.getAppListAdapter().moveItemPostion((ResolveInfoGroup) item.sidebarItem, item.viewIndex);
                } else if (item.itemType == DragItem.TYPE_SHORTCUT) {
                    mSideView.getContactListAdapter().moveItemPostion((ContactItem) item.sidebarItem, item.viewIndex);
                }
            }
            mDragging = false;
            mDragDeleting = false;
            mDragDroping = false;
            UninstallAction.dismissDialog();
            View view = mDragView.mView;
            if (view != null) {
                view.setVisibility(View.GONE);
                removeView(view);
            }
            resetSidebarWindow();
        }
        if (mSideView != null) {
            mSideView.restoreView();
        }
    }

    public void show(boolean show){
        if(show){
            setVisibility(View.VISIBLE);
            mSideView.showAnimWhenSplitWindow();
        }else{
            setVisibility(View.GONE);
            onDismiss();
        }
    }

    public boolean mEnableUninstallAnim = false;

    @Override
    protected int getChildDrawingOrder(int childCount, int index) {
        if (mEnableUninstallAnim) {
            int count = getChildCount();
            View view = getChildAt(index);
            int id = view.getId();
            if (id == R.id.trash_foreground) {
                return (count - 1);
            } else if (id == -1) {
                return (count - 2);
            }
        }
        return super.getChildDrawingOrder(childCount, index);
    }
}