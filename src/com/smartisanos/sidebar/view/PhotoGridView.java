package com.smartisanos.sidebar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import com.smartisanos.sidebar.util.LOG;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class PhotoGridView extends GridView {
    private static final LOG log = LOG.getInstance(PhotoGridView.class);

    public PhotoGridView(Context context) {
        super(context);
    }

    public PhotoGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PhotoGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private boolean mEnableTouch = true;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mEnableTouch) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public AnimTimeLine photoAnim(final boolean isShow) {
        mEnableTouch = false;
        Vector3f loc00 = null;
        Vector3f alphaFrom = new Vector3f();
        Vector3f alphaTo = new Vector3f();
        final int time = 200;
        int easeInOut = Anim.CUBIC_OUT;
        final List<Anim> anims = new ArrayList<Anim>();
        int gridViewHeight = getHeight();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            view.setPivotX(0);
            view.setPivotY(0);
            int x = (int) view.getX();
            int y = (int) view.getY();
            if (view.getBottom() >= 0 && view.getTop() <= gridViewHeight) {
            } else {
                continue;
            }
            if (loc00 == null) {
                loc00 = new Vector3f(x, y);
            }
            if (i == 0) {
                loc00.x = x;
                loc00.y = y;
                continue;
            }
            Vector3f from = new Vector3f();
            Vector3f to = new Vector3f();
            int moveDelay = 0;
            int alphaDelay = 0;
            if (isShow) {
                view.setAlpha(0);
                from.x = loc00.x - x;
                from.y = loc00.y - y;
                alphaFrom.z = 0;
                alphaTo.z = 1;
                moveDelay = i * 5;
                alphaDelay = moveDelay + 20;
            } else {
                to.x = loc00.x - x;
                to.y = loc00.y - y;
                alphaFrom.z = 1;
                alphaTo.z = 0;
            }
//            log.error("imageAnim show ["+isShow+"] from "+ from + ", to " + to);
            Anim moveAnim = new Anim(view, Anim.MOVE, time, easeInOut, from, to);
            Anim alphaAnim = new Anim(view, Anim.TRANSPARENT, time, easeInOut, alphaFrom, alphaTo);
            moveAnim.setDelay(moveDelay);
            alphaAnim.setDelay(alphaDelay);
            anims.add(moveAnim);
            anims.add(alphaAnim);
        }
        AnimTimeLine timeLine = new AnimTimeLine();
        for (Anim anim : anims) {
            timeLine.addAnim(anim);
        }
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(int type) {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = getChildAt(i);
                    if (view != null) {
                        view.setTranslationX(0);
                        view.setTranslationY(0);
                    }
                }
                mEnableTouch = true;
            }
        });
        return timeLine;
    }
}