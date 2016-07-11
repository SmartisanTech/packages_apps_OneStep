package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import com.smartisanos.sidebar.util.ContactItem;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.ResolveInfoGroup;
import com.smartisanos.sidebar.util.SidebarItem;
import com.smartisanos.sidebar.util.Utils;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimInterpolator;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.Vector3f;
import com.smartisanos.sidebar.R;

import java.util.ArrayList;
import java.util.List;

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

//        private int bubbleTextWidth;
//        private int bubbleTextHeight;

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
//            final float offsetX = 0;
//            final float offsetY = 0;
//            final float initialScale = 1;
//            int iconSize = context.getResources().getDimensionPixelSize(R.dimen.drag_view_icon_size);
//            Resources resources = context.getResources();
//            final float scaleDps = resources.getDimensionPixelSize(R.dimen.dragViewScale);
//            final float scale = (iconWidth + scaleDps) / iconWidth;

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
        mDragging = false;
        final View view = mDragView.mView;
        Vector3f from = new Vector3f(0, view.getY());
        Vector3f to = new Vector3f(0, mTrash.mWindowHeight);
        Anim anim = new Anim(view, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
                mDragDeleting = true;
            }

            @Override
            public void onComplete() {
                mDragDeleting = false;
                view.setVisibility(View.INVISIBLE);
                removeView(view);
                SidebarRootView rootView = SidebarController.getInstance(mContext).getSidebarRootView();
                rootView.resetSidebarWindow();
            }
        });
        anim.start();
    }

    private boolean mDragDroping = false;
    public void dropDrag(int[] loc) {
        if (!mDragging) {
            return;
        }
        mDragging = false;
        log.error("dropDrag !");
        mDragView.hideBubble();
        final DragItem item = mDragView.getDragItem();
        final View view = mDragView.mView;
        int time = 200;
        AnimInterpolator.Interpolator interpolator = new AnimInterpolator.Interpolator(Anim.CUBIC_OUT);
        List<Animator> anims = new ArrayList<Animator>();
        int[] from = new int[2];
        int[] to = new int[2];
        if (loc == null) {
            view.getLocationOnScreen(from);
        } else {
            from = loc;
        }

        log.error("dropDrag start loc => ("+from[0]+", "+from[1]+")");
        item.mListItemView.getLocationOnScreen(to);
        //drag view move anim
        ObjectAnimator moveAnimX = ObjectAnimator.ofFloat(view, Anim.TRANSLATE_X, from[0], to[0]);
        moveAnimX.setDuration(time);
        moveAnimX.setInterpolator(interpolator);
        anims.add(moveAnimX);
        ObjectAnimator moveAnimY = ObjectAnimator.ofFloat(view, Anim.TRANSLATE_Y, from[1], to[1]);
        moveAnimY.setDuration(time);
        moveAnimY.setInterpolator(interpolator);
        anims.add(moveAnimY);
        //drag view scale anim
        int dragViewSize = mContext.getResources().getDimensionPixelSize(R.dimen.drag_view_icon_size);
        int itemViewSize = mContext.getResources().getDimensionPixelSize(R.dimen.sidebar_list_item_img_size);
        float scale = (float) ((1.0 * itemViewSize) / (1.0 * dragViewSize));
//        log.error("dragViewSize ["+dragViewSize+"], itemViewSize ["+itemViewSize+"], scale size ==> " + scale);
        ObjectAnimator scaleAnimX = ObjectAnimator.ofFloat(view, Anim.SCALE_X, 1, scale);
        scaleAnimX.setDuration(time);
        scaleAnimX.setInterpolator(interpolator);
        anims.add(scaleAnimX);
        ObjectAnimator scaleAnimY = ObjectAnimator.ofFloat(view, Anim.SCALE_Y, 1, scale);
        scaleAnimY.setDuration(time);
        scaleAnimY.setInterpolator(interpolator);
        anims.add(scaleAnimY);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(time);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mDragDroping = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
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
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        set.playTogether(anims);
        set.start();
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
            log.error("resumeSidebar !");
            Utils.resumeSidebar(mContext);
            return true;
        }
        return false;
    }

    private int[] touchLoc = new int[2];

    private void precessTouch(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        touchLoc[0] = x;
        touchLoc[1] = y;
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
                    dropDrag(touchLoc);
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
            case MotionEvent.ACTION_SCROLL : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_SCROLL");
                break;
            }
        }
    }
}