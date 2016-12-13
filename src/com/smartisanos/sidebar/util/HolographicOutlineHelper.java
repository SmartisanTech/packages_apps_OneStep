package com.smartisanos.sidebar.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class HolographicOutlineHelper {
    private static final String TAG = "HolographicOutlineHelper";

    private final Paint mHolographicPaint = new Paint();
    private final Paint mBlurPaint = new Paint();
    private final Paint mErasePaint = new Paint();
    private final Paint mAlphaClipPaint = new Paint();

    public static final int MAX_OUTER_BLUR_RADIUS;
    public static final int MIN_OUTER_BLUR_RADIUS;

    private static final float SCALE = 1.5f;
    static {
        final float scale = 1.5f;
        MIN_OUTER_BLUR_RADIUS = (int) (scale * 1.0f);
        MAX_OUTER_BLUR_RADIUS = (int) (scale * 12.0f);
    }

    private int[] mTempOffset = new int[2];

    HolographicOutlineHelper() {
        mHolographicPaint.setFilterBitmap(true);
        mHolographicPaint.setAntiAlias(true);
        mBlurPaint.setFilterBitmap(true);
        mBlurPaint.setAntiAlias(true);
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mErasePaint.setFilterBitmap(true);
        mErasePaint.setAntiAlias(true);
    }

    Bitmap applyCustomExpensiveOutlineWithBlur(Bitmap srcDst, Canvas srcDstCanvas, int color,
            int outlineColor, float radius) {
        // We start by removing most of the alpha channel so as to ignore shadows, and
        // other types of partial transparency when defining the shape of the object
        Bitmap glowShape = srcDst.extractAlpha(mAlphaClipPaint, mTempOffset);

        BlurMaskFilter outerBlurMaskFilter = new BlurMaskFilter(SCALE * radius, BlurMaskFilter.Blur.NORMAL);
        mBlurPaint.setMaskFilter(outerBlurMaskFilter);
        int[] outerBlurOffset = new int[2];
        Bitmap thickOuterBlur = glowShape.extractAlpha(mBlurPaint, outerBlurOffset);

        int[] brightOutlineOffset = new int[2];
        Bitmap brightOutline = glowShape.extractAlpha(mBlurPaint, brightOutlineOffset);

        // calculate the inner blur
        srcDstCanvas.setBitmap(glowShape);
        srcDstCanvas.drawColor(0xFF000000, PorterDuff.Mode.CLEAR);
        BlurMaskFilter innerBlurMaskFilter = new BlurMaskFilter(SCALE * radius, BlurMaskFilter.Blur.OUTER);
        mBlurPaint.setMaskFilter(innerBlurMaskFilter);
        int[] thickInnerBlurOffset = new int[2];
        Bitmap thickInnerBlur = glowShape.extractAlpha(mBlurPaint, thickInnerBlurOffset);

        // mask out the inner blur
        srcDstCanvas.setBitmap(thickInnerBlur);
        srcDstCanvas.drawBitmap(glowShape, -thickInnerBlurOffset[0],
                -thickInnerBlurOffset[1], mErasePaint);
        srcDstCanvas.drawRect(0, 0, -thickInnerBlurOffset[0], thickInnerBlur.getHeight(),
                mErasePaint);
        srcDstCanvas.drawRect(0, 0, thickInnerBlur.getWidth(), -thickInnerBlurOffset[1],
                mErasePaint);

        Bitmap dest = Bitmap.createBitmap(brightOutline.getWidth(), brightOutline.getHeight(), Bitmap.Config.ARGB_8888);
        float offsetx = (brightOutline.getWidth() - srcDst.getWidth()) / 2f;
        // draw the inner and outer blur
        srcDstCanvas.setBitmap(dest);
//        srcDstCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        mHolographicPaint.setColor(color);
        srcDstCanvas.drawBitmap(thickInnerBlur, thickInnerBlurOffset[0] + offsetx, thickInnerBlurOffset[1],
                mHolographicPaint);
        srcDstCanvas.drawBitmap(thickOuterBlur, outerBlurOffset[0] + offsetx, outerBlurOffset[1],
                mHolographicPaint);

        // draw the bright outline
        mHolographicPaint.setColor(outlineColor);
        srcDstCanvas.drawBitmap(brightOutline, brightOutlineOffset[0] + offsetx, brightOutlineOffset[1],
                mHolographicPaint);

        // cleanup
        srcDstCanvas.setBitmap(null);
        brightOutline.recycle();
        thickOuterBlur.recycle();
        thickInnerBlur.recycle();
        glowShape.recycle();
        return dest;
    }

    public static int[][] ICON_SHADOW_COLOR = new int[2][];
    static {
        ICON_SHADOW_COLOR[0] = new int[] {0x2f000000, 0x3f000000};
        ICON_SHADOW_COLOR[1] = new int[] {0x12000000, 0x12000000};
    }

    public static Bitmap createDragOutline(Bitmap bm, int shadowIndex) {
        int sh = 246;
        float w = 192;
        int[] radius = {9, 3};
        int[] color = ICON_SHADOW_COLOR[shadowIndex];
        final Bitmap b = Bitmap.createBitmap(sh,
                sh, Bitmap.Config.ARGB_8888);
        Paint p = new Paint();
        p.setAntiAlias(true);
        Canvas c = new Canvas(b);
        final float deltax = (sh - w) / 2;
        final float deltay = deltax / 2;
        for(int i = 0; i < radius.length; i++) {
            Bitmap bt = getOutlineFill(bm, new Canvas(),
                    radius[i], color[i]);
            float deltaxs = (sh - bt.getWidth()) / 2;
            float deltays = deltay + Math.round(Math.sqrt(radius[i]));
            c.drawBitmap(bt, deltaxs, deltays, p);
            bt.recycle();
        }
        Paint pa = new Paint();
        c.drawBitmap(bm, deltax, deltay, pa);
        return b;
    }

    public static Bitmap getOutlineFill(Bitmap bm, Canvas canvas, float r, int alpha) {
        HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
        final int outlineColor = alpha;
        final Bitmap b = Bitmap.createBitmap(
                bm.getWidth() + 2 * Math.round(r), bm.getHeight() + 2 * Math.round(r), Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        canvas.drawBitmap(bm, Math.round(r), Math.round(r), new Paint());
        Bitmap dest = mOutlineHelper.applyCustomExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor, r);
        canvas.setBitmap(null);
        return dest;
    }

    public static Bitmap getOutline(Context context, Bitmap bm, int innerColor, int outerColor, int radius) {
        Canvas canvas = new Canvas();
        final Bitmap b = Bitmap.createBitmap(bm.getWidth(),
                bm.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(b);
        Paint paint = new Paint();
        BlurMaskFilter blurFilter = new BlurMaskFilter(radius,
                BlurMaskFilter.Blur.OUTER);
        Paint shadowPaint = new Paint();
        shadowPaint.setMaskFilter(blurFilter);
        int[] outerPosition = new int[2];
        Bitmap shadowBitmap = bm.extractAlpha(shadowPaint, outerPosition);
        paint.setColor(outerColor);
        canvas.drawBitmap(shadowBitmap, outerPosition[0], outerPosition[1], paint);

        BlurMaskFilter blurFilter2 = new BlurMaskFilter(radius,
                BlurMaskFilter.Blur.INNER);
        Paint shadowPaint2 = new Paint();
        shadowPaint.setMaskFilter(blurFilter2);
        int[] innerPosition = new int[2];
        Bitmap shadowBitmap2 = bm.extractAlpha(shadowPaint2, innerPosition);
        paint.setColor(innerColor);
        canvas.drawBitmap(shadowBitmap2, -innerPosition[0], -innerPosition[1], paint);
        return b;
    }

}
