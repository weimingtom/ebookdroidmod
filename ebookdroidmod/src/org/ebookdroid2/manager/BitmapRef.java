package org.ebookdroid2.manager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.ebookdroid2.page.PagePaint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Canvas.EdgeType;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.FloatMath;
import android.util.Log;

public class BitmapRef {
	private final static boolean D = false;
	private final static String TAG = "BitmapRef";	

    private static final AtomicInteger SEQ = new AtomicInteger();

    public final int id = SEQ.incrementAndGet();
    public final int size;
    public final int width;
    public final int height;
    public final Bitmap.Config config;
    public final boolean hasAlpha;
    public final AtomicBoolean used = new AtomicBoolean(true);
    public long gen;
    public String name;
    private volatile Bitmap bitmap;

    public BitmapRef(final Bitmap bitmap, final long generation) {
        this.config = bitmap.getConfig();
        this.hasAlpha = bitmap.hasAlpha();
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.size = BitmapManager.getBitmapBufferSize(width, height, config);
        this.gen = generation;        
    	this.bitmap = bitmap;
    }

    public Canvas getCanvas() {
        return new Canvas(bitmap);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void draw(final Canvas canvas, final PagePaint paint, final Rect src, final RectF r) {
        src.set(0, 0, this.width, this.height);
        final RectF dst = round(r);
        final Paint p = paint.bitmapPaint;
        draw(canvas, src, dst, p);
    }

    public void draw(final Canvas canvas, final Rect src, final RectF dst, final Paint p) {
        if (this.bitmap != null) {
            try {
                if (!canvas.quickReject(dst, EdgeType.BW)) {
                    canvas.drawBitmap(this.bitmap, src, dst, p);
                }
            } catch (final Throwable th) {
            	th.printStackTrace();
                if (D) {
                	Log.e(TAG, "Unexpected error: ", th);
                }
            }
        }
    }

    public void draw(final Canvas canvas, final Rect src, final Rect dst, final Paint p) {
        if (this.bitmap != null) {
            try {
                canvas.drawBitmap(this.bitmap, src, dst, p);
            } catch (final Throwable th) {
                th.printStackTrace();
            	if (D) {
                	Log.e(TAG, "Unexpected error: ", th);
                }
            }
        }
    }

    public void getPixels(final Bitmaps.RawBitmap slice, final int left, final int top, final int width, final int height) {
        slice.width = width;
        slice.height = height;
        bitmap.getPixels(slice.pixels, 0, width, left, top, width, height);
    }

    public void getPixels(final int[] pixels, final int width, final int height) {
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
    }

    public void setPixels(final int[] pixels, final int width, final int height) {
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    public void setPixels(final Bitmaps.RawBitmap raw) {
        bitmap.setPixels(raw.pixels, 0, raw.width, 0, 0, raw.width, raw.height);
    }

    public void eraseColor(final int color) {
        bitmap.eraseColor(color);
    }

    public int getAverageColor() {
        final int w = Math.min(bitmap.getWidth(), 7);
        final int h = Math.min(bitmap.getHeight(), 7);
        long r = 0, g = 0, b = 0;
        for (int i = 0; i < w; ++i) {
            for (int j = 0; j < h; ++j) {
                final int color = bitmap.getPixel(i, j);
                r += color & 0xFF0000;
                g += color & 0xFF00;
                b += color & 0xFF;
            }
        }
        r /= w * h;
        g /= w * h;
        b /= w * h;
        r >>= 16;
        g >>= 8;
        return Color.rgb((int) (r & 0xFF), (int) (g & 0xFF), (int) (b & 0xFF));
    }

    public boolean isRecycled() {
        if (bitmap != null) {
            if (!bitmap.isRecycled()) {
                return false;
            }
            bitmap = null;
        }
        return true;
    }

    public void recycle() {
        final Bitmap b = bitmap;
        bitmap = null;
        if (b != null) {
            if (BitmapManager.useEarlyRecycling) {
                b.recycle();
            }
        }
    }

    @Override
    public String toString() {
        return "BitmapRef [id=" + id + ", name=" + name + ", width=" + width + ", height=" + height + ", size=" + size
                + "]";
    }

    public void draw(final Canvas canvas, final int left, final int top, final Paint paint) {
        canvas.drawBitmap(bitmap, left, top, paint);
    }

    public void draw(final Canvas canvas, final Matrix matrix, final Paint paint) {
        canvas.drawBitmap(bitmap, matrix, paint);
    }

    @Override
    protected final void finalize() throws Throwable {
        recycle();
    }

    private static RectF round(final RectF rect) {
        rect.left = FloatMath.floor(rect.left);
        rect.top = FloatMath.floor(rect.top);
        rect.right = FloatMath.ceil(rect.right);
        rect.bottom = FloatMath.ceil(rect.bottom);
        return rect;
    }
}
