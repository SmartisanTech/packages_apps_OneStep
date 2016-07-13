package com.smartisanos.sidebar.util.anim;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.smartisanos.sidebar.util.IClear;

public class AnimUtils {
    private static final int ANIMATION_DURA = 314;
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

    public static Animation getClearAnimationForContainer(View container, IClear clear){
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(ANIMATION_DURA);
        anim.setAnimationListener(new DismissAnimationListener(container, clear));
        return anim;
    }

    public static Animation getEnterAnimationForContainer(View container){
        Animation scaleAnim = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
        scaleAnim.setDuration(ANIMATION_DURA);
        scaleAnim.setAnimationListener(new ShowAnimationListener(container));
        return scaleAnim;
    }

    public static Animation getExitAnimationForContainer(View container){
        Animation scaleAnim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f);
        scaleAnim.setDuration(ANIMATION_DURA);
        scaleAnim.setAnimationListener(new DismissAnimationListener(container));
        return scaleAnim;
    }

    public static final class ShowAnimationListener implements Animation.AnimationListener{

        private View view;
        public ShowAnimationListener(View view){
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            view.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //NA
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //NA
        }
    }

    public static final class DismissAnimationListener implements Animation.AnimationListener{

        private View view;
        private IClear clear;

        public DismissAnimationListener(View view){
            this.view = view;
        }

        public DismissAnimationListener(View view, IClear clear){
            this.view = view;
            this.clear = clear;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            //NA
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            view.setVisibility(View.INVISIBLE);
            if(clear != null){
                clear.clear();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //NA
        }
    }
}
