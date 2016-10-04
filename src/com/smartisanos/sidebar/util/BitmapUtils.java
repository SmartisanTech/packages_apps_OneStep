package com.smartisanos.sidebar.util;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.smartisanos.sidebar.R;

public class BitmapUtils {
    public static Bitmap getSquareBitmap(String filePath, int size){
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

        return getSquareBitmap(bitmap, size);
    }

    public static Bitmap getSquareBitmap(Bitmap bitmap, int size) {
        if (bitmap == null) {
            return null;
        }
        bitmap = getSquareBitmap(bitmap);
        if (bitmap.getWidth() != size) {
            Bitmap newBp = Bitmap.createScaledBitmap(bitmap, size, size, true);
            bitmap.recycle();
            bitmap = newBp;
        }
        return bitmap;
    }

    public static Bitmap getSquareBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getWidth() != bitmap.getHeight()) {
            int minSize = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
            Bitmap newBp = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - minSize) / 2, (bitmap.getHeight() - minSize) / 2, minSize, minSize);
            bitmap.recycle();
            bitmap = newBp;
        }
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap allNewBitmap(Bitmap src) {
        if (src == null || src.getWidth() <= 0 || src.getHeight() <= 0) {
            return null;
        }
        Config config = src.getConfig();
        if(config == null) {
            config = Bitmap.Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), config);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(src, 0, 0, null);
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        return drawableToBitmap(drawable, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
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

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int w;
        int deltaX = 0;
        int deltaY = 0;
        if (width <= height) {
            w = width;
            deltaY = height - w;
        } else {
            w = height;
            deltaX = width - w;
        }
        final Rect rect = new Rect(deltaX, deltaY, w, w);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        int radius = (int) (Math.sqrt(w * w * 2.0d) / 2);
        canvas.drawRoundRect(rectF, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap getDefaultContactAvatar(Context context){
        return getContactAvatar(context, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_contact_avatar));
    }

    public static Bitmap getContactAvatar(Context context, Bitmap photo){
        int avatarSize = context.getResources().getDimensionPixelSize(R.dimen.contact_avatar_size);
        Bitmap avatarMask = BitmapFactory.decodeResource(context.getResources(), R.drawable.avatar_mask);
        avatarMask = getSquareBitmap(avatarMask, avatarSize);

        Bitmap bitmap = Bitmap.createBitmap(avatarSize, avatarSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // step 1 draw photo
        photo = getSquareBitmap(photo, avatarSize);
        canvas.drawBitmap(photo, 0, 0, null);
        // step 2, draw mask on it!
        canvas.drawBitmap(avatarMask, 0, 0, null);

        return getRoundedCornerBitmap(bitmap);
    }

    public static byte[] Bitmap2Bytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static byte[] Drawable2Bytes(Drawable drawable) {
        return Bitmap2Bytes(drawableToBitmap(drawable));
    }

    public static Bitmap Bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
