package com.smartisanos.sidebar.util.anim;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimUtils {
    private static final int ANIMATION_DURA = 200;
    public static LayoutAnimationController getEnterLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(ANIMATION_DURA);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0);
        return controller;
    }

    public static LayoutAnimationController getExitLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
        anim.setDuration(ANIMATION_DURA);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0);
        return controller;
    }

    public static LayoutAnimationController getClearLayoutAnimationForListView(){
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        anim.setDuration(ANIMATION_DURA / 2);
        anim.setFillAfter(true);
        LayoutAnimationController controller = new LayoutAnimationController(anim, 0.125f);
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        return controller;
    }

    public static Animation getEnterAnimationForContainer(){
        Animation scaleAnim = new ScaleAnimation(1.0f, 1.0f, 0.6f, 1.0f);
        scaleAnim.setInterpolator(new AnimInterpolator.Interpolator(Anim.CUBIC_OUT));
        scaleAnim.setDuration(ANIMATION_DURA);
        return scaleAnim;
    }

    public static void contentViewExitAnim(final View view) {
        if (view == null) {
            return;
        }
        view.setPivotY(0);
        int time = 200;
        Anim scaleAnim = new Anim(view, Anim.SCALE, time, Anim.CUBIC_OUT, new Vector3f(1, 1), new Vector3f(1, 0.6f));
        Anim alphaAnim = new Anim(view, Anim.TRANSPARENT, time, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
        AnimTimeLine timeLine = new AnimTimeLine();
        timeLine.addAnim(scaleAnim);
        timeLine.addAnim(alphaAnim);
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(int type) {
                view.setVisibility(View.INVISIBLE);
                view.setAlpha(1);
                view.setScaleX(1);
                view.setScaleY(1);
            }
        });
        timeLine.start();
    }
}
