package com.smartisanos.sidebar.util;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.smartisanos.sidebar.SidebarApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class CalendarIcon {

    private final static String CALENDAR_DAY_PREFIX = "calendar/";
    private final static String CALENDAR_BG = "calendar/bg.png";
    private final static String CALENDAR_FLIP = "calendar/flip.png";
    private final static String CALENDAR_RING = "calendar/ring.png";

    private static int calendar_back_size   = 192;
    private static int calendar_day_offsetx = 45;
    private static int calendar_day_offsety = 72;
    private static int calendar_day_w       = 102;
    private static int calendar_day_h       = 78;

    private static int mCurrentDay = -1;
    private static Bitmap mCalendarIcon;
    private static Bitmap mOldIcon;

    public static void generateIcon() {
        int currentDay = getCurrentDay();
        if (mCalendarIcon == null) {
            mCalendarIcon = createComposedBitmap(currentDay);
        } else {
            if (mCurrentDay != currentDay) {
                mOldIcon = mCalendarIcon;
                mCalendarIcon = createComposedBitmap(currentDay);
            }
        }
    }

    public static void releaseOldIcon() {
        if (mOldIcon != null) {
            mOldIcon.recycle();
            mOldIcon = null;
        }
    }

    public static Bitmap getCalendarIconBitmap() {
        generateIcon();
        return mCalendarIcon;
    }

    private static Bitmap createComposedBitmap(int currentDay) {
        Bitmap bgBitmap = getBitmap(CALENDAR_BG);
        Bitmap ringBitmap = getBitmap(CALENDAR_RING);
        Bitmap flipBitmap = getBitmap(CALENDAR_FLIP);
        String image = getDayImageName(currentDay);
        Bitmap day = getBitmap(image);
        Bitmap newBitmap = Bitmap.createBitmap(calendar_back_size, calendar_back_size, bgBitmap.getConfig());
        Canvas canvas = new Canvas(newBitmap);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setFilterBitmap(true);
        canvas.drawBitmap(bgBitmap, new Rect(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight()),
                new RectF(0, 0, calendar_back_size, calendar_back_size), p);
        canvas.drawBitmap(flipBitmap, new Rect(0, 0, flipBitmap.getWidth(), flipBitmap.getHeight()),
                new RectF(0, 0, calendar_back_size, calendar_back_size), p);
        canvas.drawBitmap(ringBitmap, new Rect(0, 0, ringBitmap.getWidth(), ringBitmap.getHeight()),
                new RectF(0, 0, calendar_back_size, calendar_back_size), p);
        canvas.drawBitmap(day, new Rect(0, 0, day.getWidth(), day.getHeight()),
                new RectF(calendar_day_offsetx, calendar_day_offsety,
                        calendar_day_offsetx + calendar_day_w,
                        calendar_day_offsety + calendar_day_h), p);
        bgBitmap.recycle();
        ringBitmap.recycle();
        flipBitmap.recycle();
        day.recycle();
        return newBitmap;
    }

    private static int getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    private static String getDayImageName(int day) {
        return CALENDAR_DAY_PREFIX + (day - 1) + ".png";
    }

    private static Bitmap getBitmap(String path) {
        Bitmap bmap = null;
        try {
            AssetManager manager = SidebarApplication.getInstance().getAssets();
            InputStream is = manager.open(path);;
            bmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmap;
    }
}