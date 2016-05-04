package com.smartisanos.sidebar.view;

import com.smartisanos.sidebar.R;

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

    private static final int ANIM_DURA = 128;

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
            mTopDimView.setVisibility(View.VISIBLE);
        }
    }

    public void highlight() {
        if (!mIsHighlight) {
            mIsHighlight = true;
            Animation anim = new ScaleAnimation(1.0f, 1.05f, 1.0f, 1.05f, 0.5f,0.5f);
            anim.setDuration(ANIM_DURA);
            anim.setFillAfter(true);
            mContentGroup.startAnimation(anim);
            mBackDimView.setVisibility(View.VISIBLE);
        }
    }

    public void resume() {
        if (mIsHighlight) {
            mIsHighlight = false;
            Animation anim = new ScaleAnimation(1.05f, 1.0f, 1.05f, 1.0f, 0.5f,0.5f);
            anim.setDuration(ANIM_DURA);
            anim.setFillAfter(true);
            mContentGroup.startAnimation(anim);
            mBackDimView.setVisibility(View.GONE);
        }
        if(mIsDim){
            mIsDim = false;
            setClickable(true);
            mTopDimView.setVisibility(View.GONE);
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
