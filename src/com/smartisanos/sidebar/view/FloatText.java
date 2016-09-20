package com.smartisanos.sidebar.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.LOG;

public class FloatText {
    private static final LOG log = LOG.getInstance(FloatText.class);

    private volatile static FloatText sInstance;

    public static FloatText getInstance(Context context) {
        if (sInstance == null) {
            synchronized (FloatText.class) {
                if (sInstance == null) {
                    sInstance = new FloatText(context);
                }
            }
        }
        return sInstance;
    }

    private Context mContext;
    private View mFloatView;
    private TextView mText;
    private PopupWindow mPopupWindow;
    private int mPaddingWithSidebar;

    private FloatText(Context context) {
        mContext = context;
        mFloatView = LayoutInflater.from(context).inflate(R.layout.float_text_layout, null);
        mText = (TextView) mFloatView.findViewById(R.id.text_content);
        mPopupWindow = new PopupWindow(mFloatView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPopupWindow.setTouchable(false);
        mPopupWindow.setFocusable(false);
        mPaddingWithSidebar = mContext.getResources().getDimensionPixelSize(R.dimen.float_text_padding_with_sidebar);
    }

    public void start() {
        mFloatView.setVisibility(View.INVISIBLE);
        mPopupWindow.showAtLocation(SidebarController.getInstance(mContext).getSideView(), Gravity.NO_GRAVITY, 0, 0);
    }

    public void end() {
        mPopupWindow.dismiss();
    }

    public void show(View view, CharSequence text) {
        mText.setText(text);
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mText.measure(spec, spec);
        int textWidth = mText.getMeasuredWidth();
        int textHeight = mText.getMeasuredHeight();
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        int xOffset = 0;
        int yOffset = loc[1] + (viewHeight - textHeight) / 2;
        if (SidebarController.getInstance(view.getContext()).getSidebarMode() == SidebarMode.MODE_LEFT) {
            xOffset = loc[0] + viewWidth + mPaddingWithSidebar;
        } else {
            xOffset = loc[0] - textWidth - mPaddingWithSidebar;
        }
        mText.setX(xOffset);
        mText.setY(yOffset);
        mFloatView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mFloatView.setVisibility(View.INVISIBLE);
    }
}