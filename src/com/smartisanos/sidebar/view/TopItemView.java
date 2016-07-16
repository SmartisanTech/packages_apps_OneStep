package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimInterpolator;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class TopItemView extends FrameLayout {

    private static final int ANIM_DURA = 300;

    private View mContentGroup;
    private TextView mTextView;
    private ImageView mIcon;
    private View mBackDimView;
    private View mTopDimView;

    private boolean mIsDim = false;
    private boolean mIsHighlight = false;

    private int mIconContentPaddingTop = 0;

    public TopItemView(Context context) {
        this(context, null);
    }

    public TopItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TopItemView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopItemView);
        mIconContentPaddingTop = ta.getDimensionPixelSize(R.styleable.TopItemView_icon_content_paddingTop, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentGroup = findViewById(R.id.content_group);
        mTextView = (TextView) findViewById(R.id.text);
        mIcon = (ImageView) findViewById(R.id.icon);
        mBackDimView = findViewById(R.id.back_dim_view);
        mTopDimView = findViewById(R.id.top_dim_view);
    }

    public void setText(int resid) {
        mTextView.setText(resid);
    }

    public void setText(CharSequence cs) {
        mTextView.setText(cs);
    }

    public void setIconBackground(int resId) {
        mIcon.setBackgroundResource(resId);
    }

    public void dim(){
        if(!mIsDim){
            mIsDim = true;
            setClickable(false);
            Anim alphaAnim = new Anim(mTopDimView, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
            alphaAnim.setListener(new AnimListener() {
                @Override
                public void onStart() {
                    mTopDimView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onComplete() {
                }
            });
            alphaAnim.start();
        }
    }

    public void highlight() {
        if (!mIsHighlight) {
            mIsHighlight = true;
            Anim scaleAnim = new Anim(mContentGroup, Anim.SCALE, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(1, 1), new Vector3f(1.05f, 1.05f));
            Anim alphaAnim = new Anim(mBackDimView, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
            AnimTimeLine timeLine = new AnimTimeLine();
            timeLine.addAnim(scaleAnim);
            timeLine.addAnim(alphaAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                    mBackDimView.setVisibility(View.VISIBLE);
                }
                @Override
                public void onComplete() {
                }
            });
            timeLine.start();
        }
    }

    public void resume() {
        if (mIsHighlight) {
            mIsHighlight = false;
            Anim scaleAnim = new Anim(mContentGroup, Anim.SCALE, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(1.05f, 1.05f), new Vector3f(1, 1));
            Anim alphaAnim = new Anim(mBackDimView, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            AnimTimeLine timeLine = new AnimTimeLine();
            timeLine.addAnim(scaleAnim);
            timeLine.addAnim(alphaAnim);
            timeLine.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete() {
                    mBackDimView.setVisibility(View.GONE);
                }
            });
            timeLine.start();
        }
        if(mIsDim){
            mIsDim = false;
            setClickable(true);
            Anim alphaAnim = new Anim(mTopDimView, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            alphaAnim.setListener(new AnimListener() {
                @Override
                public void onStart() {
                }
                @Override
                public void onComplete() {
                    mTopDimView.setVisibility(View.GONE);
                }
            });
            alphaAnim.start();
        }
    }

    public void setIconContentPaddingTop(int padding){
        mIconContentPaddingTop = padding;
    }

    /**
     * should ensure pass the corrent formatted bitmap
     */
    public void setIconContent(Bitmap bitmap){
        Matrix matrix = mIcon.getMatrix();
        matrix.postTranslate((mIcon.getWidth() - bitmap.getWidth()) / 2, mIconContentPaddingTop);
        mIcon.setImageMatrix(matrix);
        mIcon.setImageBitmap(bitmap);
    }

    /**
     * should ensure pass the corrent formatted bitmap
     */
    public void setIconContent(Drawable drawable){
        Matrix matrix = mIcon.getMatrix();
        matrix.postTranslate((mIcon.getWidth() - drawable.getIntrinsicWidth()) / 2, mIconContentPaddingTop);
        mIcon.setImageMatrix(matrix);
        mIcon.setImageDrawable(drawable);
    }

    public void resetIconContent(){
        mIcon.setImageDrawable(null);
    }
}
