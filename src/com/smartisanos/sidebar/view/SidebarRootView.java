package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.action.UninstallAction;
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.SidebarItem;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimInterpolator;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.R;

public class SidebarRootView extends FrameLayout {

    private static final LOG log = LOG.getInstance(SidebarRootView.class);

    private Context mContext;
    private DragView mDragView;
    private SideView mSideView;
    private int sidebarWidth;

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
        sidebarWidth = mContext.getResources().getDimensionPixelSize(R.dimen.sidebar_width);
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

        private Drawable mIcon;
        private DragItem mItem;

        public int iconWidth;
        public int iconHeight;

        public View mView;
        public ImageView mDragViewIcon;
        public TextView mBubbleText;

        private boolean mAlreadyInit = false;

        public DragView(Context context, DragItem item, int[] loc) {
            mView = LayoutInflater.from(context).inflate(R.layout.drag_view, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mView.setLayoutParams(params);
            mItem = item;
            mIcon = item.iconOrig;
            mDragViewIcon = (ImageView) mView.findViewById(R.id.drag_view_icon);
            mDragViewIcon.setBackground(mIcon);
            mBubbleText = (TextView) mView.findViewById(R.id.drag_view_bubble_text);
            mBubbleText.setText(mItem.displayName);

            mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Anim bubbleAlpha = new Anim(mBubbleText, Anim.TRANSPARENT, 100, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
                    bubbleAlpha.start();
                    iconWidth = mDragViewIcon.getWidth();
                    iconHeight = mDragViewIcon.getHeight();
                    mAlreadyInit = true;
                    int[] screenLoc = new int[2];
                    mItem.mListItemView.getLocationOnScreen(screenLoc);
                    int realIconWidth = mItem.mListItemView.getWidth();
                    int realIconHeight = mItem.mListItemView.getHeight();
                    int deltaX = mView.getWidth() / 2 - realIconWidth / 2;
                    int deltaY = mDragViewIcon.getHeight() / 2 - realIconHeight / 2;
                    int x = screenLoc[0] - deltaX;
                    int y = screenLoc[1] - mBubbleText.getHeight() - deltaY;
                    log.error("drag view icon size ("+iconWidth+", "+iconHeight+")");
                    log.error("showView screen loc ["+screenLoc[0]+", "+screenLoc[1]+"] deltaX["+deltaX+"] xy ["+x+", "+y+"]");
                    mView.setTranslationX(x);
                    mView.setTranslationY(y);
                    mView.setVisibility(View.VISIBLE);
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

        public void showView() {
        }
    }

    private class ShowDragViewWhenRelayout implements ViewTreeObserver.OnGlobalLayoutListener {
        private DragItem mItem;
        private int[] mLoc;

        public ShowDragViewWhenRelayout(DragItem item, int[] loc) {
            mItem = item;
            mLoc = loc;
        }

        @Override
        public void onGlobalLayout() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
            mDragView = new DragView(mContext, mItem, mLoc);
            mDragView.mView.setVisibility(View.INVISIBLE);
            addView(mDragView.mView);
            mDragView.showView();
            mTrash.trashAppearWithAnim();
        }
    }

    private boolean mDragging = false;
    public void startDrag(DragItem item, int[] loc) {
        if(mDragging){
            return ;
        }
        mDragging = true;
        //set sidebar to full screen
        ShowDragViewWhenRelayout showDragViewWhenRelayout = new ShowDragViewWhenRelayout(item, loc);
        getViewTreeObserver().addOnGlobalLayoutListener(showDragViewWhenRelayout);
        SidebarController.getInstance(mContext).updateDragWindow(true);
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

    private class DropAnim extends Animation {
        private DragView mDrag;
        private DragItem item;
        private View view;
        private float scaleTo;
        private int[] moveFrom;
        private int[] moveTo;

        public DropAnim(DragView drag) {
            mDrag = drag;
            item = mDrag.getDragItem();
            view = mDrag.mView;
            moveFrom = new int[2];
            moveTo = new int[2];
            ImageView icon = mDragView.mDragViewIcon;
            icon.getLocationOnScreen(moveFrom);
            log.error("loc from ("+moveFrom[0]+", "+moveFrom[1]+")");
            item.mListItemView.getLocationOnScreen(moveTo);

            if (mDrag.mBubbleText.getVisibility() != View.GONE) {
                mDrag.hideBubble();
            }
            int dragViewSize = mContext.getResources().getDimensionPixelSize(R.dimen.drag_view_icon_size);
            int itemViewSize = mContext.getResources().getDimensionPixelSize(R.dimen.sidebar_list_item_img_size);
            scaleTo = (float) ((1.0 * itemViewSize) / (1.0 * dragViewSize));
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float scale = (1 - scaleTo) * (1 - interpolatedTime) + scaleTo;
            view.setScaleX(scale);
            view.setScaleY(scale);

            int deltaX = moveTo[0] - moveFrom[0];
            int deltaY = moveTo[1] - moveFrom[1];

            int x = (int) (deltaX * interpolatedTime + moveFrom[0]);
            int y = (int) (deltaY * interpolatedTime + moveFrom[1]);
            view.setTranslationX(x);
            view.setTranslationY(y);
//            log.error("applyTransformation XY ("+x+", "+y+")");

            if (interpolatedTime == 1) {
                cancel();
                complete();
            }
        }

        private void complete() {
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
    }

    private boolean mDragDroping = false;
    public void dropDrag() {
        if (!mDragging) {
            return;
        }
        log.error("dropDrag !");
        DropAnim anim = new DropAnim(mDragView);
        anim.setDuration(200);
        mDragView.mView.startAnimation(anim);
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
                if (mDragView != null) {
                    mDragView.move(x, y);
                    mSideView.dragObjectMove(x, y, eventTime);
                    mTrash.dragObjectMoveTo(x, y);
                }
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
            doAnimWhenEnter();
        }else{
            doAnimWhenExit();
        }
    }

    private void doAnimWhenEnter() {
        final View shadowView = mSideView.getShadowLineView();
        if (shadowView != null) {
            shadowView.setAlpha(0);
        }
        boolean isLeft = SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT;
        int delta = sidebarWidth;
        int fromX = isLeft ? -delta : delta;
        int time = 150;
        Anim moveAnim = new Anim(this, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(fromX, 0), new Vector3f());
        Anim alphaAnim = new Anim(this, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
        AnimTimeLine timeLine = new AnimTimeLine();
        timeLine.addAnim(moveAnim);
        timeLine.addAnim(alphaAnim);
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(int type) {
                SidebarRootView.this.setAlpha(1);
                SidebarRootView.this.setTranslationX(0);
                if (shadowView != null) {
                    Anim alphaAnim = new Anim(shadowView, Anim.TRANSPARENT, 50, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
                    alphaAnim.setListener(new AnimListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onComplete(int type) {
                            shadowView.setAlpha(1);
                        }
                    });
                    alphaAnim.start();
                }
            }
        });
        timeLine.setDelay(50);
        timeLine.start();
    }

    private void doAnimWhenExit() {
        final View shadowView = mSideView.getShadowLineView();
        if (shadowView != null) {
            shadowView.setVisibility(INVISIBLE);
        }
        AnimTimeLine timeLine = new AnimTimeLine();
        boolean isLeft = SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT;
        int delta = sidebarWidth;
        int toX = isLeft ? -delta : delta;
        int time = 200;
        Anim moveAnim = new Anim(this, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(toX, 0));
        Anim alphaAnim = new Anim(this, Anim.TRANSPARENT, time, 0, new Vector3f(0, 0, 1), new Vector3f());
        timeLine.addAnim(moveAnim);
        timeLine.addAnim(alphaAnim);
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(int type) {
                SidebarRootView.this.setAlpha(1);
                SidebarRootView.this.setTranslationX(0);
                setVisibility(View.GONE);
                onDismiss();
            }
        });
        timeLine.start();
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