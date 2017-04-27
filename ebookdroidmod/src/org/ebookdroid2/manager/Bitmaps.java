package org.ebookdroid2.manager;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ebookdroid2.page.PagePaint;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.util.FloatMath;
import android.util.Log;

public class Bitmaps {
	private final static boolean D = false;
	private final static String TAG = "Bitmaps";		
	
    protected static final Config DEF_BITMAP_TYPE = Bitmap.Config.RGB_565;
    protected static boolean useDefaultBitmapType = true;
    protected static final ThreadLocal<RawBitmap> threadSlices = 
    		new ThreadLocal<RawBitmap>();

    public final Bitmap.Config config;
    public final int partSize;
    public Rect bounds;
    public int columns;
    public int rows;
    public String nodeId;
    protected final ReentrantReadWriteLock lock = 
    		new ReentrantReadWriteLock();
    protected BitmapRef[] bitmaps;
    
    public Bitmaps(final String nodeId, final BitmapRef orig, final Rect bitmapBounds, final boolean invert) {
        this.nodeId = nodeId;
        this.partSize = BitmapManager.partSize;
        this.bounds = bitmapBounds;
        this.columns = (int) FloatMath.ceil(bounds.width() / (float) partSize);
        this.rows = (int) FloatMath.ceil(bounds.height() / (float) partSize);
        this.config = useDefaultBitmapType ? DEF_BITMAP_TYPE : ((BitmapRef) orig).config;
        this.bitmaps = new BitmapRef[columns * rows];
        final boolean hasAlpha = ((BitmapRef) orig).hasAlpha;
        RawBitmap slice = threadSlices.get();
        if (slice == null || slice.pixels.length < partSize * partSize || slice.hasAlpha != hasAlpha) {
            slice = new RawBitmap(partSize, partSize, hasAlpha);
            threadSlices.set(slice);
        }
        int top = 0;
        for (int row = 0; row < rows; row++, top += partSize) {
            int left = 0;
            for (int col = 0; col < columns; col++, left += partSize) {
                final BitmapRef b = BitmapManager.getTexture(null, config);
                if (row == rows - 1 || col == columns - 1) {
                    final int right = Math.min(left + partSize, bounds.width());
                    final int bottom = Math.min(top + partSize, bounds.height());
                    b.eraseColor(invert ? PagePaint.NIGHT.fillPaint.getColor() : PagePaint.DAY.fillPaint.getColor());
                    orig.getPixels(slice, left, top, right - left, bottom - top);
                } else {
                    orig.getPixels(slice, left, top, partSize, partSize);
                }
                //FIXME:这里删除了slice.invert();
                b.setPixels(slice);
                final int index = row * columns + col;
                bitmaps[index] = b;
            }
        }
    }

    public boolean reuse(final String nodeId, final BitmapRef orig, final Rect bitmapBounds, final boolean invert) {
        lock.writeLock().lock();
        try {
            final Config cfg = useDefaultBitmapType ? DEF_BITMAP_TYPE : ((BitmapRef) orig).config;
            if (cfg != this.config) {
                return false;
            }
            if (BitmapManager.partSize != this.partSize) {
                return false;
            }
            final BitmapRef[] oldBitmaps = this.bitmaps;
            final int oldBitmapsLength = 
            	(oldBitmaps != null ? oldBitmaps.length : 0);
            this.nodeId = nodeId;
            this.bounds = bitmapBounds;
            this.columns = (int) FloatMath.ceil(bitmapBounds.width() / (float) partSize);
            this.rows = (int) FloatMath.ceil(bitmapBounds.height() / (float) partSize);
            this.bitmaps = new BitmapRef[columns * rows];
            final int newsize = this.columns * this.rows;
            int i = 0;
            for (; i < newsize; i++) {
                if (i < oldBitmapsLength) {
                    this.bitmaps[i] = oldBitmaps[i];
                    if (this.bitmaps[i] != null && this.bitmaps[i].isRecycled()) {
                        BitmapManager.release(this.bitmaps[i]);
                        this.bitmaps[i] = null;
                    }
                }
                if (this.bitmaps[i] == null) {
                    this.bitmaps[i] = BitmapManager.getTexture(null, config);
                } else {
                    if (D) {
                        Log.e(TAG, "Reuse  bitmap: " + this.bitmaps[i]);
                    }
                }
                this.bitmaps[i].eraseColor(Color.CYAN);
            }
            for (; i < oldBitmapsLength; i++) {
                BitmapManager.release(oldBitmaps[i]);
            }
            final boolean hasAlpha = ((BitmapRef) orig).hasAlpha;
            RawBitmap slice = threadSlices.get();
            if (slice == null || slice.pixels.length < partSize * partSize || slice.hasAlpha != hasAlpha) {
                slice = new RawBitmap(partSize, partSize, hasAlpha);
                threadSlices.set(slice);
            }
            int top = 0;
            for (int row = 0; row < rows; row++, top += partSize) {
                int left = 0;
                for (int col = 0; col < columns; col++, left += partSize) {
                    final int index = row * columns + col;
                    final BitmapRef b = bitmaps[index];
                    if (row == rows - 1 || col == columns - 1) {
                        final int right = Math.min(left + partSize, bounds.width());
                        final int bottom = Math.min(top + partSize, bounds.height());
                        b.eraseColor(invert ? PagePaint.NIGHT.fillPaint.getColor() : PagePaint.DAY.fillPaint.getColor());
                        orig.getPixels(slice, left, top, right - left, bottom - top);
                    } else {
                        orig.getPixels(slice, left, top, partSize, partSize);
                    }
                    //FIXME:这里删除了slice.invert();
                    b.setPixels(slice);
                }
            }
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean hasBitmaps() {
        lock.readLock().lock();
        try {
            if (bitmaps == null) {
                return false;
            }
            for (int i = 0; i < bitmaps.length; i++) {
                if (bitmaps[i] == null) {
                    return false;
                }
                if (bitmaps[i].isRecycled()) {
                    return false;
                }
            }
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

    BitmapRef[] clear() {
        lock.writeLock().lock();
        try {
            final BitmapRef[] refs = this.bitmaps;
            this.bitmaps = null;
            return refs;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        lock.writeLock().lock();
        try {
            if (bitmaps != null) {
                for (final BitmapRef ref : bitmaps) {
                    BitmapManager.release(ref);
                }
                bitmaps = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean draw(final Canvas canvas, final PagePaint paint, final PointF vb, final RectF tr, final RectF cr) {
        lock.readLock().lock();
        try {
            if (this.bitmaps == null) {
                return false;
            }
            if (D) {
                Log.e(TAG, "draw()");
            }
            final RectF actual = new RectF(cr.left - vb.x, cr.top - vb.y, cr.right - vb.x, cr.bottom - vb.y);
            final Rect orig = canvas.getClipBounds();
            canvas.clipRect(actual, Op.INTERSECT);
            final float offsetX = tr.left - vb.x;
            final float offsetY = tr.top - vb.y;
            final float scaleX = tr.width() / bounds.width();
            final float scaleY = tr.height() / bounds.height();
            final float sizeX = partSize * scaleX;
            final float sizeY = partSize * scaleY;
            final Rect src = new Rect();
            final RectF rect = new RectF(offsetX, offsetY, offsetX + sizeX, offsetY + sizeY);
            final RectF r = new RectF();
            boolean res = true;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    final int index = row * columns + col;
                    final BitmapRef ref = this.bitmaps[index];
                    if (ref != null) {
                        r.set(rect);
                        src.set(0, 0, ref.width, ref.height);
                        ref.draw(canvas, paint, src, r);
                    } else {
                        res = false;
                    }
                    rect.left += sizeX;
                    rect.right += sizeX;
                }
                rect.left = offsetX;
                rect.right = offsetX + sizeX;

                rect.top += sizeY;
                rect.bottom += sizeY;
            }
            canvas.clipRect(orig, Op.REPLACE);
            return res;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public final class RawBitmap {
        final int[] pixels;
        int width;
        int height;
        boolean hasAlpha;

        public RawBitmap(final int width, final int height, final boolean hasAlpha) {
            this.width = width;
            this.height = height;
            this.hasAlpha = hasAlpha;
            this.pixels = new int[width * height];
        }
    }
}
