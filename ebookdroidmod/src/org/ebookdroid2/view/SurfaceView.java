package org.ebookdroid2.view;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.ebookdroid2.activity.IActivityController;
import org.ebookdroid2.manager.AppSettings;
import org.ebookdroid2.manager.BitmapRef;
import org.ebookdroid2.manager.Bitmaps;
import org.ebookdroid2.model.ViewState;
import org.ebookdroid2.page.Page;
import org.ebookdroid2.service.DecodeService;
import org.ebookdroid2.util.Flag;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Scroller;

public final class SurfaceView extends android.view.SurfaceView implements IView, SurfaceHolder.Callback {
	private final static boolean D = false;
	private final static String TAG = "SurfaceView";
	
    protected final IActivityController base;
    protected final Scroller scroller;
    protected DrawThread drawThread;
    protected ScrollEventThread scrollThread;
    protected boolean layoutLocked;
    protected final AtomicReference<Rect> layout = new AtomicReference<Rect>();
    protected final Flag layoutFlag = new Flag();

    public SurfaceView(final IActivityController baseActivity) {
        super(baseActivity.getContext());
        this.base = baseActivity;
        this.scroller = new Scroller(getContext());
        setKeepScreenOn(AppSettings.current().keepScreenOn);
        setFocusable(true);
        setFocusableInTouchMode(true);
        getHolder().addCallback(this);
        scrollThread = new ScrollEventThread(base, this);
        scrollThread.start();
    }

    @Override
    public final View getView() {
        return this;
    }

    @Override
    public final IActivityController getBase() {
        return base;
    }

    @Override
    public final Scroller getScroller() {
        return scroller;
    }

    @Override
    public final void invalidateScroll() {
        stopScroller();
        final float scrollScaleRatio = getScrollScaleRatio();
        scrollTo((int) (getScrollX() * scrollScaleRatio), (int) (getScrollY() * scrollScaleRatio));
    }

    @Override
    public final void invalidateScroll(final float newZoom, final float oldZoom) {
        stopScroller();
        final float ratio = newZoom / oldZoom;
        final float halfWidth = getWidth() / 2.0f;
        final float halfHeight = getHeight() / 2.0f;
        final int x = (int) ((getScrollX() + halfWidth) * ratio - halfWidth);
        final int y = (int) ((getScrollY() + halfHeight) * ratio - halfHeight);
        if (D) {
        	Log.e(TAG, "invalidateScroll(" + newZoom + ", " + oldZoom + "): " + x + ", " + y);
        }
        scrollTo(x, y);
    }

    @Override
    public void startPageScroll(final int dx, final int dy) {
        scroller.startScroll(getScrollX(), getScrollY(), dx, dy);
        redrawView();
    }

    @Override
    public void startFling(final float vX, final float vY, final Rect limits) {
        scroller.fling(getScrollX(), getScrollY(), -(int) vX, -(int) vY, limits.left, limits.right, limits.top,
                limits.bottom);
    }

    @Override
    public void continueScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
        }
    }

    @Override
    public void forceFinishScroll() {
    	//是否fling
        if (!scroller.isFinished()) { 
            //停止fling
        	scroller.forceFinished(true); 
        }
    }

    @Override
    public void onScrollChanged(final int curX, final int curY, final int oldX, final int oldY) {
        super.onScrollChanged(curX, curY, oldX, oldY);
        scrollThread.onScrollChanged(curX, curY, oldX, oldY);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        if (base.getDocumentController().onTouchEvent(ev)) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public final void scrollTo(final int x, final int y) {
        scrollThread.scrollTo(x, y);
    }

    @Override
    public void _scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    public final RectF getViewRect() {
        return new RectF(getScrollX(), getScrollY(), getScrollX() + getWidth(), getScrollY() + getHeight());
    }

    @Override
    public void changeLayoutLock(final boolean lock) {
        post(new Runnable() {
            @Override
            public void run() {
                layoutLocked = lock;
            }
        });
    }

    @Override
    public boolean isLayoutLocked() {
        return layoutLocked;
    }

    @Override
    protected final void onLayout(final boolean layoutChanged, final int left, final int top, final int right,
            final int bottom) {
        super.onLayout(layoutChanged, left, top, right, bottom);
        final Rect oldLayout = layout.getAndSet(
        	new Rect(left, top, right, bottom));
        base.getDocumentController().onLayoutChanged(layoutChanged, 
        	layoutLocked, oldLayout, layout.get());
        if (oldLayout == null) {
            layoutFlag.set();
        }
    }

    @Override
    public final void waitForInitialization() {
        while (!layoutFlag.get()) {
            layoutFlag.waitFor(TimeUnit.SECONDS, 1);
        }
    }

    @Override
    public void onDestroy() {
        if (layoutFlag != null) {
        	layoutFlag.set();
        }
        if (scrollThread != null) {
        	scrollThread.finish();
        }
        if (drawThread != null) {
        	drawThread.finish();
        }
    }

    @Override
    public float getScrollScaleRatio() {
        final Page page = base.getDocumentModel().getCurrentPageObject();
        if (page == null) {
            return 0;
        }
        final float zoom = base.getZoomModel().getZoom();
        return getWidth() * zoom / page.getBounds(zoom).width();
    }

    @Override
    public void stopScroller() {
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
    }

    @Override
    public final void redrawView() {
        redrawView(ViewState.get(base.getDocumentController()));
    }

    @Override
    public final void redrawView(final ViewState viewState) {
        if (viewState != null) {
            if (drawThread != null) {
                drawThread.draw(viewState);
            }
            final DecodeService ds = base.getDecodeService();
            if (ds != null) {
                ds.updateViewState(viewState);
            }
        }
    }

    @Override
    public final void surfaceCreated(final SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder(), this.getContext());
        final int drawThreadPriority = AppSettings.current().drawThreadPriority;
        if (D) {
        	Log.e(TAG, "Draw thread priority: " + drawThreadPriority);
        }
        drawThread.setPriority(drawThreadPriority);
        if (D) {
        	Log.e(TAG, ">>>drawThread.start()");
        }
        drawThread.start();
    }

    @Override
    public final void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        redrawView();
    }

    @Override
    public final void surfaceDestroyed(final SurfaceHolder holder) {
        drawThread.finish();
    }

    @Override
    public PointF getBase(final RectF viewRect) {
        return new PointF(viewRect.left, viewRect.top);
    }

    @Override
    public Bitmaps createBitmaps(final String nodeId, final BitmapRef orig, final Rect bitmapBounds, final boolean invert) {
        return new Bitmaps(nodeId, orig, bitmapBounds, invert);
    }
}
