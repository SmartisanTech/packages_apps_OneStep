package com.smartisanos.sidebar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ToAddResolveInfoGridView extends GridView {

    public ToAddResolveInfoGridView(Context context) {
        super(context, null);
    }

    public ToAddResolveInfoGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToAddResolveInfoGridView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ToAddResolveInfoGridView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
        getLayoutParams().height = getMeasuredHeight();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setAdapter(new ToAddResolveInfoGroupAdapter(mContext));
    }
}
