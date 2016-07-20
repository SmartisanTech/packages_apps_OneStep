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
        anim.setAnimCallbackListener();
        mAnimList.add(anim);
    }

    public void addTimeLine(AnimTimeLine timeLine) {
        if (timeLine == null) {
            return;
        }
        List<Anim> animList = timeLine.getAnimList();
        if (animList != null) {
            timeLine.setAnimCallbackListener();
            mAnimList.addAll(animList);
        }
    }

    public List<Anim> getAnimList() {
        return mAnimList;
    }

    public void setAnimCallbackListener() {
        if (mAnimList == null || mAnimList.size() == 0) {
            return;
        }
        if (mListener == null) {
            return;
        }
        AnimatorCallbackListener listener = new AnimatorCallbackListener(mListener);
        ObjectAnimator lastAnim = null;
        long totalTime = 0;
        for (Anim anim : mAnimList) {
            List<ObjectAnimator> list = anim.getAnimatorList();
            if (list == null) {
                continue;
            }
            for (ObjectAnimator animator : list) {
                long delta = animator.getDuration() + animator.getStartDelay();
                if (delta >= totalTime) {
                    totalTime = delta;
                    lastAnim = animator;
                }
            }
        }
        if (lastAnim != null) {
            lastAnim.addListener(listener);
        } else {
            log.error("lose last anim, can't set anim listener to time line");
        }
    }

    public void start() {
        if (mAnimList == null || mAnimList.size() == 0) {
            return;
        }
        List<Animator> animators = new ArrayList<Animator>();
        for (Anim anim : mAnimList) {
            List<ObjectAnimator> list = anim.getAnimatorList();
            if (list != null && list.size() > 0) {
                animators.addAll(list);
            }
        }
        setAnimCallbackListener();
        mAnimationSet.playTogether(animators);
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

    public void setAnimListener(AnimListener listener) {
        if (listener == null) {
            return;
        }
        mListener = listener;
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
                mAnimListener.onComplete(Anim.ANIM_FINISH_TYPE_COMPLETE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            if (mAnimListener != null) {
                mAnimListener.onComplete(Anim.ANIM_FINISH_TYPE_CANCELED);
            }
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}