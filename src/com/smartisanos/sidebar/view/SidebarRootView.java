package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
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
import com.smartisanos.sidebar.view.ContentView.ContentType;
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
            final float offsetX = 0;
            final float offsetY = 0;
            final float initialScale = 1;
            int iconSize = context.getResources().getDimensionPixelSize(R.dimen.drag_view_icon_size);

            Resources resources = context.getResources();
            final float scaleDps = resources.getDimensionPixelSize(R.dimen.dragViewScale);
            final float scale = (iconWidth + scaleDps) / iconWidth;

            mDragViewIcon = (ImageView) mView.findViewById(R.id.drag_view_icon);
            mDragViewIcon.setBackground(mIcon);
            mBubbleText = (TextView) mView.findViewById(R.id.drag_view_bubble_text);
            mBubbleText.setText(mItem.displayName);
            initLoc = loc;
            log.error("init loc ==> ("+initLoc[0]+", "+initLoc[1]+")");

            mInitialScale = initialScale;
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

    public void startDrag(DragItem item, int[] loc) {
        if (mDragView != null) {
            return;
        }
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

    public void dropDrag(final boolean delete) {
        log.error("dropDrag !");
        if (mDragView == null) {
            return;
        }
        if (mDragView.mView == null) {
            return;
        }
        mDragView.hideBubble();
        final DragItem item = mDragView.getDragItem();
        final View view = mDragView.mView;
        int time = 200;
        AnimInterpolator.Interpolator interpolator = new AnimInterpolator.Interpolator(Anim.CUBIC_OUT);
        List<Animator> anims = new ArrayList<Animator>();
        int[] from = new int[2];
        int[] to = new int[2];
        view.getLocationOnScreen(from);
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

        Anim trashAnim = mTrash.trashDisappearWithAnim();
        if (trashAnim != null) {
            anims.addAll(trashAnim.getAnimList());
        }
        AnimatorSet set = new AnimatorSet();
        set.setDuration(time);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.INVISIBLE);
                removeView(view);
                if (!delete && mSideView != null) {
                    if (item != null) {
                        item.mListItemView.setVisibility(View.VISIBLE);
                        int index = item.viewIndex;
                        if (item.itemType == DragItem.TYPE_APPLICATION) {
                            ResolveInfoGroup data = (ResolveInfoGroup) item.sidebarItem;
                            log.error("A set item to index ["+index+"]");
                            ResolveInfoListAdapter adapter = mSideView.getAppListAdapter();
                            adapter.setItem(index, data);
                            mSideView.notifyAppListDataSetChanged();
                        } else if (item.itemType == DragItem.TYPE_SHORTCUT) {
                            ContactItem contactItem = (ContactItem) item.sidebarItem;
                            log.error("B set item to index ["+index+"]");
                            ContactListAdapter adapter = mSideView.getContactListAdapter();
                            adapter.setItem(index, contactItem);
                            mSideView.notifyContactListDataSetChanged();
                        }
                    }
                }

                SidebarController controller = SidebarController.getInstance(mContext);
                SidebarRootView rootView = controller.getSidebarRootView();
                Trash trash = rootView.getTrash();
                trash.mTrashStatus = Trash.TRASH_HIDE;
                trash.trashDisappearAnimRunning = false;
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
        return mDragView;
    }

    private final boolean ENABLE_TOUCH_LOG = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            if (processResumeSidebar()) {
                return true;
            }
        }

        if (mDragView != null) {
            precessTouch(event);
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mDragView != null
                || SidebarController.getInstance(mContext)
                        .getCurrentContentType() != ContentType.NONE) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    private boolean processResumeSidebar() {
        if (SidebarController.getInstance(mContext).getCurrentContentType() != ContentView.ContentType.NONE) {
            log.error("resumeSidebar !");
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
            case MotionEvent.ACTION_UP : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_UP");
                if (mTrash.dragObjectUpOnUp(x, y)) {
                    //handle uninstall
                } else {
                    dropDrag(false);
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
                if (mTrash.dragObjectUpOnUp(x, y)) {
                    //handle uninstall
                } else {
                    dropDrag(false);
                }
                break;
            }
            case MotionEvent.ACTION_SCROLL : {
                if (ENABLE_TOUCH_LOG) log.error("ACTION_SCROLL");
                break;
            }
        }
    }
}