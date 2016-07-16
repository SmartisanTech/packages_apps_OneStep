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

    private List<Animator> mAnimList = new ArrayList<Animator>();
    private AnimatorSet mAnimationSet = new AnimatorSet();
    private AnimListener mListener;

    public void addAnim(Anim anim) {
        if (anim == null) {
            return;
        }
        List<ObjectAnimator> list = anim.getAnimList();
        if (list != null) {
            mAnimList.addAll(list);
        }
    }

    public void start() {
        if (mAnimList == null || mAnimList.size() == 0) {
            return;
        }
        AnimatorSetListener listener = new AnimatorSetListener();
        mAnimationSet.playTogether(mAnimList);
        mAnimationSet.addListener(listener);
        mAnimationSet.start();
    }

    public void stop() {
        mAnimationSet.end();
    }

    public boolean started() {
        return mAnimationSet.isStarted();
    }

    public boolean isRunning() {
        return mAnimationSet.isRunning();
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
            if (mListener != null) {
                mListener.onComplete();
            }
//            log.error("anim spend time ["+(endTime - startTime)+"]");
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}