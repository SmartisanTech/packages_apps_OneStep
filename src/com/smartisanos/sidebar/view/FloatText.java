package com.smartisanos.sidebar.view;

import android.content.Context;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.SidebarController;
import com.smartisanos.sidebar.SidebarMode;
import com.smartisanos.sidebar.util.Constants;
import com.smartisanos.sidebar.util.LOG;

public class FloatText {
    private static final LOG log = LOG.getInstance(FloatText.class);

    private static LinearLayout mFloatView;
    private static TextView mText;

    private static PopupWindow mPopupWindow;

    public static void handleDragEvent(Context context, DragEvent event) {
        if (event == null) {
            return;
        }
        int action = event.getAction();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED : {
                init(context);
                break;
            }
            case DragEvent.ACTION_DRAG_ENDED :
            case DragEvent.ACTION_DROP : {
                clear();
                break;
            }
        }
    }

    private static void init(Context context) {
        if (mPopupWindow != null) {
            log.error("init failed by mPopupWindow is not null");
            return;
        }
//        log.error("init !");
        mFloatView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.float_text_layout, null);
        mFloatView.setVisibility(View.INVISIBLE);
        mText = (TextView) mFloatView.findViewById(R.id.text_content);
        mPopupWindow = new PopupWindow(mFloatView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        mPopupWindow.setTouchable(false);
        mPopupWindow.setFocusable(false);
        SideView sideView = SidebarController.getInstance(context).getSideView();
        mPopupWindow.showAtLocation(sideView, Gravity.NO_GRAVITY, 0, 0);
    }

    private static void clear() {
        if (mPopupWindow == null) {
            return;
        }
//        log.error("clear !");
        mPopupWindow.dismiss();
        mPopupWindow = null;
    }

    private static ViewTreeObserver.OnGlobalLayoutListener mTextViewObs = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mText == null) {
                return;
            }
            mText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            mFloatView.setVisibility(View.VISIBLE);
        }
    };

    public static void show(final Context context, final View view, String text) {
        if (mPopupWindow == null) {
            return;
        }
        if (mText == null) {
            return;
        }
        final int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        mText.setText(text);
        mFloatView.requestLayout();
        mFloatView.invalidate();
        mText.post(new Runnable() {
            @Override
            public void run() {
                if (mText == null) {
                    log.error("show failed by mText is null");
                    return;
                }
                if (mPopupWindow == null) {
                    log.error("show failed by mPopupWindow is null");
                    return;
                }
                int xOffset = loc[0];
                int yOffset = loc[1];
                int viewWidth = view.getWidth();
                int viewHeight = view.getHeight();
                int textWidth = mText.getWidth();
                int maxTextWidth = mText.getMaxWidth();
                if (textWidth > maxTextWidth) {
                    textWidth = maxTextWidth;
                }
                int width = textWidth;
                int height = Constants.FloatTextValue.textBgIntrinsicHeight;
                if (width < Constants.FloatTextValue.textBgMinimumWidth) {
                    width = Constants.FloatTextValue.textBgMinimumWidth;
                }
                if (height < Constants.FloatTextValue.textBgMinimumHeight) {
                    height = Constants.FloatTextValue.textBgMinimumHeight;
                }
                if (SidebarController.getInstance(context).getSidebarMode() == SidebarMode.MODE_LEFT) {
                    xOffset = xOffset + viewWidth + Constants.FloatTextValue.paddingWithSidebar;
                    yOffset = yOffset + (viewHeight - height) / 2;
                } else {
                    xOffset = xOffset - width - Constants.FloatTextValue.paddingWithSidebar;
                    yOffset = yOffset + (viewHeight - height) / 2;
                }
                if (!mPopupWindow.isShowing()) {
                    SideView sideView = SidebarController.getInstance(context).getSideView();
                    mPopupWindow.showAtLocation(sideView, Gravity.NO_GRAVITY, xOffset, yOffset);
                    ViewTreeObserver observer = mText.getViewTreeObserver();
                    observer.addOnGlobalLayoutListener(mTextViewObs);
                } else {
                    mFloatView.setVisibility(View.VISIBLE);
                    mPopupWindow.update(xOffset, yOffset, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                }
            }
        });
    }

    public static void hide() {
        if (mFloatView != null) {
            mFloatView.setVisibility(View.INVISIBLE);
        }
    }
}