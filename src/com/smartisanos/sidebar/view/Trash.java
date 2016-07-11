package com.smartisanos.sidebar.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.action.UninstallAction;
import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimInterpolator;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.Vector3f;

public class Trash {
    private static final LOG log = LOG.getInstance(Trash.class);

    private Context mContext;
    private FrameLayout mTrashView;

    public SidebarRootView mRootView;

    public int mTrashWidth;
    public int mTrashHeight;
    public int mWindowWidth;
    public int mWindowHeight;
    public int mTrashDisplayHeight;
    public int mTrashFloatUpHeight;
    public int [] trash_react_area = new int[4];
    public int [] trash_uninstall_react_area = new int[4];


    public static final int TRASH_HIDE = 1;
    public static final int TRASH_SHOW = 2;
    public static final int TRASH_FLOAT = 3;

    private int mTrashStatus = TRASH_HIDE;

    public Trash(Context context, FrameLayout trashView) {
        mContext = context;
        mTrashView = trashView;
        Resources resources = mContext.getResources();
        mTrashWidth         = resources.getInteger(R.integer.trash_width);;
        mTrashHeight        = resources.getInteger(R.integer.trash_height);
        mTrashDisplayHeight = resources.getInteger(R.integer.trash_display_height);
        mTrashFloatUpHeight = resources.getInteger(R.integer.trash_float_up_height);
    }

    public boolean inTrashReactArea(float x, float y) {
        if (trash_react_area[0] == 0) {
            return false;
        }
        if (trash_react_area[0] < x && x < trash_react_area[2]) {
            if (y > trash_react_area[1]) {
                return true;
            }
        }
        return false;
    }

    public boolean inTrashUninstallReactArea(float x, float y) {
        if (trash_uninstall_react_area[0] == 0) {
            return false;
        }
        if (trash_uninstall_react_area[0] < x && x < trash_uninstall_react_area[2]) {
            if (y > trash_uninstall_react_area[1]) {
                return true;
            }
        }
        return false;
    }

    public void dragObjectMoveTo(float x, float y) {
        if (inTrashReactArea(x, y)) {
            //in trash area
            trashFloatUpWithAnim(null);
        } else {
            //out trash area
            trashFallDownWithAnim();
        }
    }

    public boolean dragObjectUpOnUp(float x, float y) {
        boolean processUninstall = false;
        log.error("dragObjectUpOnUp ["+x+"], ["+y+"]");
        if (!inTrashUninstallReactArea(x, y)) {
            return processUninstall;
        }
        SidebarRootView.DragView dragView = mRootView.getDraggedView();
        if (dragView == null) {
            log.error("dragObjectUpOnUp return by dragView is null");
            return processUninstall;
        }
        SidebarRootView.DragItem item = dragView.getDragItem();
        if (item == null) {
            log.error("dragObjectUpOnUp return by dragItem is null");
            return processUninstall;
        }
        //move icon to trash
        moveIconToTrash(dragView);
        UninstallAction action = new UninstallAction(mContext, item);
        action.showUninstallDialog();
        processUninstall = true;
        log.error("handle uninstall process");
        return processUninstall;
    }

    public void initTrashView() {
        mTrashView.setVisibility(View.GONE);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int widthPixels;
        int heightPixels;
        if (metrics.heightPixels > metrics.widthPixels) {
            widthPixels = metrics.widthPixels;
            heightPixels = metrics.heightPixels;
        } else {
            widthPixels = metrics.heightPixels;
            heightPixels = metrics.widthPixels;
        }
        mWindowWidth = widthPixels;
        mWindowHeight = heightPixels;

        int trashViewWidth = mTrashView.getWidth();
        if (trashViewWidth == 0) {
            trashViewWidth = mTrashWidth;
        }

        int locX = widthPixels / 2 - trashViewWidth / 2;
        int locY = heightPixels;
        mTrashView.setTranslationX(locX);
        mTrashView.setTranslationY(locY);
        mTrashView.setVisibility(View.VISIBLE);

        trash_react_area = new int[4];
        //left-top, right-bottom
        int left   = mWindowWidth / 2 - mTrashWidth / 2;
        int top    = mWindowHeight - mTrashHeight / 2;
        int right  = mWindowWidth / 2 + mTrashWidth / 2;
        int bottom = mWindowHeight;
        trash_react_area[0] = left;
        trash_react_area[1] = top;
        trash_react_area[2] = right;
        trash_react_area[3] = bottom;

        trash_uninstall_react_area[0] = left;
        trash_uninstall_react_area[1] = mWindowHeight - mTrashHeight;
        trash_uninstall_react_area[2] = right;
        trash_uninstall_react_area[3] = bottom;
    }

    public void hieTrashView() {
        if (mTrashView == null) {
            return;
        }
        mTrashView.setVisibility(View.GONE);
    }

    private boolean trashAnimRunning = false;

    public void trashAppearWithAnim() {
        if (mTrashStatus != TRASH_HIDE) {
            return;
        }
        if (trashAnimRunning) {
            log.error("trashAppearWithAnim return by trashAppearAnimRunning true");
            return;
        }
        mTrashView.setTranslationX(mWindowWidth / 2 - mTrashView.getWidth() / 2);
        int fromY = mWindowHeight;
        int toY = mWindowHeight - mTrashDisplayHeight;
        Vector3f from = new Vector3f(0, fromY);
        Vector3f to = new Vector3f(0, toY);
        Anim anim = new Anim(mTrashView, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
                trashAnimRunning = true;
                int width = mTrashView.getWidth();
                mTrashView.setTranslationX(mWindowWidth / 2 - width / 2);
            }
            @Override
            public void onComplete() {
                trashAnimRunning = false;
                mTrashStatus = TRASH_SHOW;
                log.error("trashAppearWithAnim onComplete");
            }
        });
        anim.start();
    }

    public void trashDisappearWithAnim() {
        if (mTrashStatus == TRASH_HIDE) {
            return;
        }
        if (trashAnimRunning) {
            log.error("trashDisappearWithAnim return by trashDisappearAnimRunning true");
            return;
        }
        Vector3f from = new Vector3f(0, mTrashView.getTranslationY());
        Vector3f to = new Vector3f(0, mWindowHeight);
        Anim anim = new Anim(mTrashView, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
                trashAnimRunning = true;
            }
            @Override
            public void onComplete() {
                trashAnimRunning = false;
                mTrashStatus = TRASH_HIDE;
            }
        });
        anim.start();
    }

    private boolean mTrashUpAnimRunning = false;
    private boolean mTrashDownAnimRunning = false;

    public void trashFloatUpWithAnim(final Runnable runnable) {
        if (mTrashStatus == TRASH_FLOAT) {
            return;
        }
        if (mTrashUpAnimRunning) {
            log.error("trashFloatUpWithAnim return by mTrashUpAnimRunning true");
            return;
        }
        mTrashUpAnimRunning = true;
        int fromY = (int) mTrashView.getY();
        int toY = mWindowHeight - mTrashDisplayHeight - mTrashFloatUpHeight;
        Vector3f from = new Vector3f(0, fromY);
        Vector3f to = new Vector3f(0, toY);
        Anim anim = new Anim(mTrashView, Anim.TRANSLATE, 100, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete() {
                mTrashStatus = TRASH_FLOAT;
                mTrashUpAnimRunning = false;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        anim.start();
    }

    public void trashFallDownWithAnim() {
        if (mTrashStatus != TRASH_FLOAT) {
            return;
        }
        if (mTrashDownAnimRunning) {
            log.error("trashFallDownWithAnim return by mTrashDownAnimRunning true");
            return;
        }
        mTrashDownAnimRunning = true;
        Vector3f from = new Vector3f(0, mTrashView.getY());
        Vector3f to = new Vector3f(0, mWindowHeight - mTrashDisplayHeight);

        Anim anim = new Anim(mTrashView, Anim.TRANSLATE, 100, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete() {
                mTrashDownAnimRunning = false;
                mTrashStatus = TRASH_SHOW;
            }
        });
        anim.start();
    }

    public void moveIconToTrash(final SidebarRootView.DragView dragView) {
        if (dragView == null) {
            return;
        }
        View view = dragView.mView;
        if (view == null) {
            return;
        }

        int[] trashLoc = new int[2];
        mTrashView.getLocationOnScreen(trashLoc);
        log.error("trash loc ==> ("+trashLoc[0]+", "+trashLoc[1]+")");

        dragView.hideBubble();
        float fromX = view.getX();
        float fromY = view.getY();
        int iconWidth = dragView.iconWidth;
        int iconHeight = dragView.iconHeight;
        log.error("moveIconToTrash view width ["+iconWidth+"], view height ["+iconHeight+"]");
        float toX = mWindowWidth / 2 - iconWidth / 2;
        float toY = mWindowHeight - mTrashDisplayHeight - mTrashFloatUpHeight - iconHeight;
        Vector3f from = new Vector3f(fromX, fromY);
        Vector3f to = new Vector3f(toX, toY);
        log.error("moveIconToTrash move from " + from + ", to " + to);

        Anim anim = new Anim(view, Anim.TRANSLATE, 200, Anim.CUBIC_OUT, from, to);
        anim.setListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete() {
                if (dragView == null) {
                    log.error("moveIconToTrash view is null when anim complete");
                    return;
                }
                if (mTrashStatus != TRASH_FLOAT) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            rockOnTrash(dragView.mView);
                        }
                    };
                    trashFloatUpWithAnim(runnable);
                } else {
                    rockOnTrash(dragView.mView);
                }

            }
        });
        anim.start();
    }

    private boolean rockRepeat = false;
    private AnimatorSet mRockAnimSet;

    public void rockOnTrash(View view) {
        if (mRockAnimSet != null) {
            mRockAnimSet = null;
        }
        float rockAngle = 2.0f;
        float offset = 2.0f;
        long interval_time = 70;
        float locX = view.getX();
        float locY = view.getY();
        //init view loc and rotate
        rockRepeat = true;
        view.setTranslationX(locX - offset);
        view.setTranslationY(locY + offset);
        view.setRotation(-rockAngle);

        Vector3f loc = new Vector3f(locX, locY);
        mRockAnimSet = generateRockAnimSet(view, loc);
        RockAnimListener listener = new RockAnimListener(view, loc);
        mRockAnimSet.addListener(listener);
        mRockAnimSet.start();
    }

    public void stopRock() {
        rockRepeat = false;
        if (mRockAnimSet != null) {
            if (mRockAnimSet.isStarted()) {
                if (mRockAnimSet.isRunning()) {
                    mRockAnimSet.end();
                } else {
                    mRockAnimSet.cancel();
                }
            }
        }
        //
    }

    private class RockAnimListener implements Animator.AnimatorListener {

        private View mView;
        private Vector3f mLoc;

        public RockAnimListener(View view, Vector3f loc) {
            mView = view;
            mLoc = loc;
        }

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (rockRepeat) {
                mRockAnimSet = generateRockAnimSet(mView, mLoc);
                RockAnimListener listener = new RockAnimListener(mView, mLoc);
                mRockAnimSet.addListener(listener);
                mRockAnimSet.start();
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    private AnimatorSet generateRockAnimSet(View view, Vector3f loc) {
        float rockAngle = 2.0f;
        float offset = 2.0f;
        long interval_time = 70;
        AnimInterpolator.Interpolator interpolator = new AnimInterpolator.Interpolator(Anim.CIRC_IN_OUT);
        AnimatorSet animSet = new AnimatorSet();

        //step 1
        ObjectAnimator anim1_translateX = ObjectAnimator.ofFloat(view, Anim.X, loc.x - offset, loc.x + offset);
        anim1_translateX.setDuration(interval_time);
        anim1_translateX.setInterpolator(interpolator);

        ObjectAnimator anim1_translateY = ObjectAnimator.ofFloat(view, Anim.Y, loc.y + offset, loc.y - offset);
        anim1_translateY.setDuration(interval_time);
        anim1_translateY.setInterpolator(interpolator);

        animSet.play(anim1_translateX).with(anim1_translateY);

        //step 2
        ObjectAnimator anim2_rotate = ObjectAnimator.ofFloat(view, Anim.ROTATION, -rockAngle, rockAngle);
        anim2_rotate.setDuration(interval_time);
        anim2_rotate.setInterpolator(interpolator);

        ObjectAnimator anim2_translateX = ObjectAnimator.ofFloat(view, Anim.X, loc.x + offset, loc.x + offset);
        anim2_translateX.setDuration(interval_time);
        anim2_translateX.setInterpolator(interpolator);

        ObjectAnimator anim2_translateY = ObjectAnimator.ofFloat(view, Anim.Y, loc.y - offset, loc.y + offset);
        anim2_translateY.setDuration(interval_time);
        anim2_translateY.setInterpolator(interpolator);
        animSet.play(anim2_rotate).with(anim2_translateX).with(anim2_translateY).after(anim1_translateX);

        //step 3
        ObjectAnimator anim3_translateX = ObjectAnimator.ofFloat(view, Anim.X, loc.x + offset, loc.x - offset);
        anim3_translateX.setDuration(interval_time);
        anim3_translateX.setInterpolator(interpolator);

        ObjectAnimator anim3_translateY = ObjectAnimator.ofFloat(view, Anim.Y, loc.y + offset, loc.y - offset);
        anim3_translateY.setDuration(interval_time);
        anim3_translateY.setInterpolator(interpolator);

        animSet.play(anim3_translateX).with(anim3_translateY).after(anim2_rotate);

        //step 4
        ObjectAnimator anim4_rotate = ObjectAnimator.ofFloat(view, Anim.ROTATION, rockAngle, -rockAngle);
        anim4_rotate.setDuration(interval_time);
        anim4_rotate.setInterpolator(interpolator);

        ObjectAnimator anim4_translateX = ObjectAnimator.ofFloat(view, Anim.X, loc.x - offset, loc.x - offset);
        anim4_translateX.setDuration(interval_time);
        anim4_translateX.setInterpolator(interpolator);

        ObjectAnimator anim4_translateY = ObjectAnimator.ofFloat(view, Anim.Y, loc.y - offset, loc.y + offset);
        anim4_translateY.setDuration(interval_time);
        anim4_translateY.setInterpolator(interpolator);

        animSet.play(anim4_rotate).with(anim4_translateX).with(anim4_translateY).after(anim3_translateX);
        return animSet;
    }
}