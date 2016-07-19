package com.smartisanos.sidebar.util.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

import com.smartisanos.sidebar.util.LOG;

import java.util.ArrayList;
import java.util.List;

public class AnimTimeLine {
    private static final LOG log = LOG.getInstance(AnimTimeLine.class);
    private static final boolean DBG_ANIM = true;
    static {
        if (!DBG_ANIM) {log.close();}
    }

    private List<Anim> mAnimList = new ArrayList<Anim>();
    private AnimatorSet mAnimationSet = new AnimatorSet();
    private AnimListener mListener;

    public void addAnim(Anim anim) {
        if (anim == null) {
            return;
        }
        mAnimList.add(anim);
    }

    public List<Anim> getAnimList() {
        return mAnimList;
    }

    public void start() {
        if (mAnimList == null || mAnimList.size() == 0) {
            return;
        }
        AnimatorSetListener listener = new AnimatorSetListener();
        List<Animator> animators = new ArrayList<Animator>();
        for (Anim anim : mAnimList) {
            List<ObjectAnimator> list = anim.getAnimatorList();
            if (list != null && list.size() > 0) {
                animators.addAll(list);
            }
        }
        mAnimationSet.playTogether(animators);
        mAnimationSet.addListener(listener);
        mAnimationSet.start();
    }

    public void stop() {
        mAnimationSet.end();
    }

    public void cancel() {
        mAnimationSet.cancel();
    }

    public boolean started() {
        return mAnimationSet.isStarted();
    }

    public boolean isRunning() {
        return mAnimationSet.isRunning();
    }

    public void addAnimatorListener(Animator.AnimatorListener listener) {
        if (listener == null) {
            return;
        }
        mAnimationSet.addListener(listener);
    }

    public void setAnimListener(AnimListener listener) {
        mListener = listener;
    }

    private class AnimatorSetListener implements Animator.AnimatorListener {
        private long startTime;
        private long endTime;

        @Override
        public void onAnimationStart(Animator animator) {
            startTime = System.currentTimeMillis();
            if (mListener != null) {
                mListener.onStart();
            }
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            endTime = System.currentTimeMillis();
//            log.error("anim spend time ["+(endTime - startTime)+"]");
            if (mListener != null) {
                mListener.onComplete(Anim.ANIM_FINISH_TYPE_COMPLETE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            if (mListener != null) {
                mListener.onComplete(Anim.ANIM_FINISH_TYPE_CANCELED);
            }
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}