package com.smartisanos.sidebar.view;

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
import android.widget.TextView;

import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimStatusManager;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.R;

public class SidebarRootView extends FrameLayout {

    private static final LOG log = LOG.getInstance(SidebarRootView.class);

    private Context mContext;
    private DragView mDragView;
    private SideView mSideView;
    private int sidebarWidth;
    private Trash mTrash;

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

    @Override
    protected void onFinishInflate() {
        mSideView = (SideView) findViewById(R.id.sidebar);
        mTrash = new Trash(mContext, (ImageView) findViewById(R.id.trash_with_shadow), (ImageView) findViewById(R.id.trash_foreground));
    }

    public Trash getTrash() {
        return mTrash;
    }

    public class DragView {
        public View mListViewItem;
        private Drawable mIcon;

        public final View mView;
        public final ImageView mDragViewIcon;
        public final TextView mBubbleText;

        public DragView(Context context, Drawable icon, View view, int[] loc) {
            mView = LayoutInflater.from(context).inflate(R.layout.drag_view, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mView.setLayoutParams(params);
            mIcon = icon;
            mListViewItem = view;
            mDragViewIcon = (ImageView) mView.findViewById(R.id.drag_view_icon);
            mDragViewIcon.setBackground(mIcon);
            mBubbleText = (TextView) mView.findViewById(R.id.drag_view_bubble_text);
            mBubbleText.setText(getDisplayName());

            mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Anim bubbleAlpha = new Anim(mBubbleText, Anim.TRANSPARENT, 100, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
                    bubbleAlpha.start();
                    int[] screenLoc = new int[2];
                    mListViewItem.getLocationOnScreen(screenLoc);
                    int realIconWidth = mListViewItem.getWidth();
                    int realIconHeight = mListViewItem.getHeight();
                    int deltaX = mView.getWidth() / 2 - realIconWidth / 2;
                    int deltaY = mDragViewIcon.getHeight() / 2 - realIconHeight / 2;
                    int x = screenLoc[0] - deltaX;
                    int y = screenLoc[1] - mBubbleText.getHeight() - deltaY;
                    mView.setTranslationX(x);
                    mView.setTranslationY(y);
                    mView.setVisibility(View.VISIBLE);
                }
            });
        }

        public CharSequence getDisplayName() {
            return mSideView.getDraggedListView().getDraggedItem().getDisplayName();
        }

        public void delelte(){
            mSideView.getDraggedListView().deleteDraggedSidebarItem();
        }

        public void backToPostion() {
            mListViewItem.setVisibility(View.VISIBLE);
            mSideView.getDraggedListView().dropBackSidebarItem();
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

        public void move(float touchX, float touchY) {
            if (mView.getVisibility() != View.VISIBLE) {
                return;
            }
            int x = (int) (touchX - mView.getWidth() / 2);
            int y = (int) (touchY - mDragViewIcon.getHeight() / 2 - mBubbleText.getHeight());
            mView.setTranslationX(x);
            mView.setTranslationY(y);
        }

        public void showView() {
            mView.setVisibility(View.INVISIBLE);
            addView(mView);
        }

        public void removeView() {
            mView.setVisibility(View.GONE);
            SidebarRootView.this.removeView(mView);
        }
    }

    private class ShowDragViewWhenRelayout implements ViewTreeObserver.OnGlobalLayoutListener {
        private Drawable iconOrig;
        public View mListViewItem;
        private int[] mLoc;

        public ShowDragViewWhenRelayout(Drawable icon, View view, int[] loc) {
            iconOrig = icon;
            mListViewItem = view;
            mLoc = loc;
        }

        @Override
        public void onGlobalLayout() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
            mDragging = true;
            mDragView = new DragView(mContext, iconOrig, mListViewItem, mLoc);
            mDragView.showView();
            mTrash.trashAppearWithAnim();
            mShowDragViewWhenRelayout = null;
        }
    }

    private boolean mDragging = false;
    private ShowDragViewWhenRelayout mShowDragViewWhenRelayout;
    public void startDrag(Drawable icon, View view, int[] loc) {
        if(mDragging || mShowDragViewWhenRelayout != null){
            return ;
        }
        mShowDragViewWhenRelayout = new ShowDragViewWhenRelayout(icon, view, loc);
        getViewTreeObserver().addOnGlobalLayoutListener(mShowDragViewWhenRelayout);
        //set sidebar to full screen
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
        requestLayout();
        View view = mDragView.mView;
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
                mDragging = false;
                mDragView.removeView();
                setChildrenDrawingOrderEnabled(false);
                mTrash.trashDisappearWithAnim(new Runnable() {
                    @Override
                    public void run() {
                        SidebarController.getInstance(mContext).updateDragWindow(false);
                    }
                });
            }
        });
        anim.start();
    }

    private class DropAnim extends Animation {
        private DragView mDrag;
        private View view;
        private float scaleTo;
        private int[] moveFrom;
        private int[] moveTo;

        public DropAnim(DragView drag) {
            mDrag = drag;
            mDrag.hideBubble();
            view = mDrag.mView;
            moveFrom = new int[2];
            moveTo = new int[2];
            ImageView icon = mDragView.mDragViewIcon;
            icon.getLocationOnScreen(moveFrom);
            log.error("loc from ("+moveFrom[0]+", "+moveFrom[1]+")");
            mDragView.mListViewItem.getLocationOnScreen(moveTo);
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
            if (interpolatedTime == 1) {
                cancel();
                complete();
            }
        }

        private void complete() {
            mDragging = false;
            mDragDroping = false;
            mDragView.backToPostion();
            mDragView.removeView();
            SidebarController.getInstance(mContext).updateDragWindow(false);
        }
    }

    private boolean mDragDroping = false;
    public void dropDrag() {
        if (!mDragging) {
            return;
        }
        if (mDragView == null) {
            log.error("dropDrag return by mDragView is null");
            return;
        }
        mDragDroping = true;
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
                if (mTrash.dragObjectUpOnUp(x, y, mDragView)) {
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

    //force stop drag !
    public void stopDrag() {
        if (mDragging) {
            mDragging = false;
            mDragDeleting = false;
            mDragDroping = false;
            mDragView.backToPostion();
            mDragView.removeView();
            mTrash.dismissDialog();
            SidebarController.getInstance(mContext).updateDragWindow(false);
        }
    }

    public void show(boolean show, final int bgMode){
        if(show){
            if (mExitAnimTimeLine != null) {
                log.error("mExitAnimTimeLine not null");
                mExitAnimTimeLine.cancel();
            }
            setAlpha(0);
            setVisibility(View.VISIBLE);
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    observer.removeOnGlobalLayoutListener(this);
                    doAnimWhenEnter(bgMode);
                }
            });
        }else{
            if (mEnterAnimTimeLine != null) {
                mEnterAnimTimeLine.cancel();
            }
            if (mSideView != null) {
                mSideView.setBgMode(false);
            }
            doAnimWhenExit();
        }
    }

    private AnimTimeLine mEnterAnimTimeLine = null;

    private void doAnimWhenEnter(final int bgMode) {
        final View shadowView = mSideView.getShadowLineView();
        if (shadowView != null) {
            shadowView.setAlpha(0);
        }
        boolean isLeft = SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT;
        int delta = sidebarWidth;
        int fromX = isLeft ? -delta : delta;
        int time = 150;
        Vector3f alphaFrom = new Vector3f();
        Vector3f alphaTo = new Vector3f(0, 0, 1);
        Anim moveAnim = new Anim(this, Anim.MOVE, time, 0, new Vector3f(fromX, 0), new Vector3f());
        Anim alphaAnim = new Anim(this, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, alphaFrom, alphaTo);

        mSideView.getDarkBgView().setVisibility(View.INVISIBLE);
        mSideView.getAddAndExitDarkBg().setVisibility(View.INVISIBLE);
        Anim showShadowBg = new Anim(mSideView.getDarkBgView(), Anim.TRANSPARENT, 200, Anim.CUBIC_OUT, alphaFrom, alphaTo);
        showShadowBg.setListener(new AnimListener() {
            @Override
            public void onStart() {
                mSideView.setBgMode(bgMode == SidebarController.BG_MODE_DARK);
                mSideView.getDarkBgView().setVisibility(View.VISIBLE);
                mSideView.getAddAndExitDarkBg().setVisibility(View.VISIBLE);
            }

            @Override
            public void onComplete(int type) {
                mSideView.getDarkBgView().setAlpha(1);
                mSideView.getAddAndExitDarkBg().setAlpha(1);
            }
        });
        showShadowBg.setDelay(100);
        showShadowBg.start();

        mEnterAnimTimeLine = new AnimTimeLine();
        mEnterAnimTimeLine.addAnim(moveAnim);
        mEnterAnimTimeLine.addAnim(alphaAnim);
        mEnterAnimTimeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_SIDE_VIEW_ENTER, true);
            }

            @Override
            public void onComplete(int type) {
                if (mEnterAnimTimeLine != null) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_SIDE_VIEW_ENTER, false);
                    SidebarRootView.this.setBackgroundResource(R.color.sidebar_root_background);
                    mSideView.setBackgroundResource(R.drawable.background);
                    SidebarRootView.this.setAlpha(1);
                    SidebarRootView.this.setTranslationX(0);
                    if (shadowView != null) {
                        if (type != Anim.ANIM_FINISH_TYPE_CANCELED) {
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
                        } else {
                            shadowView.setAlpha(1);
                        }
                    }
                    mEnterAnimTimeLine = null;
                }
            }
        });
        mEnterAnimTimeLine.start();
    }

    private AnimTimeLine mExitAnimTimeLine = null;

    private void doAnimWhenExit() {
        SidebarRootView.this.setBackgroundResource(android.R.color.transparent);
        mSideView.setBackgroundResource(android.R.color.transparent);
        final View shadowView = mSideView.getShadowLineView();
        if (shadowView != null) {
            shadowView.setVisibility(INVISIBLE);
        }
        mExitAnimTimeLine = new AnimTimeLine();
        boolean isLeft = SidebarController.getInstance(mContext).getSidebarMode() == SidebarMode.MODE_LEFT;
        int delta = sidebarWidth;
        int toX = isLeft ? -delta : delta;
        int time = 200;
        Anim moveAnim = new Anim(this, Anim.MOVE, time, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(toX, 0));
        Anim alphaAnim = new Anim(this, Anim.TRANSPARENT, time, 0, new Vector3f(0, 0, 1), new Vector3f());
        mExitAnimTimeLine.addAnim(moveAnim);
        mExitAnimTimeLine.addAnim(alphaAnim);
        mExitAnimTimeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_SIDE_VIEW_EXIT, true);
            }

            @Override
            public void onComplete(int type) {
                if (mExitAnimTimeLine != null) {
                    AnimStatusManager.getInstance().setStatus(AnimStatusManager.ON_SIDE_VIEW_EXIT, false);
                    shadowView.setVisibility(VISIBLE);
                    SidebarRootView.this.setAlpha(1);
                    SidebarRootView.this.setTranslationX(0);
                    setVisibility(View.GONE);
                    stopDrag();
                    if (mSideView != null) {
                        mSideView.restoreView();
                    }
                    mExitAnimTimeLine = null;
                }
            }
        });
        mExitAnimTimeLine.start();
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int index) {
        int id = getChildAt(index).getId();
        if (index < childCount - 2) {
            return index;
        }
        if (id == R.id.trash_foreground) { // the most up view
            return (childCount - 1);
        } else {// dragview
            return (childCount - 2);
        }
    }
}