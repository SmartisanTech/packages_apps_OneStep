package com.smartisanos.sidebar.util.anim;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.View;

import com.smartisanos.sidebar.util.LOG;

import java.util.ArrayList;
import java.util.List;

import android.os.Process;

public class Anim {
    private static final LOG log = LOG.getInstance(Anim.class);
    private static final boolean DBG_ANIM = true;
    static {
        if (!DBG_ANIM) {log.close();}
    }

    public final static int QUAD_IN      = AnimInterpolator.QUAD_IN;
    public final static int QUAD_OUT     = AnimInterpolator.QUAD_OUT;
    public final static int QUAD_IN_OUT  = AnimInterpolator.QUAD_IN_OUT;

    public final static int CIRC_IN      = AnimInterpolator.CIRC_IN;
    public final static int CIRC_OUT     = AnimInterpolator.CIRC_OUT;
    public final static int CIRC_IN_OUT  = AnimInterpolator.CIRC_IN_OUT;

    public final static int CUBIC_IN     = AnimInterpolator.CUBIC_IN;
    public final static int CUBIC_OUT    = AnimInterpolator.CUBIC_OUT;
    public final static int CUBIC_IN_OUT = AnimInterpolator.CUBIC_IN_OUT;

    public final static int QUART_IN     = AnimInterpolator.QUART_IN;
    public final static int QUART_OUT    = AnimInterpolator.QUART_OUT;
    public final static int QUART_IN_OUT = AnimInterpolator.QUART_IN_OUT;

    public final static int QUINT_IN     = AnimInterpolator.QUINT_IN;
    public final static int QUINT_OUT    = AnimInterpolator.QUINT_OUT;
    public final static int QUINT_IN_OUT = AnimInterpolator.QUINT_IN_OUT;


    //Android anim name
    public static final String X           = "x";
    public static final String Y           = "y";
    public static final String TRANSLATE_X = "translationX";
    public static final String TRANSLATE_Y = "translationY";
    public static final String ROTATION    = "rotation";
    public static final String ROTATION_X  = "rotationX";
    public static final String ROTATION_Y  = "rotationY";
    public static final String SCALE_X     = "scaleX";
    public static final String SCALE_Y     = "scaleY";
    public static final String ALPHA       = "alpha";

    public static final int TRANSLATE   = 1001;
    public static final int ROTATE      = 1002;
    public static final int SCALE       = 1003;
    public static final int TRANSPARENT = 1004;
    public static final int MOVE        = 1005;

    public static final int ANIM_FINISH_TYPE_COMPLETE = 1;
    public static final int ANIM_FINISH_TYPE_CANCELED = 2;

    private View mView;
    private int animType;
    private int duration;
    private int mDelay;
    private int mInOut;
    private Vector3f mFrom;
    private Vector3f mTo;

    private AnimListener mListener;
    private AnimatorSet mAnimationSet;

    private List<Animator> mAnimList;

    public Anim(View view, int type, int time, int easeInOut, Vector3f from, Vector3f to) {
        this(view, type, time, 0, easeInOut, from, to);
    }

    public Anim(View view, int type, int time, int delay, int easeInOut, Vector3f from, Vector3f to) {
        if (type != TRANSLATE
                && type != ROTATE
                && type != SCALE
                && type != TRANSPARENT
                && type != MOVE) {
            throw new IllegalArgumentException("error anim type ["+type+"]");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("lose from or to");
        }
        mView = view;
        animType = type;
        duration = time;
        mDelay = delay;
        mInOut = easeInOut;
        mFrom = from;
        mTo = to;
        if (from == null || to == null) {
            throw new IllegalArgumentException("something is null ["+from+"]["+to+"]");
        }
        mAnimList = new ArrayList<Animator>();
        switch (animType) {
            case TRANSLATE : {
                if (from.x != to.x) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(mView, X, from.x, to.x);
                    mAnimList.add(animator);
                }
                if (from.y != to.y) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(mView, Y, from.y, to.y);
                    mAnimList.add(animator);
                }
                break;
            }
            case ROTATE : {
                if (from.z != to.z) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(mView, ROTATION, from.z, to.z);
                    mAnimList.add(animator);
                }
                break;
            }
            case SCALE : {
                if (from.x != to.x) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(mView, SCALE_X, from.x, to.x);
                    mAnimList.add(animator);
                }
                if (from.y != to.y) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(mView, SCALE_Y, from.y, to.y);
                    mAnimList.add(animator);
                }
                break;
            }
            case TRANSPARENT : {
                if (mFrom.z != mTo.z) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(mView, ALPHA, mFrom.z, mTo.z);
                    mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    mAnimList.add(animator);
                }
                break;
            }
            case MOVE : {
                if (from.x != to.x) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(mView, TRANSLATE_X, from.x, to.x);
                    mAnimList.add(animator);
                }
                if (from.y != to.y) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(mView, TRANSLATE_Y, from.y, to.y);
                    mAnimList.add(animator);
                }
                break;
            }
        }
        AnimInterpolator.Interpolator interpolator = null;
        if (mInOut != 0) {
            interpolator = new AnimInterpolator.Interpolator(mInOut);
        }
        for (Animator animator : mAnimList) {
            animator.setDuration(duration);
            if (interpolator != null) {
                animator.setInterpolator(interpolator);
            }
        }
    }

    public Vector3f getFrom() {
        return mFrom;
    }

    public Vector3f getTo() {
        return mTo;
    }

    public int getAnimType() {
        return animType;
    }

    public View getView() {
        return mView;
    }

    public void setDelay(long delay) {
        if (mAnimList == null) {
            return;
        }
        if (delay == 0) {
            return;
        }
        for (Animator animator : mAnimList) {
            animator.setStartDelay(delay);
        }
    }

    public void setAnimCallbackListener() {
        if (mAnimList == null || mAnimList.size() == 0) {
            return;
        }
        if (mListener != null) {
            //need do some callback
            long totalTime = 0;
            Animator lastAnim = null;
            for (Animator animator : mAnimList) {
                long delta = animator.getDuration() + animator.getStartDelay();
                if (delta >= totalTime) {
                    totalTime = delta;
                    lastAnim = animator;
                }
            }
            if (lastAnim != null) {
                AnimatorCallbackListener listener = new AnimatorCallbackListener(mListener);
                lastAnim.addListener(listener);
            } else {
                throw new IllegalArgumentException("set anim listener err !");
            }
        }
    }

    public boolean start() {
        if (mAnimList == null || mAnimList.size() == 0) {
//            throw new IllegalArgumentException("anim size is 0");
            log.error("anim array is empty !");
            LOG.trace();
            return false;
        }
        setAnimCallbackListener();
        mAnimationSet = new AnimatorSet();
        mAnimationSet.playTogether(mAnimList);
        mAnimationSet.start();
        return true;
    }

    public List<ObjectAnimator> getAnimatorList() {
        if (mAnimList == null || mAnimList.size() == 0) {
            return null;
        }
        List<ObjectAnimator> list = new ArrayList<ObjectAnimator>();
        int size = mAnimList.size();
        for (int i = 0; i < size; i++) {
            ObjectAnimator anim = (ObjectAnimator) mAnimList.get(i);
            if (anim != null) {
                list.add(anim);
            }
        }
        return list;
    }

    private class AnimatorCallbackListener implements Animator.AnimatorListener {
        private AnimListener mAnimListener;

        public AnimatorCallbackListener(AnimListener listener) {
            mAnimListener = listener;
        }

        @Override
        public void onAnimationStart(Animator animator) {
            if (mAnimListener != null) {
                mAnimListener.onStart();
            }
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (mAnimListener != null) {
                mAnimListener.onComplete(ANIM_FINISH_TYPE_COMPLETE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            if (mAnimListener != null) {
                mAnimListener.onComplete(ANIM_FINISH_TYPE_CANCELED);
            }
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    public void cancel() {
        if (mAnimationSet == null) {
            return;
        }
        mAnimationSet.cancel();
    }

    public void setListener(AnimListener l) {
        mListener = l;
    }
}