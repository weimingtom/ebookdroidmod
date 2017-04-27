package org.ebookdroid2.page;

import java.util.List;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.codec.CodecPageInfo;
import org.ebookdroid2.codec.PageLink;
import org.ebookdroid2.manager.Bitmaps;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.manager.DocumentViewMode;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.FloatMath;

public class Page {
    public final PageIndex index;
    public final PageType type;
    public final CodecPageInfo cpi;
    public final IActivityController base;
    public final PageTree nodes;
    public RectF bounds;
    private int aspectRatio;
    public boolean recycled;
    private float storedZoom;
    private RectF zoomedBounds;
    private int zoomLevel = 1;
    public List<PageLink> links;

    public Page(final IActivityController base, final PageIndex index, 
    		final PageType pt, final CodecPageInfo cpi) {
        this.base = base;
        this.index = index;
        this.cpi = cpi;
        this.type = pt != null ? pt : PageType.FULL_PAGE;
        this.bounds = new RectF(0, 0, 
        		cpi.width / type.getWidthScale(), 
        		cpi.height);
        setAspectRatio(cpi);
        nodes = new PageTree(this, this.base.getContext());
    }

    public void recycle(final List<Bitmaps> bitmapsToRecycle) {
        recycled = true;
        nodes.recycleAll(bitmapsToRecycle, true);
    }

    public float getAspectRatio() {
        return aspectRatio / 128.0f;
    }

    private boolean setAspectRatio(final float aspectRatio) {
        final int newAspectRatio = (int) FloatMath.floor(aspectRatio * 128);
        if (this.aspectRatio != newAspectRatio) {
            this.aspectRatio = newAspectRatio;
            return true;
        }
        return false;
    }

    public boolean setAspectRatio(final CodecPageInfo page) {
        if (page != null) {
            return this.setAspectRatio(
            		page.width / type.getWidthScale(), page.height);
        }
        return false;
    }

    public boolean setAspectRatio(final float width, 
    		final float height) {
        return setAspectRatio(width / height);
    }

    public void setBounds(final RectF pageBounds) {
        storedZoom = 0.0f;
        zoomedBounds = null;
        bounds = pageBounds;
    }

    public void setBounds(final float l, final float t, 
    		final float r, final float b) {
        if (bounds == null) {
            bounds = new RectF(l, t, r, b);
        } else {
            bounds.set(l, t, r, b);
        }
    }

    public boolean shouldCrop() {
        final BookSettings bs = base.getBookSettings();
        if (nodes.root.hasManualCropping()) {
            return true;
        }
        return bs != null && bs.cropPages;
    }

    public RectF getCropping() {
        return shouldCrop() ? nodes.root.getCropping() : null;
    }

    public RectF getCropping(PageTreeNode node) {
        return shouldCrop() ? node.getCropping() : null;
    }

    protected void updateAspectRatio() {
        final RectF cropping = getCropping();
        if (cropping != null) {
            final float pageWidth = cpi.width * cropping.width();
            final float pageHeight = cpi.height * cropping.height();
            setAspectRatio(pageWidth, pageHeight);
        } else {
            setAspectRatio(cpi);
        }
    }

    public RectF getBounds(final float zoom) {
        RectF bounds = new RectF();
        getBounds(zoom, bounds);
        return bounds;
    }

    public void getBounds(final float zoom, RectF target) {
        zoom(bounds, zoom, target);
        final BookSettings bs = base.getBookSettings();
        if (bs != null && 
        		bs.viewMode == DocumentViewMode.SINGLE_PAGE && 
        		bounds.left > 0) {
            target.offset((bounds.left + bounds.right)*(1 - zoom)/2, 0);
        }
    }
    
    private static void zoom(final RectF rect, final float zoom, 
    		final RectF target) {
        target.left = rect.left * zoom;
        target.right = rect.right * zoom;
        target.top = rect.top * zoom;
        target.bottom = rect.bottom * zoom;
    }

    public float getTargetRectScale() {
        return type.getWidthScale();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("Page");
        buf.append("[");
        buf.append("index").append("=").append(index);
        buf.append(", ");
        buf.append("bounds").append("=").append(bounds);
        buf.append(", ");
        buf.append("aspectRatio").append("=").append(aspectRatio);
        buf.append(", ");
        buf.append("type").append("=").append(type.name());
        buf.append("]");
        return buf.toString();
    }

    public static RectF getTargetRect(final PageType pageType, 
    		final RectF pageBounds, final RectF normalizedRect) {
        final Matrix tmpMatrix = getMatrix();
        tmpMatrix.postScale(pageBounds.width() * pageType.getWidthScale(), pageBounds.height());
        tmpMatrix.postTranslate(pageBounds.left - pageBounds.width() * pageType.getLeftPos(), pageBounds.top);
        final RectF targetRectF = new RectF();
        tmpMatrix.mapRect(targetRectF, normalizedRect);
        floor(targetRectF);
        return targetRectF;
    }

    private static RectF floor(final RectF rect) {
        rect.left = FloatMath.floor(rect.left);
        rect.top = FloatMath.floor(rect.top);
        rect.right = FloatMath.floor(rect.right);
        rect.bottom = FloatMath.floor(rect.bottom);
        return rect;
    }
    
    public RectF getLinkSourceRect(final RectF pageBounds, 
    		final PageLink link) {
        if (link == null || link.sourceRect == null) {
            return null;
        }
        return getPageRegion(pageBounds, new RectF(link.sourceRect));
    }

    public RectF getPageRegion(final RectF pageBounds, 
    		final RectF sourceRect) {
        final RectF cb = getCropping();
        if (cb != null) {
            final Matrix m = getMatrix();
            final RectF psb = nodes.root.pageSliceBounds;
            m.postTranslate(psb.left - cb.left, psb.top - cb.top);
            m.postScale(psb.width() / cb.width(), psb.height() / cb.height());
            m.mapRect(sourceRect);
        }
        if (type == PageType.LEFT_PAGE && sourceRect.left >= 0.5f) {
            return null;
        }
        if (type == PageType.RIGHT_PAGE && sourceRect.right < 0.5f) {
            return null;
        }
        return getTargetRect(type, pageBounds, sourceRect);
    }
    
	public static Matrix getMatrix() {
		Matrix matrix = new Matrix();
		matrix.reset();
		return matrix;
	}
}
