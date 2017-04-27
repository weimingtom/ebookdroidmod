package org.ebookdroid2.page;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.ebookdroid2.codec.CodecPage;
import org.ebookdroid2.core.AbstractViewController;
import org.ebookdroid2.core.IViewController;
import org.ebookdroid2.event.EventPool;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BitmapManager;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.manager.Bitmaps;
import org.ebookdroid2.manager.BookSettings;
import org.ebookdroid2.model.DecodingProgressModel;
import org.ebookdroid2.model.DocumentModel.PageInfo;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.service.DecodeService;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * FIXME:这里可能涉及一些跟剪裁有关的代码
 */
public class PageTreeNode implements DecodeService.DecodeCallback {
	private final static boolean D = false;
	private final static String TAG = "PageTreeNode";	
	
    public final Page page;
    public final PageTreeNode parent;
    public final int id;
    public final PageTreeLevel level;
    private final String shortId;
    public final String fullId;
    private final AtomicBoolean decodingNow = new AtomicBoolean();
    public final BitmapHolder holder = new BitmapHolder();
    private final RectF localPageSliceBounds;
    public final RectF pageSliceBounds;
    public float bitmapZoom = 1;
    private RectF autoCropping = null; //FIXME:可能没用了
    private RectF manualCropping = null; //FIXME:可能没用了
    private Context mContext;
    
    public RectF getCropping() {
        return manualCropping != null ? manualCropping : autoCropping;
    }

    public boolean hasManualCropping() {
        return manualCropping != null;
    }

    public void setInitialCropping(final PageInfo pi) {
        if (id != 0) {
            return;
        }
        if (pi != null) {
            autoCropping = pi.autoCropping != null ? new RectF(pi.autoCropping) : null;
            manualCropping = pi.manualCropping != null ? new RectF(pi.manualCropping) : null;
        } else {
            autoCropping = null;
            manualCropping = null;
        }
        page.updateAspectRatio();
    }

    public void setAutoCropping(final RectF r, final boolean commit) {
        autoCropping = r;
        if (id == 0) {
            if (commit) {
                page.base.getDocumentModel().updateAutoCropping(page, r);
            }
            page.updateAspectRatio();
        }
    }

    public void setManualCropping(final RectF r, final boolean commit) {
        manualCropping = r;
        if (id == 0) {
            if (commit) {
                page.base.getDocumentModel().updateManualCropping(page, r);
            }
            page.updateAspectRatio();
        }
    }

    public PageTreeNode(final Page page, Context context) {
        //FIXME:???这里不要这样写
    	assert page != null;
        this.page = page;
        this.parent = null;
        this.id = 0;
        this.level = PageTreeLevel.ROOT;
        this.shortId = page.index.viewIndex + ":0";
        this.fullId = page.index + ":0";
        this.localPageSliceBounds = page.type.getInitialRect();
        this.pageSliceBounds = localPageSliceBounds;
        this.autoCropping = null;
        this.manualCropping = null;
        this.mContext = context;
    }

    public PageTreeNode(final Page page, final PageTreeNode parent, final int id, final RectF localPageSliceBounds, Context context) {
    	//FIXME:???这里不要这样写
    	assert id != 0;
        assert page != null;
        assert parent != null;
        this.page = page;
        this.parent = parent;
        this.id = id;
        this.level = parent.level.next;
        this.shortId = page.index.viewIndex + ":" + id;
        this.fullId = page.index + ":" + id;
        this.localPageSliceBounds = localPageSliceBounds;
        this.pageSliceBounds = evaluatePageSliceBounds(localPageSliceBounds, parent);
        this.mContext = context;
        evaluateCroppedPageSliceBounds();
    }

    @Override
    protected void finalize() throws Throwable {
        holder.recycle(null);
    }

    public boolean recycle(final List<Bitmaps> bitmapsToRecycle) {
        stopDecodingThisNode("node recycling");
        return holder.recycle(bitmapsToRecycle);
    }

    public void decodePageTreeNode(final List<PageTreeNode> nodesToDecode, final ViewState viewState) {
        if (this.decodingNow.compareAndSet(false, true)) {
            bitmapZoom = viewState.zoom;
            nodesToDecode.add(this);
        }
    }

    public void stopDecodingThisNode(final String reason) {
        if (this.decodingNow.compareAndSet(true, false)) {
            final DecodingProgressModel dpm = page.base.getDecodingProgressModel();
            if (dpm != null) {
                dpm.decrease();
            }
            if (reason != null) {
                final DecodeService ds = page.base.getDecodeService();
                if (ds != null) {
                    ds.stopDecoding(this, reason);
                }
            }
        }
    }

    @Override
    public void decodeComplete(final CodecPage codecPage, final BitmapRef bitmap, final Rect bitmapBounds,
            final RectF croppedPageBounds) {
        try {
            if (bitmap == null || bitmapBounds == null) {
                stopDecodingThisNode(null);
                return;
            }
            final BookSettings bs = page.base.getBookSettings();
            if (bs != null) {
                //FIXME:这里是调整correctContrast || correctExposure || bs.autoLevels
                //代码已经被删除
            }
            final Bitmaps bitmaps = holder.reuse(fullId, bitmap, bitmapBounds);
            holder.setBitmap(bitmaps);
            stopDecodingThisNode(null);
            final IViewController dc = page.base.getDocumentController();
            if (dc instanceof AbstractViewController) {
                EventPool.newEventChildLoaded((AbstractViewController) dc, PageTreeNode.this, bitmapBounds, this.mContext).process()
                        .release();
            }
        } catch (final OutOfMemoryError ex) {
        	ex.printStackTrace();
            if (D) {
            	Log.e(TAG, "No memory: ", ex);
            }
            BitmapManager.clear("PageTreeNode OutOfMemoryError: ");
            stopDecodingThisNode(null);
        } finally {
            BitmapManager.release(bitmap);
        }
    }

    public RectF getTargetRect(final RectF pageBounds) {
        return Page.getTargetRect(page.type, pageBounds, pageSliceBounds);
    }

    public static RectF evaluatePageSliceBounds(final RectF localPageSliceBounds, final PageTreeNode parent) {
        final Matrix tmpMatrix = getMatrix();
        tmpMatrix.postScale(parent.pageSliceBounds.width(), parent.pageSliceBounds.height());
        tmpMatrix.postTranslate(parent.pageSliceBounds.left, parent.pageSliceBounds.top);
        final RectF sliceBounds = new RectF();
        tmpMatrix.mapRect(sliceBounds, localPageSliceBounds);
        return sliceBounds;
    }

    public void evaluateCroppedPageSliceBounds() {
        if (parent == null) {
            return;
        }
        if (parent.getCropping() == null) {
            parent.evaluateCroppedPageSliceBounds();
        }
        autoCropping = evaluateCroppedPageSliceBounds(parent.autoCropping, this.localPageSliceBounds);
        manualCropping = evaluateCroppedPageSliceBounds(parent.manualCropping, this.localPageSliceBounds);
    }

    public static RectF evaluateCroppedPageSliceBounds(final RectF crop, final RectF slice) {
        if (crop == null) {
            return null;
        }
        final RectF sliceBounds = new RectF();
        final Matrix tmpMatrix = getMatrix();
        tmpMatrix.postScale(crop.width(), crop.height());
        tmpMatrix.postTranslate(crop.left, crop.top);
        tmpMatrix.mapRect(sliceBounds, slice);
        return sliceBounds;
    }

    @Override
    public int hashCode() {
        return (page == null) ? 0 : page.index.viewIndex;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PageTreeNode) {
            final PageTreeNode that = (PageTreeNode) obj;
            if (this.page == null) {
                return that.page == null;
            }
            return this.page.index.viewIndex == that.page.index.viewIndex
                    && this.pageSliceBounds.equals(that.pageSliceBounds);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("PageTreeNode");
        buf.append("[");
        buf.append("id").append("=").append(page.index.viewIndex).append(":").append(id);
        buf.append(", ");
        buf.append("rect").append("=").append(this.pageSliceBounds);
        buf.append(", ");
        buf.append("hasBitmap").append("=").append(holder.hasBitmaps());
        buf.append("]");
        return buf.toString();
    }

    //FIXME:这个类移动到外面
    public class BitmapHolder {
        final AtomicReference<Bitmaps> ref = new AtomicReference<Bitmaps>();

        public boolean drawBitmap(final Canvas canvas, final PagePaint paint, final PointF viewBase,
                final RectF targetRect, final RectF clipRect) {
            final Bitmaps bitmaps = ref.get();
            return bitmaps != null ? bitmaps.draw(canvas, paint, viewBase, targetRect, clipRect) : false;
        }

        public Bitmaps reuse(final String nodeId, final BitmapRef bitmap, final Rect bitmapBounds) {
            final BookSettings bs = page.base.getBookSettings();
            final AppSettings app = AppSettings.current();
            final boolean invert = bs != null ? bs.nightMode : app.nightMode;
            if (app.textureReuseEnabled) {
                final Bitmaps bitmaps = ref.get();
                if (bitmaps != null) {
                    if (bitmaps.reuse(nodeId, bitmap, bitmapBounds, invert)) {
                        return bitmaps;
                    }
                }
            }
            return page.base.getView().createBitmaps(nodeId, bitmap, bitmapBounds, invert);
        }

        public boolean hasBitmaps() {
            final Bitmaps bitmaps = ref.get();
            return bitmaps != null ? bitmaps.hasBitmaps() : false;
        }

        public boolean recycle(final List<Bitmaps> bitmapsToRecycle) {
            final Bitmaps bitmaps = ref.getAndSet(null);
            if (bitmaps != null) {
                if (bitmapsToRecycle != null) {
                    bitmapsToRecycle.add(bitmaps);
                } else {
                    BitmapManager.release(Arrays.asList(bitmaps));
                }
                return true;
            }
            return false;
        }

        public void setBitmap(final Bitmaps bitmaps) {
            if (bitmaps == null) {
                return;
            }
            final Bitmaps oldBitmaps = ref.getAndSet(bitmaps);
            if (oldBitmaps != null && oldBitmaps != bitmaps) {
                BitmapManager.release(Arrays.asList(oldBitmaps));
            }
        }
    }
    
	public static Matrix getMatrix() {
		Matrix matrix = new Matrix();
		matrix.reset();
		return matrix;
	}
}
