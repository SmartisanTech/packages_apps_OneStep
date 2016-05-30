package com.smartisanos.sidebar.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class BitmapUtils {
    public static Bitmap getBitmap(String filePath, int size){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = options.outHeight > options.outWidth ? options.outHeight / size
                : options.outWidth / size;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(filePath, options);

        if(bitmap == null){
            return null;
        }

        if (bitmap.getWidth() != bitmap.getHeight()) {
            int minSize = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
            Bitmap newBp = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - minSize) / 2, (bitmap.getHeight() - minSize) / 2, minSize, minSize);
            bitmap.recycle();
            bitmap = newBp;
        }

        if(bitmap.getWidth() != size){
            Bitmap newBp = Bitmap.createScaledBitmap(bitmap, size, size, true);
            bitmap.recycle();
            bitmap = newBp;
        }
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;

    }

    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int alpha = ((grey & 0xFF000000) >> 24);
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = (alpha << 24) | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }

        Bitmap newBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBmp;
    }

    public static Bitmap convertToBlackWhite(Drawable drawable){
        return convertToBlackWhite(drawableToBitmap(drawable));
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpGrayscale);
        canvas.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}
