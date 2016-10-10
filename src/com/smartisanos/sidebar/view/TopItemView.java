package com.smartisanos.sidebar.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.anim.Anim;
import com.smartisanos.sidebar.util.anim.AnimListener;
import com.smartisanos.sidebar.util.anim.AnimTimeLine;
import com.smartisanos.sidebar.util.anim.Vector3f;

public class TopItemView extends FrameLayout implements ITopItem {

    private static final int ANIM_DURA = 300;

    private View mContentGroup;
    private TextView mTextView;
    private ImageView mIcon;
    private View mBackDimView;
    private View mTopDimView;
    private boolean mDim = false;

    private int mTextResId, mIconResId, mIconDimResId;

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

    public void setText(int resId) {
        mTextResId = resId;
        mTextView.setText(mTextResId);
    }

    public void setIconBackground(int resId, int dimResId) {
        mIconResId = resId;
        mIconDimResId = dimResId;
        mIcon.setBackgroundResource(mIconResId);
    }

    public AnimTimeLine dim(){
        Anim anim = new Anim(mTopDimView, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));
        AnimTimeLine timeLine = new AnimTimeLine();
        timeLine.addAnim(anim);
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                setClickable(false);
                mTopDimView.setVisibility(View.VISIBLE);
                mBackDimView.setVisibility(View.GONE);
                mDim = true;
                updateView();
            }
            @Override
            public void onComplete(int type) {
            }
        });
        return timeLine;
    }

    public AnimTimeLine highlight() {
        AnimTimeLine timeLine = new AnimTimeLine();
        Anim scaleAnim = new Anim(mContentGroup, Anim.SCALE, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(1, 1), new Vector3f(1.05f, 1.05f));
        Anim alphaAnim = new Anim(mBackDimView, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(), new Vector3f(0, 0, 1));

        timeLine.addAnim(scaleAnim);
        timeLine.addAnim(alphaAnim);
        timeLine.setAnimListener(new AnimListener() {
            @Override
            public void onStart() {
                mTopDimView.setVisibility(View.GONE);
                mBackDimView.setVisibility(View.VISIBLE);
                mDim = false;
                updateView();
            }
            @Override
            public void onComplete(int type) {
            }
        });
        return timeLine;
    }

    public AnimTimeLine resume() {
        AnimTimeLine timeLine = new AnimTimeLine();
        if (mBackDimView.getVisibility() != View.GONE) {
            Anim scaleAnim = new Anim(mContentGroup, Anim.SCALE, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(1.05f, 1.05f), new Vector3f(1, 1));
            Anim alphaAnim = new Anim(mBackDimView, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            AnimTimeLine removeHighLightAnim = new AnimTimeLine();
            removeHighLightAnim.addAnim(scaleAnim);
            removeHighLightAnim.addAnim(alphaAnim);
            removeHighLightAnim.setAnimListener(new AnimListener() {
                @Override
                public void onStart() {
                    setClickable(true);
                    mDim = false;
                    updateView();
                }

                @Override
                public void onComplete(int type) {
                    mContentGroup.setScaleX(1);
                    mContentGroup.setScaleY(1);
                    mBackDimView.setVisibility(View.GONE);
                }
            });
            timeLine.addTimeLine(removeHighLightAnim);
        }
        if (mTopDimView.getVisibility() != View.GONE) {
            Anim alphaAnim = new Anim(mTopDimView, Anim.TRANSPARENT, ANIM_DURA, Anim.CUBIC_OUT, new Vector3f(0, 0, 1), new Vector3f());
            alphaAnim.setListener(new AnimListener() {
                @Override
                public void onStart() {
                    setClickable(true);
                    mDim = false;
                    updateView();
                }
                @Override
                public void onComplete(int type) {
                    mTopDimView.setVisibility(View.GONE);
                }
            });
            timeLine.addAnim(alphaAnim);
        }
        return timeLine;
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

    private void updateView() {
        if (mTextResId != 0) {
            mTextView.setText(mTextResId);
        }
        if (!mDim) {
            mIcon.setBackgroundResource(mIconResId);
            mTextView.setTextColor(mContext.getResources().getColor(R.color.topbar_text_color));
        } else {
            mIcon.setBackgroundResource(mIconDimResId);
            mTextView.setTextColor(mContext.getResources().getColor(R.color.topbar_text_dim_color));
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        updateView();
    }
}
