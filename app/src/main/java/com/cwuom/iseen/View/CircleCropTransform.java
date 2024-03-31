package com.cwuom.iseen.View;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

import androidx.annotation.NonNull;

/**
 * <a href="https://blog.csdn.net/weixin_44911775/article/details/122563061">原文地址</a>
 * android Glide实现圆角和圆形图
 * */
public class CircleCropTransform extends BitmapTransformation {

    private final Paint mBorderPaint;
    private final float mBorderSize;

    /**
     * @param borderSize 边框宽度(px)
     * @param borderColor 边框颜色
     */
    public CircleCropTransform(float borderSize, int borderColor) {
        this(TypedValue.COMPLEX_UNIT_DIP, borderSize, borderColor);
    }

    /**
     * @param unit        borderSize 单位
     * @param borderSize 边框宽度(px)
     * @param borderColor 边框颜色
     */
    public CircleCropTransform(int unit, float borderSize, int borderColor) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        mBorderSize = TypedValue.applyDimension(unit, borderSize, displayMetrics);
        mBorderPaint = new Paint();
        mBorderPaint.setDither(true);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderSize);
    }

    private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        int size = (int) (Math.min(source.getWidth(), source.getHeight()) - (mBorderSize / 2));
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        if (mBorderPaint != null) {
            float borderRadius = r - mBorderSize / 2;
            canvas.drawCircle(r, r, borderRadius, mBorderPaint);
        }
        return result;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }
}
