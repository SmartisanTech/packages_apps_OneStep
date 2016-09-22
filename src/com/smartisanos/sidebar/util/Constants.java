package com.smartisanos.sidebar.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.smartisanos.sidebar.R;

public class Constants {
    private static final LOG log = LOG.getInstance(Constants.class);

    public static int WindowWidth;
    public static int WindowHeight;

    public static int SHADOW_BG_COLOR_LIGHT;
    public static int SHADOW_BG_COLOR_DARK;

    public static int share_item_offset_x;
    public static int share_item_offset_y;

    public static void init(Context context) {
        int[] size = getWindowSize(context);
        WindowWidth = size[0];
        WindowHeight = size[1];

        Resources resources = context.getResources();
        SHADOW_BG_COLOR_LIGHT = resources.getColor(R.color.light_bg_color);
        SHADOW_BG_COLOR_DARK = resources.getColor(R.color.dark_bg_color);
        share_item_offset_x = resources.getDimensionPixelSize(R.dimen.share_item_icon_offset_x);
        share_item_offset_y = resources.getDimensionPixelSize(R.dimen.share_item_icon_offset_y);
    }

    public static int[] getWindowSize(Context context) {
        int[] size = new int[2];
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        if (metrics.heightPixels > metrics.widthPixels) {
            size[0] = metrics.widthPixels;
            size[1] = metrics.heightPixels;
        } else {
            size[0] = metrics.heightPixels;
            size[1] = metrics.widthPixels;
        }
        return size;
    }
}